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
package org.eclipse.ditto.signals.commands.things.query;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;
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
import org.eclipse.ditto.model.things.AttributesModelFactory;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link RetrieveAttribute} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrieveAttributeResponse.TYPE)
public final class RetrieveAttributeResponse extends AbstractCommandResponse<RetrieveAttributeResponse>
        implements ThingQueryCommandResponse<RetrieveAttributeResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrieveAttribute.NAME;

    static final JsonFieldDefinition<String> JSON_ATTRIBUTE =
            JsonFactory.newStringFieldDefinition("attribute", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<JsonValue> JSON_VALUE =
            JsonFactory.newJsonValueFieldDefinition("value", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final JsonPointer attributePointer;
    private final JsonValue attributeValue;

    private RetrieveAttributeResponse(final ThingId thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final HttpStatus httpStatus,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "thing ID");
        this.attributePointer = checkAttributePointer(attributePointer);
        this.attributeValue = checkNotNull(attributeValue, "Attribute Value");
    }

    private static JsonPointer checkAttributePointer(final JsonPointer attributePointer) {
        checkNotNull(attributePointer, "The JSON pointer which attribute to retrieve must not be null!");
        return AttributesModelFactory.validateAttributePointer(attributePointer);
    }

    /**
     * Creates a response to a {@link RetrieveAttribute} command.
     *
     * @param thingId the Thing ID of the retrieved attribute.
     * @param attributePointer the JSON pointer of the attribute to retrieve.
     * @param attributeValue the retrieved Attribute value.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of {@code attributePointer} are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     * @deprecated Thing ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.json.JsonPointer,
     * org.eclipse.ditto.json.JsonValue, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrieveAttributeResponse of(final String thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final DittoHeaders dittoHeaders) {

        return of(ThingId.of(thingId), attributePointer, attributeValue, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAttribute} command.
     *
     * @param thingId the Thing ID of the retrieved attribute.
     * @param attributePointer the JSON pointer of the attribute to retrieve.
     * @param attributeValue the retrieved Attribute value.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of {@code attributePointer} are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static RetrieveAttributeResponse of(final ThingId thingId,
            final JsonPointer attributePointer,
            final JsonValue attributeValue,
            final DittoHeaders dittoHeaders) {

        return new RetrieveAttributeResponse(thingId, attributePointer, attributeValue, HttpStatus.OK, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAttribute} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of attribute pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static RetrieveAttributeResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAttribute} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of attribute pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static RetrieveAttributeResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<RetrieveAttributeResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final String extractedPointerString = jsonObject.getValueOrThrow(JSON_ATTRIBUTE);
                    final JsonPointer extractedPointer = JsonFactory.newPointer(extractedPointerString);
                    final JsonValue extractedAttribute = jsonObject.getValueOrThrow(JSON_VALUE);

                    return of(thingId, extractedPointer, extractedAttribute, dittoHeaders);
                });
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the retrieved Attribute.
     *
     * @return the retrieved Attribute.
     */
    public JsonValue getAttributeValue() {
        return attributeValue;
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return attributeValue;
    }

    @Override
    public RetrieveAttributeResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(thingId, attributePointer, entity, getDittoHeaders());
    }

    @Override
    public RetrieveAttributeResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(thingId, attributePointer, attributeValue, dittoHeaders);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/attributes" + attributePointer;
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        jsonObjectBuilder.set(JSON_ATTRIBUTE, attributePointer.toString(), predicate);
        jsonObjectBuilder.set(JSON_VALUE, attributeValue, predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrieveAttributeResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetrieveAttributeResponse that = (RetrieveAttributeResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(attributePointer, that.attributePointer) &&
                Objects.equals(attributeValue, that.attributeValue) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, attributePointer, attributeValue);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId
                + ", attributePointer=" + attributePointer + ", attributeValue=" + attributeValue + "]";
    }

}
