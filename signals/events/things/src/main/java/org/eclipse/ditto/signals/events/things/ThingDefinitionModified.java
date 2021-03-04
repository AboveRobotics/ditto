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
package org.eclipse.ditto.signals.events.things;

import static org.eclipse.ditto.signals.events.things.ThingEvent.TYPE_PREFIX;

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
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingDefinition;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.events.base.EventJsonDeserializer;


/**
 * This event is emitted after a Thing's {@code definition} was modified.
 */
@Immutable
@JsonParsableEvent(name = ThingDefinitionModified.NAME, typePrefix = TYPE_PREFIX)
public final class ThingDefinitionModified extends AbstractThingEvent<ThingDefinitionModified>
        implements ThingModifiedEvent<ThingDefinitionModified> {


    /**
     * Name of the "Thing Definition Modified" event.
     */
    public static final String NAME = "definitionModified";

    /**
     * Type of this event.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    static final JsonFieldDefinition<JsonValue> JSON_DEFINITION =
            JsonFactory.newJsonValueFieldDefinition("definition", FieldType.REGULAR, JsonSchemaVersion.V_2);


    private final ThingDefinition definition;

    private ThingDefinitionModified(final ThingId thingId,
            final ThingDefinition definition,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders,
            @Nullable final Metadata metadata) {

        super(TYPE, thingId, revision, timestamp, dittoHeaders, metadata);
        this.definition = definition;
    }

    /**
     * Constructs a new {@code ThingDefinitionModified} object.
     *
     * @param thingId the ID of the Thing whose Definition was modified.
     * @param definition the modified {@link ThingDefinition}.
     * @param revision the revision of the Thing.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the ThingDefinitionModified created.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Use {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.model.things.ThingDefinition, long, java.time.Instant, org.eclipse.ditto.model.base.headers.DittoHeaders, org.eclipse.ditto.model.base.entity.metadata.Metadata)}
     * instead.
     */
    @Deprecated
    public static ThingDefinitionModified of(final ThingId thingId,
            final ThingDefinition definition,
            final long revision,
            final DittoHeaders dittoHeaders) {

        return of(thingId, definition, revision, null, dittoHeaders, null);
    }

    /**
     * Constructs a new {@code ThingDefinitionModified} object.
     *
     * @param thingId the ID of the Thing whose Definition was modified.
     * @param definition the modified {@link ThingDefinition}.
     * @param revision the revision of the Thing.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the ThingDefinitionModified created.
     * @throws NullPointerException if any argument but {@code timestamp} is {@code null}.
     * @deprecated Use {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.model.things.ThingDefinition, long, java.time.Instant, org.eclipse.ditto.model.base.headers.DittoHeaders, org.eclipse.ditto.model.base.entity.metadata.Metadata)}
     * instead.
     */
    @Deprecated
    public static ThingDefinitionModified of(final ThingId thingId,
            final ThingDefinition definition,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        return of(thingId, definition, revision, timestamp, dittoHeaders, null);
    }

    /**
     * Constructs a new {@code ThingDefinitionModified} object.
     *
     * @param thingId the ID of the Thing whose Definition was modified.
     * @param definition the modified {@link ThingDefinition}.
     * @param revision the revision of the Thing.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @param metadata the metadata to apply for the event.
     * @return the ThingDefinitionModified created.
     * @throws NullPointerException if any argument but {@code timestamp} and {@code metadata} is {@code null}.
     * @since 1.3.0
     */
    public static ThingDefinitionModified of(final ThingId thingId,
            final ThingDefinition definition,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders,
            @Nullable final Metadata metadata) {

        return new ThingDefinitionModified(thingId, definition, revision, timestamp, dittoHeaders, metadata);
    }

    /**
     * Creates a new {@code ThingDefinitionModified} from a JSON string.
     *
     * @param jsonString the JSON string of which a new ThingDefinitionModified instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code ThingDefinitionModified} which was created from the given JSON string.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * 'ThingDefinitionModified' format.
     */
    public static ThingDefinitionModified fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code ThingDefinitionModified} from a JSON object.
     *
     * @param jsonObject the JSON object from which a new ThingDefinitionModified instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code ThingDefinitionModified} which was created from the given JSON object.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * 'ThingDefinitionModified' format.
     */
    public static ThingDefinitionModified fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new EventJsonDeserializer<ThingDefinitionModified>(TYPE, jsonObject)
                .deserialize((revision, timestamp, metadata) -> {
                    final String extractedThingId = jsonObject.getValueOrThrow(ThingEvent.JsonFields.THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final JsonValue extractedDefinition = jsonObject.getValueOrThrow(JSON_DEFINITION);
                    final ThingDefinition definition;
                    if (extractedDefinition.isNull()) {
                        definition = ThingsModelFactory.nullDefinition();
                    } else {
                        definition = ThingsModelFactory.newDefinition(extractedDefinition.asString());
                    }

                    return of(thingId, definition, revision, timestamp, dittoHeaders, metadata);
                });
    }

    /**
     * Returns the modified {@link ThingDefinition}.
     *
     * @return the modified Definition.
     */
    public ThingDefinition getThingDefinition() {
        return definition;
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        if (schemaVersion == JsonSchemaVersion.V_2) {
            return Optional.of(JsonValue.of(definition));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = Thing.JsonFields.DEFINITION.getPointer().toString();
        return JsonPointer.of(path);
    }

    @Override
    public ThingDefinitionModified setRevision(final long revision) {
        return of(getThingEntityId(), definition, revision, getTimestamp().orElse(null), getDittoHeaders(),
                getMetadata().orElse(null));
    }

    @Override
    public ThingDefinitionModified setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(getThingEntityId(), definition, getRevision(), getTimestamp().orElse(null), dittoHeaders,
                getMetadata().orElse(null));
    }

    @Override
    protected void appendPayloadAndBuild(final JsonObjectBuilder jsonObjectBuilder,
            final JsonSchemaVersion schemaVersion, final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        if (definition.equals(ThingsModelFactory.nullDefinition())) {
            jsonObjectBuilder.set(JSON_DEFINITION, JsonValue.nullLiteral(), predicate);
        } else {
            jsonObjectBuilder.set(JSON_DEFINITION, JsonValue.of(String.valueOf(definition)), predicate);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), definition);
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
        final ThingDefinitionModified that = (ThingDefinitionModified) o;
        return that.canEqual(this) &&
                Objects.equals(definition, that.definition) &&
                super.equals(that);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ThingDefinitionModified;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", definition=" + definition +
                "]";
    }

}
