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
package org.eclipse.ditto.protocoladapter.things;

import static org.eclipse.ditto.protocoladapter.TopicPath.Channel.LIVE;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.LiveTwinTest;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.EventsTopicPathBuilder;
import org.eclipse.ditto.protocoladapter.Payload;
import org.eclipse.ditto.protocoladapter.ProtocolAdapterTest;
import org.eclipse.ditto.protocoladapter.TestConstants;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.protocoladapter.TopicPathBuilder;
import org.eclipse.ditto.protocoladapter.UnknownEventException;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.signals.events.things.AclEntryCreated;
import org.eclipse.ditto.signals.events.things.AclEntryDeleted;
import org.eclipse.ditto.signals.events.things.AclEntryModified;
import org.eclipse.ditto.signals.events.things.AclModified;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeDeleted;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.eclipse.ditto.signals.events.things.AttributesCreated;
import org.eclipse.ditto.signals.events.things.AttributesDeleted;
import org.eclipse.ditto.signals.events.things.AttributesModified;
import org.eclipse.ditto.signals.events.things.FeatureCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionModified;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertyCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.FeaturesCreated;
import org.eclipse.ditto.signals.events.things.FeaturesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturesModified;
import org.eclipse.ditto.signals.events.things.PolicyIdCreated;
import org.eclipse.ditto.signals.events.things.PolicyIdModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDefinitionCreated;
import org.eclipse.ditto.signals.events.things.ThingDefinitionDeleted;
import org.eclipse.ditto.signals.events.things.ThingDefinitionModified;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.eclipse.ditto.signals.events.things.ThingModified;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ThingEventAdapter}.
 */
public final class ThingEventAdapterTest extends LiveTwinTest implements ProtocolAdapterTest {

    private ThingEventAdapter underTest;

    @Before
    public void setUp() {
        underTest = ThingEventAdapter.of(DittoProtocolAdapter.getHeaderTranslator());
    }

    @Test(expected = UnknownEventException.class)
    public void unknownEventToAdaptable() {
        underTest.toAdaptable(new UnknownThingEvent(), channel);
    }

    @Test
    public void thingCreatedFromAdaptable() {
        final Instant now = Instant.now();
        final ThingCreated expected =
                ThingCreated.of(TestConstants.THING, TestConstants.REVISION, now,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.empty();

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.THING.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .withTimestamp(now)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void thingCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.empty();

        final Instant now = Instant.now();
        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.THING.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .withTimestamp(now)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final ThingCreated thingCreated =
                ThingCreated.of(TestConstants.THING, TestConstants.REVISION, now,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(thingCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void thingModifiedFromAdaptable() {
        final ThingModified expected =
                ThingModified.of(TestConstants.THING, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.empty();

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.THING.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void thingModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.empty();

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.THING.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final ThingModified thingModified =
                ThingModified.of(TestConstants.THING, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(thingModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void thingDeletedFromAdaptable() {
        final ThingDeleted expected =
                ThingDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.empty();

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void thingDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.empty();

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final ThingDeleted thingDeleted =
                ThingDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(thingDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclModifiedFromAdaptable() {
        final AclModified expected = AclModified.of(TestConstants.THING_ID, TestConstants.ACL, TestConstants.REVISION,
                setChannelHeader(TestConstants.DITTO_HEADERS_V_1));

        final JsonPointer path = JsonPointer.of("/acl");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ACL.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/acl");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ACL.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();

        final AclModified aclModified =
                AclModified.of(TestConstants.THING_ID, TestConstants.ACL, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_1_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(aclModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclEntryCreatedFromAdaptable() {
        final AclEntryCreated expected = AclEntryCreated.of(TestConstants.THING_ID, TestConstants.ACL_ENTRY,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_1));

        final JsonPointer path = JsonPointer.of("/acl/" + TestConstants.AUTHORIZATION_SUBJECT.getId());

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ACL_ENTRY.getPermissions().toJson())
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclEntryCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/acl/" + TestConstants.AUTHORIZATION_SUBJECT.getId());

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ACL_ENTRY.toJson())
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();

        final AclEntryCreated aclEntryCreated = AclEntryCreated.of(TestConstants.THING_ID, TestConstants.ACL_ENTRY,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_1_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(aclEntryCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclEntryModifiedFromAdaptable() {
        final AclEntryModified expected = AclEntryModified.of(TestConstants.THING_ID, TestConstants.ACL_ENTRY,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_1));

        final JsonPointer path = JsonPointer.of("/acl/" + TestConstants.AUTHORIZATION_SUBJECT.getId());

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ACL_ENTRY.getPermissions().toJson())
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclEntryModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/acl/" + TestConstants.AUTHORIZATION_SUBJECT.getId());

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ACL_ENTRY.toJson())
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();

        final AclEntryModified aclEntryModified = AclEntryModified.of(TestConstants.THING_ID, TestConstants.ACL_ENTRY,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_1_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(aclEntryModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclEntryDeletedFromAdaptable() {
        final AclEntryDeleted expected = AclEntryDeleted.of(TestConstants.THING_ID, TestConstants.AUTHORIZATION_SUBJECT,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_1));

        final JsonPointer path = JsonPointer.of("/acl/" + TestConstants.AUTHORIZATION_SUBJECT.getId());

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void aclEntryDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/acl/" + TestConstants.AUTHORIZATION_SUBJECT.getId());

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_1)
                .build();

        final AclEntryDeleted aclEntryDeleted = AclEntryDeleted.of(TestConstants.THING_ID,
                TestConstants.AUTHORIZATION_SUBJECT, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_1_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(aclEntryDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributesCreatedFromAdaptable() {
        final AttributesCreated expected = AttributesCreated.of(TestConstants.THING_ID, TestConstants.ATTRIBUTES,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/attributes");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributesCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/attributes");

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final AttributesCreated attributesCreated =
                AttributesCreated.of(TestConstants.THING_ID, TestConstants.ATTRIBUTES,
                        TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(attributesCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributesModifiedFromAdaptable() {
        final AttributesModified expected = AttributesModified.of(TestConstants.THING_ID, TestConstants.ATTRIBUTES,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/attributes");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributesModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/attributes");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final AttributesModified attributesModified = AttributesModified.of(TestConstants.THING_ID,
                TestConstants.ATTRIBUTES, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(attributesModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributesDeletedFromAdaptable() {
        final AttributesDeleted expected =
                AttributesDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/attributes");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributesDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/attributes");

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final AttributesDeleted attributesDeleted =
                AttributesDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(attributesDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributeCreatedFromAdaptable() {
        final AttributeCreated expected = AttributeCreated.of(TestConstants.THING_ID, TestConstants.ATTRIBUTE_POINTER,
                TestConstants.ATTRIBUTE_VALUE, TestConstants.REVISION,
                setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/attributes" + TestConstants.ATTRIBUTE_POINTER);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTE_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributeCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/attributes" + TestConstants.ATTRIBUTE_POINTER);

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTE_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final AttributeCreated attributeCreated =
                AttributeCreated.of(TestConstants.THING_ID, TestConstants.ATTRIBUTE_POINTER,
                        TestConstants.ATTRIBUTE_VALUE,
                        TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(attributeCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributeModifiedFromAdaptable() {
        final AttributeModified expected = AttributeModified.of(TestConstants.THING_ID, TestConstants.ATTRIBUTE_POINTER,
                TestConstants.ATTRIBUTE_VALUE, TestConstants.REVISION,
                setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/attributes" + TestConstants.ATTRIBUTE_POINTER);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTE_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributeModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/attributes" + TestConstants.ATTRIBUTE_POINTER);

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.ATTRIBUTE_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final AttributeModified attributeModified =
                AttributeModified.of(TestConstants.THING_ID, TestConstants.ATTRIBUTE_POINTER,
                        TestConstants.ATTRIBUTE_VALUE,
                        TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(attributeModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributeDeletedFromAdaptable() {
        final AttributeDeleted expected = AttributeDeleted.of(TestConstants.THING_ID, TestConstants.ATTRIBUTE_POINTER,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/attributes" + TestConstants.ATTRIBUTE_POINTER);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void attributeDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/attributes" + TestConstants.ATTRIBUTE_POINTER);

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final AttributeDeleted attributeDeleted = AttributeDeleted.of(TestConstants.THING_ID,
                TestConstants.ATTRIBUTE_POINTER, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(attributeDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void definitionCreatedFromAdaptable() {
        final ThingDefinitionCreated expected = ThingDefinitionCreated.of(TestConstants.THING_ID,
                TestConstants.THING_DEFINITION,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/definition");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.THING_DEFINITION))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void definitionCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/definition");

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.THING_DEFINITION))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final ThingDefinitionCreated thingDefintionCreated =
                ThingDefinitionCreated.of(TestConstants.THING_ID, TestConstants.THING_DEFINITION,
                        TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(thingDefintionCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void definitionModifiedFromAdaptable() {
        final ThingDefinitionModified expected = ThingDefinitionModified.of(TestConstants.THING_ID,
                TestConstants.THING_DEFINITION,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/definition");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.THING_DEFINITION))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void definitionModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/definition");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.THING_DEFINITION))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final ThingDefinitionModified definitionModified = ThingDefinitionModified.of(TestConstants.THING_ID,
                TestConstants.THING_DEFINITION, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(definitionModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void definitionDeletedFromAdaptable() {
        final ThingDefinitionDeleted expected =
                ThingDefinitionDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/definition");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void definitionDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/definition");

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final ThingDefinitionDeleted definitionDeleted =
                ThingDefinitionDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(definitionDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featuresCreatedFromAdaptable() {
        final FeaturesCreated expected = FeaturesCreated.of(TestConstants.THING_ID, TestConstants.FEATURES,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featuresCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features");

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturesCreated featuresCreated = FeaturesCreated.of(TestConstants.THING_ID, TestConstants.FEATURES,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featuresCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featuresModifiedFromAdaptable() {
        final FeaturesModified expected = FeaturesModified.of(TestConstants.THING_ID, TestConstants.FEATURES,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featuresModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURES.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturesModified featuresModified = FeaturesModified.of(TestConstants.THING_ID, TestConstants.FEATURES,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featuresModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featuresDeletedFromAdaptable() {
        final FeaturesDeleted expected =
                FeaturesDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featuresDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features");

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturesDeleted featuresDeleted =
                FeaturesDeleted.of(TestConstants.THING_ID, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featuresDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureCreatedFromAdaptable() {
        final FeatureCreated expected = FeatureCreated.of(TestConstants.THING_ID, TestConstants.FEATURE,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID);

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeatureCreated featureCreated = FeatureCreated.of(TestConstants.THING_ID, TestConstants.FEATURE,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featureCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureModifiedFromAdaptable() {
        final FeatureModified expected = FeatureModified.of(TestConstants.THING_ID, TestConstants.FEATURE,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID);

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE.toJson(FieldType.notHidden()))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeatureModified featureModified = FeatureModified.of(TestConstants.THING_ID, TestConstants.FEATURE,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featureModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDeletedFromAdaptable() {
        final FeatureDeleted expected = FeatureDeleted.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID);

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeatureDeleted featureDeleted = FeatureDeleted.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featureDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertiesCreatedFromAdaptable() {
        final FeaturePropertiesCreated expected =
                FeaturePropertiesCreated.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                        TestConstants.FEATURE_PROPERTIES, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/properties");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTIES_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertiesCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/properties");

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTIES_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturePropertiesCreated featurePropertiesCreated =
                FeaturePropertiesCreated.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                        TestConstants.FEATURE_PROPERTIES,
                        TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featurePropertiesCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertiesModifiedFromAdaptable() {
        final FeaturePropertiesModified expected =
                FeaturePropertiesModified.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                        TestConstants.FEATURE_PROPERTIES, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/properties");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTIES_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertiesModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/properties");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTIES_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturePropertiesModified featurePropertiesModified =
                FeaturePropertiesModified.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                        TestConstants.FEATURE_PROPERTIES, TestConstants.REVISION,
                        setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featurePropertiesModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertiesDeletedFromAdaptable() {
        final FeaturePropertiesDeleted expected = FeaturePropertiesDeleted.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/properties");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertiesDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/properties");

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturePropertiesDeleted featurePropertiesDeleted = FeaturePropertiesDeleted.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featurePropertiesDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertyCreatedFromAdaptable() {
        final FeaturePropertyCreated expected = FeaturePropertyCreated.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_PROPERTY_POINTER, TestConstants.FEATURE_PROPERTY_VALUE,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of(
                "/features/" + TestConstants.FEATURE_ID + "/properties" + TestConstants.FEATURE_PROPERTY_POINTER);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTY_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertyCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of(
                "/features/" + TestConstants.FEATURE_ID + "/properties" + TestConstants.FEATURE_PROPERTY_POINTER);

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTY_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturePropertyCreated featurePropertyCreated = FeaturePropertyCreated.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_PROPERTY_POINTER, TestConstants.FEATURE_PROPERTY_VALUE,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featurePropertyCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertyModifiedFromAdaptable() {
        final FeaturePropertyModified expected = FeaturePropertyModified.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_PROPERTY_POINTER, TestConstants.FEATURE_PROPERTY_VALUE,
                TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of(
                "/features/" + TestConstants.FEATURE_ID + "/properties" + TestConstants.FEATURE_PROPERTY_POINTER);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTY_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertyModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of(
                "/features/" + TestConstants.FEATURE_ID + "/properties" + TestConstants.FEATURE_PROPERTY_POINTER);

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_PROPERTY_VALUE)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturePropertyModified featurePropertyModified = FeaturePropertyModified.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_PROPERTY_POINTER, TestConstants.FEATURE_PROPERTY_VALUE,
                TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featurePropertyModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertyDeletedFromAdaptable() {
        final FeaturePropertyDeleted expected =
                FeaturePropertyDeleted.of(TestConstants.THING_ID, TestConstants.FEATURE_ID,
                        TestConstants.FEATURE_PROPERTY_POINTER, TestConstants.REVISION,
                        setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of(
                "/features/" + TestConstants.FEATURE_ID + "/properties" + TestConstants.FEATURE_PROPERTY_POINTER);

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featurePropertyDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of(
                "/features/" + TestConstants.FEATURE_ID + "/properties" + TestConstants.FEATURE_PROPERTY_POINTER);

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeaturePropertyDeleted featurePropertyDeleted = FeaturePropertyDeleted.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_PROPERTY_POINTER, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featurePropertyDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDefinitionCreatedFromAdaptable() {
        final FeatureDefinitionCreated expected = FeatureDefinitionCreated.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_DEFINITION, TestConstants.REVISION,
                setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/definition");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_DEFINITION_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDefinitionCreatedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/definition");

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_DEFINITION_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeatureDefinitionCreated featureDefinitionCreated = FeatureDefinitionCreated.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_DEFINITION, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featureDefinitionCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDefinitionModifiedFromAdaptable() {
        final FeatureDefinitionModified expected = FeatureDefinitionModified.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_DEFINITION, TestConstants.REVISION,
                setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/definition");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_DEFINITION_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDefinitionModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/definition");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_DEFINITION_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeatureDefinitionModified featureDefinitionModified = FeatureDefinitionModified.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.FEATURE_DEFINITION, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featureDefinitionModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDefinitionDeletedFromAdaptable() {
        final FeatureDefinitionDeleted expected = FeatureDefinitionDeleted.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/definition");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withValue(TestConstants.FEATURE_DEFINITION_JSON)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void featureDefinitionDeletedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/features/" + TestConstants.FEATURE_ID + "/definition");

        final Adaptable expected = Adaptable.newBuilder(topicPathDeleted())
                .withPayload(Payload.newBuilder(path)
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final FeatureDefinitionDeleted featureDefinitionDeleted = FeatureDefinitionDeleted.of(TestConstants.THING_ID,
                TestConstants.FEATURE_ID, TestConstants.REVISION,
                setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(featureDefinitionDeleted, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void policyIdCreatedFromAdaptable() {
        final PolicyIdCreated expected =
                PolicyIdCreated.of(TestConstants.THING_ID, TestConstants.Policies.POLICY_ID,
                        TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/policyId");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.Policies.POLICY_ID))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void policyIdCreatedToAdaptable() {
        final PolicyIdCreated policyIdCreated =
                PolicyIdCreated.of(TestConstants.THING_ID, TestConstants.Policies.POLICY_ID,
                        TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/policyId");

        final Adaptable expected = Adaptable.newBuilder(topicPathCreated())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.Policies.POLICY_ID))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final Adaptable actual = underTest.toAdaptable(policyIdCreated, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void policyIdModifiedFromAdaptable() {
        final PolicyIdModified expected =
                PolicyIdModified.of(TestConstants.THING_ID, TestConstants.Policies.POLICY_ID,
                        TestConstants.REVISION, setChannelHeader(TestConstants.DITTO_HEADERS_V_2));

        final JsonPointer path = JsonPointer.of("/policyId");

        final Adaptable adaptable = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.Policies.POLICY_ID))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingEvent<?> actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void policyIdModifiedToAdaptable() {
        final JsonPointer path = JsonPointer.of("/policyId");

        final Adaptable expected = Adaptable.newBuilder(topicPathModified())
                .withPayload(Payload.newBuilder(path)
                        .withValue(JsonValue.of(TestConstants.Policies.POLICY_ID))
                        .withRevision(TestConstants.REVISION)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final PolicyIdModified policyIdModified =
                PolicyIdModified.of(TestConstants.THING_ID, TestConstants.Policies.POLICY_ID,
                        TestConstants.REVISION, setChannelHeader(TestConstants.HEADERS_V_2_NO_CONTENT_TYPE));
        final Adaptable actual = underTest.toAdaptable(policyIdModified, channel);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    private static final class UnknownThingEvent implements ThingEvent<UnknownThingEvent> {

        @Override
        public ThingId getThingEntityId() {
            return TestConstants.THING_ID;
        }

        @Override
        public String getType() {
            return "things.events:aclDeleted";
        }

        @Override
        public long getRevision() {
            return TestConstants.REVISION;
        }

        @Override
        public UnknownThingEvent setRevision(final long revision) {
            return this;
        }

        @Override
        public Optional<Instant> getTimestamp() {
            return Optional.empty();
        }

        @Override
        public DittoHeaders getDittoHeaders() {
            return TestConstants.DITTO_HEADERS_V_1;
        }

        @Override
        public JsonObject toJson(final JsonSchemaVersion schemaVersion, final Predicate predicate) {
            return JsonObject.newBuilder()
                    .set(Event.JsonFields.TYPE, getType())
                    .set(Event.JsonFields.REVISION, getRevision())
                    .set(JsonFields.THING_ID, getThingEntityId().toString())
                    .build();
        }

        @Override
        public JsonPointer getResourcePath() {
            return JsonPointer.of("/acl");
        }

        @Override
        public UnknownThingEvent setDittoHeaders(final DittoHeaders dittoHeaders) {
            return this;
        }

        @Nonnull
        @Override
        public String getManifest() {
            return getType();
        }

    }

    private DittoHeaders setChannelHeader(final DittoHeaders dittoHeaders) {
        if (channel == LIVE) {
            return dittoHeaders.toBuilder().channel(LIVE.getName()).build();
        } else {
            return dittoHeaders;
        }
    }

    private EventsTopicPathBuilder topicPathBuilder() {
        final TopicPathBuilder topicPathBuilder = TopicPath.newBuilder(TestConstants.THING_ID)
                .things();

        if (channel == LIVE) {
            topicPathBuilder.live();
        } else {
            topicPathBuilder.twin();
        }

        return topicPathBuilder.events();
    }

    private TopicPath topicPathCreated() {
        return topicPathBuilder().created().build();
    }

    private TopicPath topicPathModified() {
        return topicPathBuilder().modified().build();
    }

    private TopicPath topicPathDeleted() {
        return topicPathBuilder().deleted().build();
    }
}
