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
package org.eclipse.ditto.services.connectivity.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonParseOptions;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.messages.SendClaimMessage;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingResponse;
import org.eclipse.ditto.signals.events.things.AttributeDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.ConfigFactory;

/**
 * Tests {@link org.eclipse.ditto.services.connectivity.mapping.NormalizedMessageMapper}.
 */
public final class NormalizedMessageMapperTest {

    private static final ProtocolAdapter ADAPTER = DittoProtocolAdapter.newInstance();

    private MessageMapper underTest;

    @Before
    public void setUp() {
        underTest = new NormalizedMessageMapper();
    }

    @Test
    public void thingCreated() {
        final ThingCreated event = ThingCreated.of(ThingsModelFactory.newThingBuilder()
                .setId(ThingId.of("thing:created"))
                .setPolicyId(PolicyId.of("thing:created"))
                .setAttributes(Attributes.newBuilder().set("x", 5).build())
                .setFeatures(Features.newBuilder().set(Feature.newBuilder()
                        .properties(JsonObject.of("{\"y\":6}"))
                        .withId("feature")
                        .build()))
                .build(), 1L, Instant.ofEpochSecond(1L), DittoHeaders.empty());

        final Adaptable adaptable = ADAPTER.toAdaptable(event);
        Assertions.assertThat(mapToJson(adaptable))
                .isEqualTo(JsonObject.of("{\n" +
                        "  \"thingId\": \"thing:created\",\n" +
                        "  \"policyId\": \"thing:created\",\n" +
                        "  \"attributes\": {\n" +
                        "    \"x\": 5\n" +
                        "  },\n" +
                        "  \"features\": {\n" +
                        "    \"feature\": {\n" +
                        "      \"properties\": {\n" +
                        "        \"y\": 6\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"_modified\": \"1970-01-01T00:00:01Z\",\n" +
                        "  \"_revision\": 1,\n" +
                        "  \"_context\": {\n" +
                        "    \"topic\": \"thing/created/things/twin/events/created\",\n" +
                        "    \"path\": \"/\",\n" +
                        "    \"headers\": {\n" +
                        "      \"content-type\": \"application/vnd.eclipse.ditto+json\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void featurePropertyModified() {
        final FeaturePropertyModified event = FeaturePropertyModified.of(
                ThingId.of("thing:id"),
                "featureId",
                JsonPointer.of("/the/quick/brown/fox/jumps/over/the/lazy/dog"),
                JsonValue.of(9),
                2L,
                Instant.ofEpochSecond(2L),
                DittoHeaders.empty());

        final Adaptable adaptable = ADAPTER.toAdaptable(event);
        Assertions.assertThat(mapToJson(adaptable))
                .isEqualTo(JsonObject.of("{\n" +
                        "  \"thingId\": \"thing:id\",\n" +
                        "  \"features\": {\n" +
                        "    \"featureId\": {\n" +
                        "      \"properties\": {\"the\":{\"quick\":{\"brown\":{\"fox\":{\"jumps\":{\"over\":{" +
                        "        \"the\":{\"lazy\":{\"dog\":9}}}}}}}}}\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"_modified\": \"1970-01-01T00:00:02Z\",\n" +
                        "  \"_revision\": 2,\n" +
                        "  \"_context\": {\n" +
                        "    \"topic\": \"thing/id/things/twin/events/modified\",\n" +
                        "    \"path\": \"/features/featureId/properties/the/quick/brown/fox/jumps/over/the/lazy/dog\",\n" +
                        "    \"headers\": {\n" +
                        "      \"content-type\": \"application/vnd.eclipse.ditto+json\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void headersFromAdaptableAreNotMapped() {
        final FeaturePropertyModified event = FeaturePropertyModified.of(
                ThingId.of("thing:id"),
                "featureId",
                JsonPointer.of("/the/quick/brown/fox/jumps/over/the/lazy/dog"),
                JsonValue.of(9),
                2L,
                Instant.ofEpochSecond(2L),
                DittoHeaders.newBuilder().putHeader("random", "header").build());

        final Adaptable adaptable = ADAPTER.toAdaptable(event, TopicPath.Channel.TWIN);
        Assertions.assertThat(underTest.map(adaptable).get(0).getHeaders()).isEmpty();
    }

    @Test
    public void withFieldSelection() {
        final FeaturePropertyModified event = FeaturePropertyModified.of(
                ThingId.of("thing:id"),
                "featureId",
                JsonPointer.of("/the/quick/brown/fox/jumps/over/the/lazy/dog"),
                JsonValue.of(9),
                2L,
                Instant.ofEpochSecond(2L),
                DittoHeaders.empty());

        underTest.configure(DefaultMappingConfig.of(ConfigFactory.load("mapping-test")),
                DefaultMessageMapperConfiguration.of("normalizer",
                        Collections.singletonMap(NormalizedMessageMapper.FIELDS,
                                "_modified,_context/topic,_context/headers/content-type,nonexistent/json/pointer")));

        final Adaptable adaptable = ADAPTER.toAdaptable(event);
        Assertions.assertThat(mapToJson(adaptable))
                .isEqualTo(JsonObject.of("{\n" +
                        "  \"_modified\": \"1970-01-01T00:00:02Z\",\n" +
                        "  \"_context\": {\n" +
                        "    \"topic\": \"thing/id/things/twin/events/modified\",\n" +
                        "    \"headers\": {\n" +
                        "      \"content-type\": \"application/vnd.eclipse.ditto+json\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void withFullThingPayloadFieldSelection() {
        final ThingCreated event = ThingCreated.of(ThingsModelFactory.newThingBuilder()
                .setId(ThingId.of("thing:created"))
                .setPolicyId(PolicyId.of("thing:created"))
                .setAttributes(Attributes.newBuilder().set("x", 5).build())
                .setFeatures(Features.newBuilder().set(Feature.newBuilder()
                        .properties(JsonObject.of("{\"y\":6}"))
                        .withId("feature")
                        .build()))
                .build(), 1L, Instant.ofEpochSecond(1L), DittoHeaders.empty());

        underTest.configure(DefaultMappingConfig.of(ConfigFactory.load("mapping-test")),
                DefaultMessageMapperConfiguration.of("normalizer",
                        Collections.singletonMap(NormalizedMessageMapper.FIELDS,
                                "thingId,policyId,attributes,features,_modified,_revision,_context(topic,path)," +
                                        "_context/headers/correlation-id")));

        final Adaptable adaptable = ADAPTER.toAdaptable(event);
        Assertions.assertThat(mapToJson(adaptable))
                .isEqualTo(JsonObject.of("{\n" +
                        "  \"thingId\": \"thing:created\",\n" +
                        "  \"policyId\": \"thing:created\",\n" +
                        "  \"attributes\": {\"x\": 5},\n" +
                        "  \"features\":{\"feature\":{\"properties\":{\"y\":6}}},\n" +
                        "  \"_modified\": \"1970-01-01T00:00:01Z\",\n" +
                        "  \"_revision\": 1,\n" +
                        "  \"_context\": {\n" +
                        "    \"topic\": \"thing/created/things/twin/events/created\",\n" +
                        "    \"path\": \"/\"\n" +
                        "  }\n" +
                        "}"));
    }

    @Test
    public void withExtraFieldsBeingIncluded() {
        final ThingId thingId = ThingId.of("thing:feature-modified");
        final ThingEvent<?> event =
                FeaturePropertyModified.of(thingId, "my-feature", JsonPointer.of("abc"), JsonValue.of(false), 2L,
                        Instant.ofEpochSecond(1L), DittoHeaders.empty());

        underTest.configure(DefaultMappingConfig.of(ConfigFactory.load("mapping-test")),
                DefaultMessageMapperConfiguration.of("normalizer",
                        Collections.singletonMap(NormalizedMessageMapper.FIELDS,
                                "thingId,policyId,attributes/foo,features,_modified,_revision")));

        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setPolicyId(PolicyId.of(thingId))
                .setAttributes(Attributes.newBuilder()
                        .set("some-attr", 42)
                        .set("foo", "bar")
                        .build())
                .setFeature(Feature.newBuilder()
                        .properties(JsonObject.of("{\"abc\":false,\"def\":true}"))
                        .withId("my-feature")
                        .build())
                .build();

        final Adaptable adaptable = ADAPTER.toAdaptable(event);
        final Adaptable adaptableWithExtra = ProtocolFactory.setExtra(adaptable, thing.toJson(
                JsonFactory.newFieldSelector("policyId,attributes,features/my-feature/properties/def",
                        JsonParseOptions.newBuilder().withoutUrlDecoding().build())));
        Assertions.assertThat(mapToJson(adaptableWithExtra))
                .isEqualTo(JsonObject.of("{\n" +
                        "  \"thingId\": \"thing:feature-modified\",\n" +
                        "  \"policyId\": \"thing:feature-modified\",\n" +
                        "  \"attributes\": {\"foo\": \"bar\"},\n" +
                        "  \"features\":{\"my-feature\":{\"properties\":{\"abc\":false,\"def\":true}}},\n" +
                        "  \"_modified\": \"1970-01-01T00:00:01Z\",\n" +
                        "  \"_revision\": 2\n" +
                        "}"));
    }

    @Test
    public void deletedEventsAreNotMapped() {
        assertNotMapped(AttributeDeleted.of(ThingId.of("thing:id"), JsonPointer.of("/the/quick/brown/fox/"), 3L,
                Instant.ofEpochSecond(3L), DittoHeaders.empty()));
        assertNotMapped(FeaturePropertyDeleted.of(ThingId.of("thing:id"), "featureId",
                JsonPointer.of("jumps/over/the/lazy/dog"), 4L, Instant.ofEpochSecond(4L), DittoHeaders.empty()));
        assertNotMapped(FeatureDeleted.of(ThingId.of("thing:id"), "featureId", 5L, DittoHeaders.empty()));
        assertNotMapped(ThingDeleted.of(ThingId.of("thing:id"), 6L, DittoHeaders.empty()));
    }

    @Test
    public void nonThingEventsAreNotMapped() {
        // command
        assertNotMapped(DeleteThing.of(ThingId.of("thing:id"), DittoHeaders.empty()));

        // response
        assertNotMapped(ModifyThingResponse.modified(ThingId.of("thing:id"), DittoHeaders.empty()));

        // message
        assertNotMapped(SendClaimMessage.of(ThingId.of("thing:id"),
                Message.newBuilder(
                        MessageHeaders.newBuilder(MessageDirection.TO, ThingId.of("thing:id"), "subject").build()
                ).build(),
                DittoHeaders.newBuilder().channel(TopicPath.Channel.LIVE.getName()).build()
        ));
    }

    private void assertNotMapped(final Signal signal) {
        assertThat(underTest.map(ADAPTER.toAdaptable(signal))).isEmpty();
    }

    private JsonObject mapToJson(final Adaptable message) {
        return underTest.map(message)
                .stream()
                .findFirst()
                .flatMap(ExternalMessage::getTextPayload)
                .map(JsonObject::of)
                .orElse(JsonObject.empty());
    }
}
