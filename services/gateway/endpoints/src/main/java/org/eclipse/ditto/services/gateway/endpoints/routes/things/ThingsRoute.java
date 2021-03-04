/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.gateway.endpoints.routes.things;

import static org.eclipse.ditto.model.base.exceptions.DittoJsonException.wrapJsonRuntimeException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.exceptions.DittoJsonException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingBuilder;
import org.eclipse.ditto.model.things.ThingDefinition;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.services.gateway.endpoints.routes.AbstractRoute;
import org.eclipse.ditto.services.gateway.endpoints.utils.UriEncoding;
import org.eclipse.ditto.services.gateway.util.config.endpoints.CommandConfig;
import org.eclipse.ditto.services.gateway.util.config.endpoints.HttpConfig;
import org.eclipse.ditto.services.gateway.util.config.endpoints.MessageConfig;
import org.eclipse.ditto.signals.commands.things.exceptions.PolicyIdNotDeletableException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingIdNotExplicitlySettableException;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingMergeInvalidException;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAclEntry;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingDefinition;
import org.eclipse.ditto.signals.commands.things.modify.MergeThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAcl;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAclEntry;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingDefinition;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAcl;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAclEntry;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttribute;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes;
import org.eclipse.ditto.signals.commands.things.query.RetrievePolicyId;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThing;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThingDefinition;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThings;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.RequestContext;
import akka.http.javadsl.server.Route;
import akka.stream.javadsl.Source;

/**
 * Builder for creating Akka HTTP routes for {@code /things}.
 */
public final class ThingsRoute extends AbstractRoute {

    public static final String PATH_THINGS = "things";
    private static final String PATH_POLICY_ID = "policyId";
    private static final String PATH_ATTRIBUTES = "attributes";
    private static final String PATH_THING_DEFINITION = "definition";
    private static final String PATH_ACL = "acl";

    private final FeaturesRoute featuresRoute;
    private final MessagesRoute messagesRoute;

    /**
     * Constructs the {@code /things} route builder.
     *
     * @param proxyActor an actor selection of the command delegating actor.
     * @param actorSystem the ActorSystem to use.
     * @param commandConfig the configuration settings of the Gateway service's incoming command processing.
     * @param messageConfig the MessageConfig.
     * @param claimMessageConfig the MessageConfig for claim messages.
     * @param httpConfig the configuration settings of the Gateway service's HTTP endpoint.
     * @param headerTranslator translates headers from external sources or to external sources.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public ThingsRoute(final ActorRef proxyActor,
            final ActorSystem actorSystem,
            final HttpConfig httpConfig,
            final CommandConfig commandConfig,
            final MessageConfig messageConfig,
            final MessageConfig claimMessageConfig,
            final HeaderTranslator headerTranslator) {

        super(proxyActor, actorSystem, httpConfig, commandConfig, headerTranslator);

        featuresRoute = new FeaturesRoute(proxyActor, actorSystem, httpConfig, commandConfig, messageConfig,
                claimMessageConfig, headerTranslator);
        messagesRoute = new MessagesRoute(proxyActor, actorSystem, httpConfig, commandConfig, messageConfig,
                claimMessageConfig, headerTranslator);
    }

    private static Thing createThingForPost(final String jsonString) {
        final JsonObject inputJson = wrapJsonRuntimeException(() -> JsonFactory.newObject(jsonString));
        if (inputJson.contains(Thing.JsonFields.ID.getPointer())) {
            throw ThingIdNotExplicitlySettableException.forPostMethod().build();
        }

        final ThingId thingId = ThingBuilder.generateRandomTypedThingId();

        final JsonObjectBuilder outputJsonBuilder = inputJson.toBuilder();
        outputJsonBuilder.set(Thing.JsonFields.ID.getPointer(), thingId.toString());

        return ThingsModelFactory.newThingBuilder(outputJsonBuilder.build())
                .setId(thingId)
                .build();
    }

    @Nullable
    private static JsonObject createInlinePolicyJson(final String jsonString) {
        final JsonObject inputJson = wrapJsonRuntimeException(() -> JsonFactory.newObject(jsonString));
        return inputJson.getValue(Policy.INLINED_FIELD_NAME)
                .map(jsonValue -> wrapJsonRuntimeException(jsonValue::asObject))
                .orElse(null);
    }

    @Nullable
    private static String getCopyPolicyFrom(final String jsonString) {
        final JsonObject inputJson = wrapJsonRuntimeException(() -> JsonFactory.newObject(jsonString));
        return inputJson.getValue(ModifyThing.JSON_COPY_POLICY_FROM).orElse(null);
    }

    /**
     * Builds the {@code /things} route.
     *
     * @return the {@code /things} route.
     */
    public Route buildThingsRoute(final RequestContext ctx, final DittoHeaders dittoHeaders) {
        return rawPathPrefix(PathMatchers.slash().concat(PATH_THINGS), () ->
                concat(
                        things(ctx, dittoHeaders),
                        rawPathPrefix(PathMatchers.slash().concat(PathMatchers.segment()),
                                // /things/<thingId>
                                thingId -> buildThingEntryRoute(ctx, dittoHeaders, ThingId.of(thingId))
                        )
                )
        );
    }

    private Route buildThingEntryRoute(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {
        return concat(
                thingsEntry(ctx, dittoHeaders, thingId),
                thingsEntryPolicyId(ctx, dittoHeaders, thingId),
                thingsEntryAcl(ctx, dittoHeaders, thingId),
                thingsEntryAclEntry(ctx, dittoHeaders, thingId),
                thingsEntryAttributes(ctx, dittoHeaders, thingId),
                thingsEntryAttributesEntry(ctx, dittoHeaders, thingId),
                thingsEntryDefinition(ctx, dittoHeaders, thingId),
                thingsEntryFeatures(ctx, dittoHeaders, thingId),
                thingsEntryInboxOutbox(ctx, dittoHeaders, thingId)
        );
    }

    /*
     * Describes {@code /things} route.
     *
     * @return {@code /things} route.
     */
    private Route things(final RequestContext ctx, final DittoHeaders dittoHeaders) {
        return pathEndOrSingleSlash(() ->
                concat(
                        // GET /things?ids=<idsString>&fields=<fieldsString>
                        get(() -> buildRetrieveThingsRoute(ctx, dittoHeaders)),
                        // POST /things
                        post(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                        thingJson -> CreateThing.of(createThingForPost(thingJson),
                                                createInlinePolicyJson(thingJson), getCopyPolicyFrom(thingJson),
                                                dittoHeaders)
                                )
                                )
                        )
                )
        );
    }

    private Route buildRetrieveThingsRoute(final RequestContext ctx, final DittoHeaders dittoHeaders) {
        return parameter(ThingsParameter.IDS.toString(), idsString ->
                parameterOptional(ThingsParameter.FIELDS.toString(), fieldsString ->
                        handlePerRequest(ctx, dittoHeaders, Source.empty(), emptyRequestBody -> RetrieveThings
                                .getBuilder(
                                        idsString.isEmpty() ? Collections.emptyList() : splitThingIdString(idsString))
                                .selectedFields(calculateSelectedFields(fieldsString))
                                .dittoHeaders(dittoHeaders).build())
                )

        );
    }

    private List<ThingId> splitThingIdString(final String thingIdString) {
        return Arrays.stream(thingIdString.split(","))
                .map(ThingId::of)
                .collect(Collectors.toList());
    }

    /*
     * Describes {@code /things/<thingId>} route.
     * @return {@code /things/<thingId>} route.
     */
    private Route thingsEntry(final RequestContext ctx, final DittoHeaders dittoHeaders, final ThingId thingId) {
        return pathEndOrSingleSlash(() ->
                concat(
                        // GET /things/<thingId>?fields=<fieldsString>
                        get(() -> parameterOptional(ThingsParameter.FIELDS.toString(), fieldsString ->
                                        handlePerRequest(ctx, RetrieveThing.getBuilder(thingId, dittoHeaders)
                                                .withSelectedFields(calculateSelectedFields(fieldsString)
                                                        .orElse(null))
                                                .build())
                                )
                        ),
                        // PUT /things/<thingId>
                        put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                        thingJson -> ModifyThing.of(thingId,
                                                ThingsModelFactory.newThingBuilder(
                                                        ThingJsonObjectCreator.newInstance(thingJson,
                                                                thingId.toString()).forPut()).build(),
                                                createInlinePolicyJson(thingJson),
                                                getCopyPolicyFrom(thingJson),
                                                dittoHeaders)))
                        ),
                        // PATCH /things/<thingId>
                        patch(() -> ensureMediaTypeMergePatchJsonThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                        thingJson -> MergeThing.withThing(thingId,
                                                thingFromJsonForPatch(thingJson, thingId, dittoHeaders),
                                                dittoHeaders)))
                        ),
                        // DELETE /things/<thingId>
                        delete(() -> handlePerRequest(ctx, DeleteThing.of(thingId, dittoHeaders)))
                )
        );
    }

    private static Thing thingFromJsonForPatch(final String thingJson, final ThingId thingId,
            final DittoHeaders dittoHeaders) {
        if (JsonFactory.readFrom(thingJson).isNull() &&
                dittoHeaders.getSchemaVersion().filter(JsonSchemaVersion.V_2::equals).isPresent()) {
            throw ThingMergeInvalidException.fromMessage(
                    "The provided json value can not be applied at this resource", dittoHeaders);
        }
        return ThingsModelFactory.newThingBuilder(
                ThingJsonObjectCreator.newInstance(thingJson, thingId.toString()).forPatch()).build();
    }

    /*
     * Describes {@code /things/<thingId>/policyId} route.
     *
     * @return {@code /things/<thingId>/policyId} route.
     */
    private Route thingsEntryPolicyId(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {
        // /things/<thingId>/policyId
        return path(PATH_POLICY_ID, () ->
                concat(
                        // GET /things/<thingId>/policyId
                        get(() -> handlePerRequest(ctx, RetrievePolicyId.of(thingId, dittoHeaders))),
                        // PUT /things/<thingId>/policyId
                        put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                        policyIdJson -> ModifyPolicyId.of(thingId, policyIdFromJson(policyIdJson),
                                                dittoHeaders))
                                )
                        ),
                        // PATCH /things/<thingId>/policyId
                        patch(() -> ensureMediaTypeMergePatchJsonThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                        policyIdJson -> MergeThing.withPolicyId(thingId,
                                                policyIdFromJsonForPatch(policyIdJson),
                                                dittoHeaders))
                                )
                        )
                )
        );
    }

    private static PolicyId policyIdFromJsonForPatch(final String policyIdJson) {
        if (JsonFactory.readFrom(policyIdJson).isNull()) {
            throw PolicyIdNotDeletableException.newBuilder().build();
        }
        return policyIdFromJson(policyIdJson);
    }

    private static PolicyId policyIdFromJson(final String policyIdJson) {
        return PolicyId.of(Optional.of(JsonFactory.readFrom(policyIdJson))
                .filter(JsonValue::isString)
                .map(JsonValue::asString)
                .orElse(policyIdJson));
    }

    /*
     * Describes {@code /things/<thingId>/acl} route.
     *
     * @return {@code /things/<thingId>/acl} route.
     */
    private Route thingsEntryAcl(final RequestContext ctx, final DittoHeaders dittoHeaders, final ThingId thingId) {
        // /things/<thingId>/acl
        return rawPathPrefix(PathMatchers.slash().concat(PATH_ACL), () ->
                pathEndOrSingleSlash(() ->
                        concat(
                                // GET /things/<thingId>/acl
                                get(() -> handlePerRequest(ctx, RetrieveAcl.of(thingId, dittoHeaders))),
                                // PUT /things/<thingId>/acl
                                put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                        payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource, aclJson ->
                                                ModifyAcl.of(thingId, ThingsModelFactory.newAcl(aclJson), dittoHeaders))
                                        )
                                )
                        )
                )
        );
    }

    /*
     * Describes {@code /things/<thingId>/acl/<authorizationSubject>} route.
     *
     * @return {@code /things/<thingId>/acl/<authorizationSubject>} route.
     */
    private Route thingsEntryAclEntry(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {
        return rawPathPrefix(PathMatchers.slash().concat(PATH_ACL), () ->
                rawPathPrefix(PathMatchers.slash().concat(PathMatchers.segment()), subject ->
                        pathEndOrSingleSlash(() ->
                                concat(
                                        // GET /things/<thingId>/acl/<authorizationSubject>?fields=<fieldsString>
                                        get(() -> parameterOptional(ThingsParameter.FIELDS.toString(),
                                                fieldsString -> handlePerRequest(ctx, RetrieveAclEntry.of(thingId,
                                                        AuthorizationModelFactory.newAuthSubject(subject),
                                                        calculateSelectedFields(fieldsString).orElse(null),
                                                        dittoHeaders))
                                                )
                                        ),
                                        // PUT /things/<thingId>/acl/<authorizationSubject>
                                        put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx,
                                                dittoHeaders, payloadSource ->
                                                        handlePerRequest(ctx, dittoHeaders, payloadSource,
                                                                aclEntryJson -> ModifyAclEntry.of(thingId,
                                                                        ThingsModelFactory.newAclEntry(subject,
                                                                                JsonFactory.readFrom(aclEntryJson)),
                                                                        dittoHeaders))
                                                )
                                        ),
                                        // DELETE /things/<thingId>/acl/<authorizationSubject>
                                        delete(() -> handlePerRequest(ctx, DeleteAclEntry.of(thingId,
                                                AuthorizationModelFactory.newAuthSubject(subject),
                                                dittoHeaders))
                                        )
                                )
                        )
                )
        );
    }

    /*
     * Describes {@code /things/<thingId>/attributes} route.
     *
     * @return {@code /things/<thingId>/attributes} route.
     */
    private Route thingsEntryAttributes(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {

        return rawPathPrefix(PathMatchers.slash().concat(PATH_ATTRIBUTES), () ->
                pathEndOrSingleSlash(() ->
                        concat(
                                // GET /things/<thingId>/attributes?fields=<fieldsString>
                                get(() -> parameterOptional(ThingsParameter.FIELDS.toString(), fieldsString ->
                                                handlePerRequest(ctx, RetrieveAttributes.of(thingId,
                                                        calculateSelectedFields(fieldsString).orElse(null),
                                                        dittoHeaders))
                                        )
                                ),
                                // PUT /things/<thingId>/attributes
                                put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                        payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                                attributesJson -> ModifyAttributes.of(thingId,
                                                        ThingsModelFactory.newAttributes(attributesJson),
                                                        dittoHeaders))
                                        )
                                ),
                                // PATCH /things/<thingId>/attributes
                                patch(() -> ensureMediaTypeMergePatchJsonThenExtractDataBytes(ctx, dittoHeaders,
                                        payloadSource -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                                attributesJson -> MergeThing.withAttributes(thingId,
                                                        ThingsModelFactory.newAttributes(attributesJson), dittoHeaders))
                                        )
                                ),
                                // DELETE /things/<thingId>/attributes
                                delete(() -> handlePerRequest(ctx, DeleteAttributes.of(thingId, dittoHeaders)))
                        )
                )
        );
    }

    /*
     * Describes {@code /things/<thingId>/attributes/<attributesSelector>} route.
     *
     * {@code attributeJsonPointer} JSON pointer to GET/PUT/PATCH/DELETE e.g.:
     * <pre>
     *    GET /things/fancy-car-1/attributes/model
     *    PUT /things/fancy-car-1/attributes/someProp
     *    PATCH /things/fancy-car-1/attributes/someProp
     *    DELETE /things/fancy-car-1/attributes/foo/bar
     * </pre>
     *
     * @return {@code /things/<thingId>/attributes/<attributeJsonPointer>} route.
     */
    private Route thingsEntryAttributesEntry(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {

        return rawPathPrefix(PathMatchers.slash()
                        .concat(PATH_ATTRIBUTES)
                        .concat(PathMatchers.slash())
                        .concat(PathMatchers.remaining())
                        .map(path -> UriEncoding.decode(path, UriEncoding.EncodingType.RFC3986))
                        .map(path -> "/" + path), // Prepend slash to path to fail request with double slashes
                jsonPointerString -> concat(
                        // GET /things/<thingId>/attributes/<attributePointerStr>
                        get(() ->
                                handlePerRequest(ctx, RetrieveAttribute.of(thingId,
                                        JsonFactory.newPointer(jsonPointerString),
                                        dittoHeaders))
                        ),
                        // PUT /things/<thingId>/attributes/<attributePointerStr>
                        put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource ->
                                        handlePerRequest(ctx, dittoHeaders, payloadSource, attributeValueJson ->
                                                ModifyAttribute.of(thingId,
                                                        JsonFactory.newPointer(jsonPointerString),
                                                        DittoJsonException.wrapJsonRuntimeException(() ->
                                                                JsonFactory.readFrom(attributeValueJson)),
                                                        dittoHeaders))
                                )
                        ),
                        // PATCH /things/<thingId>/attributes/<attributePointerStr>
                        patch(() -> ensureMediaTypeMergePatchJsonThenExtractDataBytes(ctx, dittoHeaders,
                                payloadSource ->
                                        handlePerRequest(ctx, dittoHeaders, payloadSource, attributeValueJson ->
                                                MergeThing.withAttribute(thingId,
                                                        JsonFactory.newPointer(jsonPointerString),
                                                        DittoJsonException.wrapJsonRuntimeException(() ->
                                                                JsonFactory.readFrom(attributeValueJson)),
                                                        dittoHeaders)
                                        )
                                )
                        ),
                        // DELETE /things/<thingId>/attributes/<attributePointerStr>
                        delete(() -> handlePerRequest(ctx,
                                DeleteAttribute.of(thingId, JsonFactory.newPointer(jsonPointerString), dittoHeaders))
                        )
                )
        );
    }

    /*
     * Describes {@code /things/<thingId>/definition} route.
     *
     * @return {@code /things/<thingId>/definition} route.
     */
    private Route thingsEntryDefinition(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {

        return rawPathPrefix(PathMatchers.slash().concat(PATH_THING_DEFINITION), () ->
                pathEndOrSingleSlash(() ->
                        concat(
                                // GET /things/<thingId>/definition
                                get(() -> handlePerRequest(ctx, RetrieveThingDefinition.of(thingId, dittoHeaders)
                                        )
                                ),
                                // PUT /things/<thingId>/definition
                                put(() -> ensureMediaTypeJsonWithFallbacksThenExtractDataBytes(ctx, dittoHeaders,
                                        payloadSource ->
                                                pathEnd(() -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                                        definitionJson -> ModifyThingDefinition.of(thingId,
                                                                getDefinitionFromJson(definitionJson),
                                                                dittoHeaders))
                                                )
                                        )
                                ),
                                // PATCH /things/<thingId>/definition
                                patch(() -> ensureMediaTypeMergePatchJsonThenExtractDataBytes(ctx, dittoHeaders,
                                        payloadSource ->
                                                pathEnd(() -> handlePerRequest(ctx, dittoHeaders, payloadSource,
                                                        definitionJson -> MergeThing.withThingDefinition(thingId,
                                                                getDefinitionFromJson(definitionJson), dittoHeaders))
                                                )
                                        )
                                ),
                                // DELETE /things/<thingId>/definition
                                delete(() -> handlePerRequest(ctx, DeleteThingDefinition.of(thingId, dittoHeaders)))
                        )
                )
        );
    }

    private ThingDefinition getDefinitionFromJson(final String definitionJson) {
        return DittoJsonException.wrapJsonRuntimeException(() -> {
            final JsonValue jsonValue = JsonFactory.readFrom(definitionJson);
            if (jsonValue.isNull()) {
                return ThingsModelFactory.nullDefinition();
            } else {
                return ThingsModelFactory.newDefinition(jsonValue.asString());
            }
        });
    }

    /*
     * Describes {@code /things/<thingId>/features} route.
     *
     * @return {@code /things/<thingId>/features} route.
     */
    private Route thingsEntryFeatures(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {

        // /things/<thingId>/features
        return featuresRoute.buildFeaturesRoute(ctx, dittoHeaders, thingId);
    }

    /*
     * Describes {@code /things/<thingId>/{inbox|outbox}} route.
     *
     * @return {@code /things/<thingId>/{inbox|outbox}} route.
     */
    private Route thingsEntryInboxOutbox(final RequestContext ctx, final DittoHeaders dittoHeaders,
            final ThingId thingId) {

        // /things/<thingId>/<inbox|outbox>
        return messagesRoute.buildThingsInboxOutboxRoute(ctx, dittoHeaders, thingId);
    }

}
