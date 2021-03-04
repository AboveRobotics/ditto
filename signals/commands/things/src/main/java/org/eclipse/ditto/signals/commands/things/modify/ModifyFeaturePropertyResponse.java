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
package org.eclipse.ditto.signals.commands.things.modify;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

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
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link ModifyFeatureProperty} command.
 */
@Immutable
@JsonParsableCommandResponse(type = ModifyFeaturePropertyResponse.TYPE)
public final class ModifyFeaturePropertyResponse extends AbstractCommandResponse<ModifyFeaturePropertyResponse>
        implements ThingModifyCommandResponse<ModifyFeaturePropertyResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + ModifyFeatureProperty.NAME;

    static final JsonFieldDefinition<String> JSON_FEATURE_ID =
            JsonFactory.newStringFieldDefinition("featureId", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<String> JSON_PROPERTY =
            JsonFactory.newStringFieldDefinition("property", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<JsonValue> JSON_VALUE =
            JsonFactory.newJsonValueFieldDefinition("value", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final String featureId;
    private final JsonPointer featurePropertyPointer;
    @Nullable private final JsonValue featurePropertyValue;

    private ModifyFeaturePropertyResponse(final ThingId thingId,
            final String featureId,
            final JsonPointer featurePropertyPointer,
            @Nullable final JsonValue featurePropertyValue,
            final HttpStatus httpStatus,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "thingId");
        this.featureId = checkNotNull(featureId, "featrueId");
        this.featurePropertyPointer = checkPropertyPointer(featurePropertyPointer);
        this.featurePropertyValue = featurePropertyValue;
    }

    private static JsonPointer checkPropertyPointer(final JsonPointer propertyPointer) {
        checkNotNull(propertyPointer, "The FeatureProperty Pointer must not be null!");
        return ThingsModelFactory.validateFeaturePropertyPointer(propertyPointer);
    }

    /**
     * Returns a new {@code ModifyFeaturePropertyResponse} for a created FeatureProperty. This corresponds to the HTTP
     * status {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created feature property.
     * @param featureId the {@code Feature}'s ID whose Property was created.
     * @param featurePropertyPointer the pointer of the created FeatureProperty.
     * @param featureValue the created FeatureProperty value.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created FeatureProperty.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #created(org.eclipse.ditto.model.things.ThingId, String, org.eclipse.ditto.json.JsonPointer, org.eclipse.ditto.json.JsonValue, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static ModifyFeaturePropertyResponse created(final String thingId,
            final String featureId,
            final JsonPointer featurePropertyPointer,
            final JsonValue featureValue,
            final DittoHeaders dittoHeaders) {

        return created(ThingId.of(thingId), featureId, featurePropertyPointer, featureValue, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyFeaturePropertyResponse} for a created FeatureProperty. This corresponds to the HTTP
     * status {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created feature property.
     * @param featureId the {@code Feature}'s ID whose Property was created.
     * @param featurePropertyPointer the pointer of the created FeatureProperty.
     * @param featureValue the created FeatureProperty value.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created FeatureProperty.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyFeaturePropertyResponse created(final ThingId thingId,
            final String featureId,
            final JsonPointer featurePropertyPointer,
            final JsonValue featureValue,
            final DittoHeaders dittoHeaders) {

        return new ModifyFeaturePropertyResponse(thingId, featureId, featurePropertyPointer, featureValue,
                HttpStatus.CREATED, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyFeaturePropertyResponse} for a modified FeatureProperty. This corresponds to the HTTP
     * status {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified feature property.
     * @param featureId the {@code Feature}'s ID whose Property was modified.
     * @param featurePropertyPointer the pointer of the modified FeatureProperty.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified FeatureProperty.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #modified(org.eclipse.ditto.model.things.ThingId, String, org.eclipse.ditto.json.JsonPointer, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static ModifyFeaturePropertyResponse modified(final String thingId,
            final String featureId,
            final JsonPointer featurePropertyPointer,
            final DittoHeaders dittoHeaders) {

        return modified(ThingId.of(thingId), featureId, featurePropertyPointer, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyFeaturePropertyResponse} for a modified FeatureProperty. This corresponds to the HTTP
     * status {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified feature property.
     * @param featureId the {@code Feature}'s ID whose Property was modified.
     * @param featurePropertyPointer the pointer of the modified FeatureProperty.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified FeatureProperty.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyFeaturePropertyResponse modified(final ThingId thingId,
            final String featureId,
            final JsonPointer featurePropertyPointer,
            final DittoHeaders dittoHeaders) {

        return new ModifyFeaturePropertyResponse(thingId,
                featureId,
                featurePropertyPointer,
                null,
                HttpStatus.NO_CONTENT,
                dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyFeatureProperty} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of property pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static ModifyFeaturePropertyResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyFeatureProperty} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of property pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static ModifyFeaturePropertyResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<ModifyFeaturePropertyResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> new ModifyFeaturePropertyResponse(
                        ThingId.of(jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID)),
                        jsonObject.getValueOrThrow(JSON_FEATURE_ID),
                        JsonFactory.newPointer(jsonObject.getValueOrThrow(JSON_PROPERTY)),
                        jsonObject.getValue(JSON_VALUE).orElse(null),
                        httpStatus,
                        dittoHeaders));
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the pointer of the modified {@code FeatureProperty}.
     *
     * @return the the pointer of the modified FeatureProperty.
     */
    public JsonPointer getFeaturePropertyPointer() {
        return featurePropertyPointer;
    }

    /**
     * Returns the ID of the {@code Feature} whose properties were modified.
     *
     * @return the ID.
     */
    public String getFeatureId() {
        return featureId;
    }

    /**
     * Returns the created {@code FeatureProperty}.
     *
     * @return the created FeatureProperty.
     */
    public Optional<JsonValue> getFeaturePropertyValue() {
        return Optional.ofNullable(featurePropertyValue);
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.ofNullable(featurePropertyValue);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/features/" + featureId + "/properties" + featurePropertyPointer;
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        jsonObjectBuilder.set(JSON_FEATURE_ID, featureId, predicate);
        jsonObjectBuilder.set(JSON_PROPERTY, featurePropertyPointer.toString(), predicate);
        if (null != featurePropertyValue) {
            jsonObjectBuilder.set(JSON_VALUE, featurePropertyValue, predicate);
        }
    }

    @Override
    public ModifyFeaturePropertyResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return featurePropertyValue != null
                ? created(thingId, featureId, featurePropertyPointer, featurePropertyValue, dittoHeaders)
                : modified(thingId, featureId, featurePropertyPointer, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ModifyFeaturePropertyResponse;
    }

    @SuppressWarnings("squid:S1067")
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModifyFeaturePropertyResponse that = (ModifyFeaturePropertyResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(featureId, that.featureId) &&
                Objects.equals(featurePropertyPointer, that.featurePropertyPointer) &&
                Objects.equals(featurePropertyValue, that.featurePropertyValue) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, featureId, featurePropertyPointer, featurePropertyValue);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId
                + ", featureId=" + featureId + ", featurePropertyPointer=" + featurePropertyPointer +
                ", featurePropertyValue=" + featurePropertyValue + "]";
    }

}
