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
package org.eclipse.ditto.signals.events.things;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableEvent;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.events.base.EventJsonDeserializer;

/**
 * This event is emitted after an {@code Attribute} was modified.
 */
@Immutable
@JsonParsableEvent(name = AttributeModified.NAME, typePrefix = AttributeModified.TYPE_PREFIX)
public final class AttributeModified extends AbstractThingEvent<AttributeModified>
        implements ThingModifiedEvent<AttributeModified> {

    /**
     * Name of the "Thing Attribute Modified" event.
     */
    public static final String NAME = "attributeModified";

    /**
     * Type of this event.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    static final JsonFieldDefinition<String> JSON_ATTRIBUTE =
            JsonFactory.newStringFieldDefinition("attribute", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<JsonValue> JSON_VALUE =
            JsonFactory.newJsonValueFieldDefinition("value", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    private final JsonPointer attributePointer;
    private final JsonValue attributeValue;

    private AttributeModified(final ThingId thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders,
            @Nullable final Metadata metadata) {

        super(TYPE, thingId, revision, timestamp, dittoHeaders, metadata);

        this.attributePointer = Objects.requireNonNull(attributePointer, "The attribute key must not be null!");
        this.attributeValue = Objects.requireNonNull(attributeValue, "The attribute value must not be null!");
    }

    /**
     * Constructs a new {@code AttributeModified} object.
     *
     * @param thingId the ID of the Thing with which this event is associated.
     * @param attributePointer the pointer of the attribute with which this event is associated.
     * @param attributeValue the value of the attribute with which this event is associated.
     * @param revision the revision of the Thing.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the AttributeModified created.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.json.JsonPointer, org.eclipse.ditto.json.JsonValue, long, java.time.Instant, org.eclipse.ditto.model.base.headers.DittoHeaders, org.eclipse.ditto.model.base.entity.metadata.Metadata)}
     * instead.
     */
    @Deprecated
    public static AttributeModified of(final String thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final long revision,
            final DittoHeaders dittoHeaders) {

        return of(ThingId.of(thingId), attributePointer, attributeValue, revision, null, dittoHeaders, null);
    }

    /**
     * Constructs a new {@code AttributeModified} object.
     *
     * @param thingId the ID of the Thing with which this event is associated.
     * @param attributePointer the pointer of the attribute with which this event is associated.
     * @param attributeValue the value of the attribute with which this event is associated.
     * @param revision the revision of the Thing.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the AttributeModified created.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Use {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.json.JsonPointer, org.eclipse.ditto.json.JsonValue, long, java.time.Instant, org.eclipse.ditto.model.base.headers.DittoHeaders, org.eclipse.ditto.model.base.entity.metadata.Metadata)}
     * instead.
     */
    @Deprecated
    public static AttributeModified of(final ThingId thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final long revision,
            final DittoHeaders dittoHeaders) {

        return of(thingId, attributePointer, attributeValue, revision, null, dittoHeaders, null);
    }

    /**
     * Constructs a new {@code AttributeModified} object.
     *
     * @param thingId the ID of the Thing with which this event is associated.
     * @param attributePointer the pointer of the attribute with which this event is associated.
     * @param attributeValue the value of the attribute with which this event is associated.
     * @param revision the revision of the Thing.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the AttributeModified created.
     * @throws NullPointerException if any argument but {@code timestamp} is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.json.JsonPointer, org.eclipse.ditto.json.JsonValue, long, java.time.Instant, org.eclipse.ditto.model.base.headers.DittoHeaders, org.eclipse.ditto.model.base.entity.metadata.Metadata)}
     * instead.
     */
    @Deprecated
    public static AttributeModified of(final String thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        return of(ThingId.of(thingId), attributePointer, attributeValue, revision, timestamp, dittoHeaders, null);
    }

    /**
     * Constructs a new {@code AttributeModified} object.
     *
     * @param thingId the ID of the Thing with which this event is associated.
     * @param attributePointer the pointer of the attribute with which this event is associated.
     * @param attributeValue the value of the attribute with which this event is associated.
     * @param revision the revision of the Thing.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the AttributeModified created.
     * @throws NullPointerException if any argument but {@code timestamp} is {@code null}.
     * @deprecated Use {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.json.JsonPointer, org.eclipse.ditto.json.JsonValue, long, java.time.Instant, org.eclipse.ditto.model.base.headers.DittoHeaders, org.eclipse.ditto.model.base.entity.metadata.Metadata)}
     * instead.
     */
    @Deprecated
    public static AttributeModified of(final ThingId thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        return of(thingId, attributePointer, attributeValue, revision, timestamp, dittoHeaders, null);
    }

    /**
     * Constructs a new {@code AttributeModified} object.
     *
     * @param thingId the ID of the Thing with which this event is associated.
     * @param attributePointer the pointer of the attribute with which this event is associated.
     * @param attributeValue the value of the attribute with which this event is associated.
     * @param revision the revision of the Thing.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @param metadata the metadata to apply for the event.
     * @return the AttributeModified created.
     * @throws NullPointerException if any argument but {@code timestamp} and {@code metadata} is {@code null}.
     * @since 1.3.0
     */
    public static AttributeModified of(final ThingId thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders,
            @Nullable final Metadata metadata) {

        return new AttributeModified(thingId, attributePointer, attributeValue, revision, timestamp, dittoHeaders,
                metadata);
    }

    /**
     * Creates a new {@code AttributeModified} from a JSON string.
     *
     * @param jsonString the JSON string of which a new AttributeModified instance is to be deleted.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code AttributeModified} which was deleted from the given JSON string.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * 'AttributeModified' format.
     */
    public static AttributeModified fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code AttributeModified} from a JSON object.
     *
     * @param jsonObject the JSON object from which a new AttributeModified instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code AttributeModified} which was created from the given JSON object.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * 'AttributeModified' format.
     */
    public static AttributeModified fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new EventJsonDeserializer<AttributeModified>(TYPE, jsonObject).deserialize(
                (revision, timestamp, metadata) -> {
                    final String extractedThingId = jsonObject.getValueOrThrow(JsonFields.THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final String pointerString = jsonObject.getValueOrThrow(JSON_ATTRIBUTE);
                    final JsonPointer extractedAttributePointer = JsonFactory.newPointer(pointerString);
                    final JsonValue extractedValue = jsonObject.getValueOrThrow(JSON_VALUE);

                    return of(thingId, extractedAttributePointer, extractedValue, revision, timestamp, dittoHeaders,
                            metadata);
                });
    }

    /**
     * Returns the key of the attribute modified.
     *
     * @return the key of the attribute modified.
     */
    public JsonPointer getAttributePointer() {
        return attributePointer;
    }

    /**
     * Returns the value of the attribute modified.
     *
     * @return the value of the attribute modified.
     */
    public JsonValue getAttributeValue() {
        return attributeValue;
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.of(attributeValue);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/attributes" + attributePointer;
        return JsonPointer.of(path);
    }

    @Override
    public AttributeModified setRevision(final long revision) {
        return of(getThingEntityId(), attributePointer, attributeValue, revision, getTimestamp().orElse(null),
                getDittoHeaders(), getMetadata().orElse(null));
    }

    @Override
    public AttributeModified setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(getThingEntityId(), attributePointer, attributeValue, getRevision(), getTimestamp().orElse(null),
                dittoHeaders, getMetadata().orElse(null));
    }

    @Override
    protected void appendPayloadAndBuild(final JsonObjectBuilder jsonObjectBuilder,
            final JsonSchemaVersion schemaVersion, final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(JSON_ATTRIBUTE, attributePointer.toString(), predicate);
        jsonObjectBuilder.set(JSON_VALUE, attributeValue, predicate);
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(getThingEntityId());
        result = prime * result + Objects.hashCode(attributePointer);
        result = prime * result + Objects.hashCode(attributeValue);
        return result;
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "OverlyComplexMethod"})
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AttributeModified that = (AttributeModified) o;
        return that.canEqual(this) && Objects.equals(getThingEntityId(), that.getThingEntityId())
                && Objects.equals(attributePointer, that.attributePointer)
                && Objects.equals(attributeValue, that.attributeValue) && super.equals(that);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof AttributeModified;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", attributePointer=" + attributePointer
                + ", attributeValue=" + attributeValue + "]";
    }

}
