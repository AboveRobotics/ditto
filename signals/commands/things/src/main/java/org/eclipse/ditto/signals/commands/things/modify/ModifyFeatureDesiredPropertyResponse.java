/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
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
 * Response to a {@link ModifyFeatureDesiredProperty} command.
 *
 * @since 1.5.0
 */
@Immutable
@JsonParsableCommandResponse(type = ModifyFeatureDesiredPropertyResponse.TYPE)
public final class ModifyFeatureDesiredPropertyResponse
        extends AbstractCommandResponse<ModifyFeatureDesiredPropertyResponse>
        implements ThingModifyCommandResponse<ModifyFeatureDesiredPropertyResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + ModifyFeatureDesiredProperty.NAME;

    static final JsonFieldDefinition<String> JSON_FEATURE_ID =
            JsonFactory.newStringFieldDefinition("featureId", FieldType.REGULAR, JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<String> JSON_DESIRED_PROPERTY =
            JsonFactory.newStringFieldDefinition("desiredProperty", FieldType.REGULAR, JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<JsonValue> JSON_DESIRED_VALUE =
            JsonFactory.newJsonValueFieldDefinition("desiredValue", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final String featureId;
    private final JsonPointer desiredPropertyPointer;
    @Nullable private final JsonValue desiredPropertyValue;

    private ModifyFeatureDesiredPropertyResponse(final ThingId thingId,
            final CharSequence featureId,
            final JsonPointer desiredPropertyPointer,
            @Nullable final JsonValue desiredPropertyValue,
            final HttpStatus httpStatus,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "thingId");
        this.featureId = argumentNotEmpty(featureId, "featureId").toString();
        this.desiredPropertyPointer = checkDesiredPropertyPointer(desiredPropertyPointer);
        this.desiredPropertyValue = desiredPropertyValue;
    }

    private static JsonPointer checkDesiredPropertyPointer(final JsonPointer desiredPropertyPointer) {
        checkNotNull(desiredPropertyPointer, "desiredPropertyPointer");
        return ThingsModelFactory.validateFeaturePropertyPointer(desiredPropertyPointer);
    }

    /**
     * Returns a new {@code ModifyFeatureDesiredPropertyResponse} for a created desired property. This corresponds to the HTTP
     * status {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created desired property.
     * @param featureId the {@code Feature}'s ID whose desired property was created.
     * @param desiredPropertyPointer the pointer of the created desired property.
     * @param desiredValue the created desired property's value.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created desired property.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyFeatureDesiredPropertyResponse created(final ThingId thingId,
            final CharSequence featureId,
            final JsonPointer desiredPropertyPointer,
            final JsonValue desiredValue,
            final DittoHeaders dittoHeaders) {

        return new ModifyFeatureDesiredPropertyResponse(thingId, featureId, desiredPropertyPointer, desiredValue,
                HttpStatus.CREATED, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyFeatureDesiredPropertyResponse} for a modified desired property. This corresponds to the HTTP
     * status {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified desired property.
     * @param featureId the {@code Feature}'s ID whose desired property was modified.
     * @param desiredPropertyPointer the pointer of the modified desired property.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified desired property.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyFeatureDesiredPropertyResponse modified(final ThingId thingId,
            final CharSequence featureId,
            final JsonPointer desiredPropertyPointer,
            final DittoHeaders dittoHeaders) {

        return new ModifyFeatureDesiredPropertyResponse(thingId, featureId, desiredPropertyPointer, null,
                HttpStatus.NO_CONTENT, dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyFeatureDesiredProperty} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of the desired property pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static ModifyFeatureDesiredPropertyResponse fromJson(final String jsonString,
            final DittoHeaders dittoHeaders) {

        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyFeatureDesiredProperty} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of the desired property pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static ModifyFeatureDesiredPropertyResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<ModifyFeatureDesiredPropertyResponse>(TYPE, jsonObject)
                .deserialize(httpStatus -> new ModifyFeatureDesiredPropertyResponse(
                                ThingId.of(jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID)),
                                jsonObject.getValueOrThrow(JSON_FEATURE_ID),
                                JsonFactory.newPointer(jsonObject.getValueOrThrow(JSON_DESIRED_PROPERTY)),
                                jsonObject.getValue(JSON_DESIRED_VALUE).orElse(null),
                                httpStatus,
                                dittoHeaders
                        )
                );
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the pointer of the modified desired property.
     *
     * @return the the pointer of the modified desired property.
     */
    public JsonPointer getDesiredPropertyPointer() {
        return desiredPropertyPointer;
    }

    /**
     * Returns the ID of the {@code Feature} whose desired properties were modified.
     *
     * @return the ID.
     */
    public String getFeatureId() {
        return featureId;
    }

    /**
     * Returns the created desired property.
     *
     * @return the created desired property.
     */
    public Optional<JsonValue> getDesiredPropertyValue() {
        return Optional.ofNullable(desiredPropertyValue);
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.ofNullable(desiredPropertyValue);
    }

    /**
     * ModifyFeatureDesiredPropertyResponse is only available in JsonSchemaVersion V_2.
     *
     * @return the supported JsonSchemaVersions.
     */
    @Override
    public JsonSchemaVersion[] getSupportedSchemaVersions() {
        return new JsonSchemaVersion[]{JsonSchemaVersion.V_2};
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/features/" + featureId + "/desiredProperties" + desiredPropertyPointer;
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        jsonObjectBuilder.set(JSON_FEATURE_ID, featureId, predicate);
        jsonObjectBuilder.set(JSON_DESIRED_PROPERTY, desiredPropertyPointer.toString(), predicate);
        if (null != desiredPropertyValue) {
            jsonObjectBuilder.set(JSON_DESIRED_VALUE, desiredPropertyValue, predicate);
        }
    }

    @Override
    public ModifyFeatureDesiredPropertyResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return desiredPropertyValue != null
                ? created(thingId, featureId, desiredPropertyPointer, desiredPropertyValue, dittoHeaders)
                : modified(thingId, featureId, desiredPropertyPointer, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ModifyFeatureDesiredPropertyResponse;
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
        final ModifyFeatureDesiredPropertyResponse that = (ModifyFeatureDesiredPropertyResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(featureId, that.featureId) &&
                Objects.equals(desiredPropertyPointer, that.desiredPropertyPointer) &&
                Objects.equals(desiredPropertyValue, that.desiredPropertyValue) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, featureId, desiredPropertyPointer, desiredPropertyValue);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId
                + ", featureId=" + featureId
                + ", desiredPropertyPointer=" + desiredPropertyPointer
                + ", desiredPropertyValue=" + desiredPropertyValue + "]";
    }

}
