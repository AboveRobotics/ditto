/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.models.signalenrichment;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.services.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.services.utils.akka.logging.ThreadSafeDittoLogger;
import org.eclipse.ditto.services.utils.cache.Cache;
import org.eclipse.ditto.services.utils.cache.CacheFactory;
import org.eclipse.ditto.services.utils.cache.EntityIdWithResourceType;
import org.eclipse.ditto.services.utils.cache.config.CacheConfig;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingEvent;

/**
 * Retrieve additional parts of things by asking an asynchronous cache.
 * Instantiated once per cluster node so that it builds up a cache across all signal enrichments on a local cluster
 * node.
 */
public final class CachingSignalEnrichmentFacade implements SignalEnrichmentFacade {

    private static final ThreadSafeDittoLogger LOGGER = DittoLoggerFactory
            .getThreadSafeLogger(CachingSignalEnrichmentFacade.class);

    private final Cache<EntityIdWithResourceType, JsonObject> extraFieldsCache;

    private CachingSignalEnrichmentFacade(
            final SignalEnrichmentFacade cacheLoaderFacade,
            final CacheConfig cacheConfig,
            final Executor cacheLoaderExecutor,
            final String cacheNamePrefix) {

        extraFieldsCache = CacheFactory.createCache(
                SignalEnrichmentCacheLoader.of(cacheLoaderFacade),
                cacheConfig,
                cacheNamePrefix + "_signal_enrichment_cache",
                cacheLoaderExecutor);
    }

    /**
     * Create a signal-enriching facade that retrieves partial things by using a Caffeine cache.
     *
     * @param cacheLoaderFacade the facade whose argument-result-pairs we are caching.
     * @param cacheConfig the cache configuration to use for the cache.
     * @param cacheLoaderExecutor the executor to use in order to asynchronously load cache entries.
     * @param cacheNamePrefix the prefix to use as cacheName of the cache.
     * @return The facade.
     * @throws NullPointerException if any argument is null.
     */
    public static CachingSignalEnrichmentFacade of(final SignalEnrichmentFacade cacheLoaderFacade,
            final CacheConfig cacheConfig, final Executor cacheLoaderExecutor, final String cacheNamePrefix) {

        return new CachingSignalEnrichmentFacade(cacheLoaderFacade, cacheConfig, cacheLoaderExecutor,
                cacheNamePrefix);
    }

    @Override
    public CompletionStage<JsonObject> retrievePartialThing(final ThingId thingId,
            final JsonFieldSelector jsonFieldSelector,
            final DittoHeaders dittoHeaders,
            @Nullable final Signal<?> concernedSignal) {

        if (concernedSignal instanceof ThingDeleted && !(ProtocolAdapter.isLiveSignal(concernedSignal))) {
            // twin deleted events should not be enriched, return empty JsonObject
            return CompletableFuture.completedFuture(JsonObject.empty());
        }

        // as second step only return what was originally requested as fields:
        return doRetrievePartialThing(thingId, jsonFieldSelector, dittoHeaders, concernedSignal)
                .thenApply(jsonObject -> jsonObject.get(jsonFieldSelector));
    }

    private CompletionStage<JsonObject> doRetrievePartialThing(final ThingId thingId,
            final JsonFieldSelector jsonFieldSelector,
            final DittoHeaders dittoHeaders,
            @Nullable final Signal<?> concernedSignal) {

        final JsonFieldSelector enhancedFieldSelector = JsonFactory.newFieldSelectorBuilder()
                .addPointers(jsonFieldSelector)
                .addFieldDefinition(Thing.JsonFields.REVISION) // additionally always select the revision
                .build();

        final EntityIdWithResourceType idWithResourceType =
                EntityIdWithResourceType.of(ThingCommand.RESOURCE_TYPE, thingId,
                        CacheFactory.newCacheLookupContext(dittoHeaders, enhancedFieldSelector));

        if (concernedSignal instanceof ThingEvent && !(ProtocolAdapter.isLiveSignal(concernedSignal))) {
            final ThingEvent<?> thingEvent = (ThingEvent<?>) concernedSignal;
            return smartUpdateCachedObject(enhancedFieldSelector, idWithResourceType, thingEvent);
        }
        return doCacheLookup(idWithResourceType, dittoHeaders);
    }

    private CompletableFuture<JsonObject> doCacheLookup(final EntityIdWithResourceType idWithResourceType,
            final DittoHeaders dittoHeaders) {

        LOGGER.withCorrelationId(dittoHeaders)
                .debug("Looking up cache entry for <{}>", idWithResourceType);
        return extraFieldsCache.get(idWithResourceType)
                .thenApply(optionalJsonObject -> optionalJsonObject.orElseGet(JsonObject::empty));
    }

    private CompletableFuture<JsonObject> smartUpdateCachedObject(
            final JsonFieldSelector enhancedFieldSelector,
            final EntityIdWithResourceType idWithResourceType,
            final ThingEvent<?> thingEvent) {

        final DittoHeaders dittoHeaders = thingEvent.getDittoHeaders();
        return doCacheLookup(idWithResourceType, dittoHeaders).thenCompose(cachedJsonObject -> {
            final JsonObjectBuilder jsonObjectBuilder = cachedJsonObject.toBuilder();
            final long cachedRevision = cachedJsonObject.getValue(Thing.JsonFields.REVISION).orElse(0L);
            if (cachedRevision == thingEvent.getRevision()) {
                // the cache entry was not present before and just loaded
                return CompletableFuture.completedFuture(cachedJsonObject);
            } else if (cachedRevision + 1 == thingEvent.getRevision()) {
                // the cache entry was already present and the thingEvent was the next expected revision no
                // -> we have all information necessary to calculate it without making another roundtrip
                return handleNextExpectedThingEvent(enhancedFieldSelector, idWithResourceType, thingEvent,
                        jsonObjectBuilder);
            } else {
                // the cache entry was already present, but we missed sth and need to invalidate the cache
                // and to another cache lookup (via roundtrip)
                extraFieldsCache.invalidate(idWithResourceType);
                return doCacheLookup(idWithResourceType, dittoHeaders);
            }
        });
    }

    private CompletionStage<JsonObject> handleNextExpectedThingEvent(final JsonFieldSelector enhancedFieldSelector,
            final EntityIdWithResourceType idWithResourceType, final ThingEvent<?> thingEvent,
            final JsonObjectBuilder jsonObjectBuilder) {

        final JsonPointer resourcePath = thingEvent.getResourcePath();
        if (Thing.JsonFields.POLICY_ID.getPointer().equals(resourcePath) ||
                resourcePath.toString().startsWith(Thing.JsonFields.ACL.getPointer().toString())) {
            // invalidate the cache
            extraFieldsCache.invalidate(idWithResourceType);
            // and to another cache lookup (via roundtrip):
            return doCacheLookup(idWithResourceType, thingEvent.getDittoHeaders());
        }
        final Optional<JsonValue> optEntity = thingEvent.getEntity();
        if (resourcePath.isEmpty() && optEntity.filter(JsonValue::isObject).isPresent()) {
            optEntity.map(JsonValue::asObject).ifPresent(jsonObjectBuilder::setAll);
        } else {
            optEntity.ifPresent(entity -> jsonObjectBuilder
                    .set(resourcePath.toString(), entity)
            );
        }
        jsonObjectBuilder.set(Thing.JsonFields.REVISION, thingEvent.getRevision());
        final JsonObject enhancedJsonObject = jsonObjectBuilder.build().get(enhancedFieldSelector);
        // update local cache with enhanced object:
        extraFieldsCache.put(idWithResourceType, enhancedJsonObject);
        return CompletableFuture.completedFuture(enhancedJsonObject);
    }

}
