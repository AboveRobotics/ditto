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
package org.eclipse.ditto.services.thingsearch.persistence.write.streaming;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.enforcers.AclEnforcer;
import org.eclipse.ditto.model.enforcers.Enforcer;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.services.models.things.commands.sudo.SudoRetrieveThing;
import org.eclipse.ditto.services.models.things.commands.sudo.SudoRetrieveThingResponse;
import org.eclipse.ditto.services.thingsearch.common.config.StreamCacheConfig;
import org.eclipse.ditto.services.thingsearch.common.config.StreamConfig;
import org.eclipse.ditto.services.thingsearch.persistence.write.mapping.EnforcedThingMapper;
import org.eclipse.ditto.services.thingsearch.persistence.write.model.AbstractWriteModel;
import org.eclipse.ditto.services.thingsearch.persistence.write.model.Metadata;
import org.eclipse.ditto.services.thingsearch.persistence.write.model.ThingDeleteModel;
import org.eclipse.ditto.services.utils.cache.Cache;
import org.eclipse.ditto.services.utils.cache.CacheFactory;
import org.eclipse.ditto.services.utils.cache.EntityIdWithResourceType;
import org.eclipse.ditto.services.utils.cache.entry.Entry;
import org.eclipse.ditto.services.utils.cacheloaders.PolicyEnforcerCacheLoader;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotAccessibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.dispatch.MessageDispatcher;
import akka.pattern.Patterns;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;

/**
 * Converts Thing changes into write models by retrieving data and applying enforcement via an enforcer cache.
 */
final class EnforcementFlow {

    private static final Source<Entry<Enforcer>, NotUsed> ENFORCER_NONEXISTENT = Source.single(Entry.nonexistent());

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ActorRef thingsShardRegion;
    private final Cache<EntityIdWithResourceType, Entry<Enforcer>> policyEnforcerCache;
    private final Duration thingsTimeout;
    private final Duration cacheRetryDelay;
    private final int maxArraySize;
    private final boolean deleteEvent;

    private EnforcementFlow(final ActorRef thingsShardRegion,
            final Cache<EntityIdWithResourceType, Entry<Enforcer>> policyEnforcerCache,
            final Duration thingsTimeout,
            final Duration cacheRetryDelay,
            final int maxArraySize,
            final boolean deleteEvent) {

        this.thingsShardRegion = thingsShardRegion;
        this.policyEnforcerCache = policyEnforcerCache;
        this.thingsTimeout = thingsTimeout;
        this.cacheRetryDelay = cacheRetryDelay;
        this.maxArraySize = maxArraySize;
        this.deleteEvent = deleteEvent;
    }

    /**
     * Create an EnforcementFlow object.
     *
     * @param updaterStreamConfig configuration of the updater stream.
     * @param thingsShardRegion the shard region to retrieve things from.
     * @param policiesShardRegion the shard region to retrieve policies from.
     * @param cacheDispatcher dispatcher for the enforcer cache.
     * @return an EnforcementFlow object.
     */
    public static EnforcementFlow of(final StreamConfig updaterStreamConfig,
            final ActorRef thingsShardRegion,
            final ActorRef policiesShardRegion,
            final MessageDispatcher cacheDispatcher,
            final boolean deleteEvent) {

        final Duration askTimeout = updaterStreamConfig.getAskTimeout();
        final StreamCacheConfig streamCacheConfig = updaterStreamConfig.getCacheConfig();

        final AsyncCacheLoader<EntityIdWithResourceType, Entry<Enforcer>> policyEnforcerCacheLoader =
                new PolicyEnforcerCacheLoader(askTimeout, policiesShardRegion);
        final Cache<EntityIdWithResourceType, Entry<Enforcer>> policyEnforcerCache =
                CacheFactory.createCache(policyEnforcerCacheLoader, streamCacheConfig,
                        EnforcementFlow.class.getCanonicalName() + ".cache", cacheDispatcher);

        return new EnforcementFlow(thingsShardRegion, policyEnforcerCache, askTimeout,
                streamCacheConfig.getRetryDelay(), updaterStreamConfig.getMaxArraySize(), deleteEvent);
    }

    private static EntityIdWithResourceType getPolicyEntityId(final PolicyId policyId) {
        return EntityIdWithResourceType.of(PolicyCommand.RESOURCE_TYPE, policyId);
    }

    /**
     * Extract Thing ID from SudoRetrieveThingResponse.
     * This is needed because SudoRetrieveThingResponse#id() is always the empty string.
     *
     * @param response the SudoRetrieveThingResponse.
     * @return the extracted Thing ID.
     */
    private static ThingId getThingId(final SudoRetrieveThingResponse response) {
        final String thingId = response.getEntity().asObject().getValueOrThrow(Thing.JsonFields.ID);
        return ThingId.of(thingId);
    }

    /**
     * Decide whether to reload an enforcer entry.
     * An entry should be reload if it is out-of-date, nonexistent, or corresponds to a nonexistent entity.
     *
     * @param entry the enforcer cache entry
     * @param metadata the metadata
     * @param iteration how many times cache read was attempted
     * @return whether to reload the cache
     */
    private static boolean shouldReloadCache(@Nullable final Entry<?> entry, final Metadata metadata,
            final int iteration) {

        if (iteration <= 0) {
            return entry == null || !entry.exists() ||
                    entry.getRevision() < metadata.getPolicyRevision().orElse(Long.MAX_VALUE);
        } else {
            // never attempt to reload cache more than once
            return false;
        }
    }

    /**
     * Create a flow from Thing changes to write models by retrieving data from Things shard region and enforcer cache.
     *
     * @param parallelism how many SudoRetrieveThing commands to send in parallel.
     * @return the flow.
     */
    public Flow<Map<ThingId, Metadata>, Source<AbstractWriteModel, NotUsed>, NotUsed> create(final int parallelism) {
        return Flow.<Map<ThingId, Metadata>>create().map(changeMap -> {
            log.info("Updating search index of <{}> things", changeMap.size());
            final Set<ThingId> thingIds = changeMap.keySet();
            return sudoRetrieveThingJsons(parallelism, thingIds).flatMapConcat(responseMap ->
                    Source.fromIterator(changeMap.values()::iterator).flatMapMerge(parallelism, metadataRef ->
                            computeWriteModel(metadataRef, responseMap.get(metadataRef.getThingId())))
            );
        });

    }

    private Source<Map<ThingId, SudoRetrieveThingResponse>, NotUsed> sudoRetrieveThingJsons(
            final int parallelism, final Collection<ThingId> thingIds) {

        return Source.fromIterator(thingIds::iterator)
                .flatMapMerge(parallelism, this::sudoRetrieveThing)
                .<Map<ThingId, SudoRetrieveThingResponse>>fold(new HashMap<>(), (map, response) -> {
                    map.put(getThingId(response), response);
                    return map;
                })
                .map(result -> {
                    log.info("Got SudoRetrieveThingResponse <{}> times", result.size());
                    return result;
                });
    }

    private Source<SudoRetrieveThingResponse, NotUsed> sudoRetrieveThing(final ThingId thingId) {
        final SudoRetrieveThing command =
                SudoRetrieveThing.withOriginalSchemaVersion(thingId, DittoHeaders.empty());
        final CompletionStage<Source<SudoRetrieveThingResponse, NotUsed>> responseFuture =
                // using default thread-pool for asking Things shard region
                Patterns.ask(thingsShardRegion, command, thingsTimeout)
                        .handle((response, error) -> {
                            if (response instanceof SudoRetrieveThingResponse) {
                                return Source.single((SudoRetrieveThingResponse) response);
                            } else {
                                if (error != null) {
                                    log.error("Failed " + command, error);
                                } else if (!(response instanceof ThingNotAccessibleException)) {
                                    log.error("Unexpected response for <{}>: <{}>", command, response);
                                }
                                return Source.empty();
                            }
                        });

        return Source.fromSourceCompletionStage(responseFuture)
                .viaMat(Flow.create(), Keep.none());
    }

    private Source<AbstractWriteModel, NotUsed> computeWriteModel(final Metadata metadata,
            @Nullable final SudoRetrieveThingResponse sudoRetrieveThingResponse) {

        if (sudoRetrieveThingResponse == null) {
            return deleteEvent
                    ? Source.single(ThingDeleteModel.of(metadata))
                    : Source.empty();
        } else {
            final JsonObject thing = sudoRetrieveThingResponse.getEntity().asObject();

            return getEnforcer(metadata, thing)
                    .map(entry -> {
                        if (entry.exists()) {
                            try {
                                return EnforcedThingMapper.toWriteModel(thing, entry.getValueOrThrow(),
                                        entry.getRevision(),
                                        maxArraySize);
                            } catch (final JsonRuntimeException e) {
                                log.error(e.getMessage(), e);
                                return ThingDeleteModel.of(metadata);
                            }
                        } else {
                            // no enforcer; delete thing from search index
                            return ThingDeleteModel.of(metadata);
                        }
                    });
        }
    }

    /**
     * Get the enforcer of a thing or an empty source if it does not exist.
     *
     * @param metadata metadata of the thing.
     * @param thing the thing (possibly containing ACL)
     * @return source of an enforcer or an empty source.
     */
    private Source<Entry<Enforcer>, NotUsed> getEnforcer(final Metadata metadata, final JsonObject thing) {
        final Optional<JsonObject> acl = thing.getValue(Thing.JsonFields.ACL);
        if (acl.isPresent()) {
            return Source.single(Entry.permanent(AclEnforcer.of(ThingsModelFactory.newAcl(acl.get()))));
        } else {
            return thing.getValue(Thing.JsonFields.POLICY_ID)
                    .map(PolicyId::of)
                    .map(policyId -> readCachedEnforcer(metadata, getPolicyEntityId(policyId), 0))
                    .orElse(ENFORCER_NONEXISTENT);
        }
    }

    private Source<Entry<Enforcer>, NotUsed> readCachedEnforcer(final Metadata metadata,
            final EntityIdWithResourceType policyId, final int iteration) {

        final Source<Entry<Enforcer>, ?> lazySource = Source.lazily(() -> {
            final CompletionStage<Source<Entry<Enforcer>, NotUsed>> enforcerFuture = policyEnforcerCache.get(policyId)
                    .thenApply(optionalEnforcerEntry -> {
                        if (shouldReloadCache(optionalEnforcerEntry.orElse(null), metadata, iteration)) {
                            // invalid entry; invalidate and retry after delay
                            policyEnforcerCache.invalidate(policyId);
                            return readCachedEnforcer(metadata, policyId, iteration + 1)
                                    .initialDelay(cacheRetryDelay);
                        } else {
                            return optionalEnforcerEntry.map(Source::single)
                                    .orElse(ENFORCER_NONEXISTENT);
                        }
                    })
                    .exceptionally(error -> {
                        log.error("Failed to read policyEnforcerCache", error);
                        return ENFORCER_NONEXISTENT;
                    });

            return Source.fromSourceCompletionStage(enforcerFuture);
        });

        return lazySource.viaMat(Flow.create(), Keep.none());
    }

}
