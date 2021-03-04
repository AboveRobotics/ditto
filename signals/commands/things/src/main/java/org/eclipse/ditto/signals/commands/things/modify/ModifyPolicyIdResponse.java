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
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link ModifyPolicyId} command.
 */
@Immutable
@JsonParsableCommandResponse(type = ModifyPolicyIdResponse.TYPE)
public final class ModifyPolicyIdResponse extends AbstractCommandResponse<ModifyPolicyIdResponse>
        implements ThingModifyCommandResponse<ModifyPolicyIdResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + ModifyPolicyId.NAME;

    static final JsonFieldDefinition<String> JSON_POLICY_ID =
            JsonFactory.newStringFieldDefinition("policyId", FieldType.REGULAR, JsonSchemaVersion.V_2);


    private final ThingId thingId;
    @Nullable private final PolicyId policyId;

    private ModifyPolicyIdResponse(final ThingId thingId,
            final HttpStatus httpStatus,
            @Nullable final PolicyId policyId,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "Thing ID");
        this.policyId = policyId;
    }

    /**
     * ModifyPolicyIdResponse is only available in JsonSchemaVersion V_2.
     *
     * @return the supported JsonSchemaVersions.
     */
    @Override
    public JsonSchemaVersion[] getSupportedSchemaVersions() {
        return new JsonSchemaVersion[]{JsonSchemaVersion.V_2};
    }

    /**
     * Returns a new {@code ModifyPolicyIdResponse} for a created Policy ID. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created policy ID.
     * @param policyId the created Policy ID.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created Policy ID.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #created(ThingId, PolicyId, DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static ModifyPolicyIdResponse created(final String thingId, final String policyId,
            final DittoHeaders dittoHeaders) {

        return created(ThingId.of(thingId), PolicyId.of(policyId), dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyPolicyIdResponse} for a created Policy ID. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created policy ID.
     * @param policyId the created Policy ID.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created Policy ID.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyPolicyIdResponse created(final ThingId thingId, final PolicyId policyId,
            final DittoHeaders dittoHeaders) {

        return new ModifyPolicyIdResponse(thingId, HttpStatus.CREATED, policyId, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyPolicyIdResponse} for a modified Policy ID. This corresponds to the HTTP status
     * {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified policy ID.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified Policy ID.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #modified(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static ModifyPolicyIdResponse modified(final String thingId, final DittoHeaders dittoHeaders) {
        return modified(ThingId.of(thingId), dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyPolicyIdResponse} for a modified Policy ID. This corresponds to the HTTP status
     * {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified policy ID.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified Policy ID.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyPolicyIdResponse modified(final ThingId thingId, final DittoHeaders dittoHeaders) {
        return new ModifyPolicyIdResponse(thingId, HttpStatus.NO_CONTENT, null, dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyPolicyId} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static ModifyPolicyIdResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyPolicyId} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static ModifyPolicyIdResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<ModifyPolicyIdResponse>(TYPE, jsonObject).deserialize(httpStatus -> {
            final String extractedThingId = jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
            final ThingId thingId = ThingId.of(extractedThingId);
            final String extractedPolicyId = jsonObject.getValue(JSON_POLICY_ID).orElse(null);
            final PolicyId thingPolicyId = extractedPolicyId == null ? null : PolicyId.of(extractedPolicyId);

            return new ModifyPolicyIdResponse(thingId, httpStatus, thingPolicyId, dittoHeaders);
        });
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the created Policy ID.
     *
     * @return the created Policy ID.
     * @deprecated Policy ID of Thing is now typed. Use {@link #getPolicyEntityId()} instead.
     */
    @Deprecated
    public Optional<String> getPolicyId() {
        return getPolicyEntityId().map(String::valueOf);
    }

    /**
     * Returns the created Policy ID.
     *
     * @return the created Policy ID.
     */
    public Optional<PolicyId> getPolicyEntityId() {
        return Optional.ofNullable(policyId);
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.ofNullable(policyId).map(JsonValue::of);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = Thing.JsonFields.POLICY_ID.getPointer().toString();
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        if (policyId != null) {
            jsonObjectBuilder.set(JSON_POLICY_ID, String.valueOf(policyId), predicate);
        }
    }

    @Override
    public ModifyPolicyIdResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return policyId != null ? created(thingId, policyId, dittoHeaders) : modified(thingId, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ModifyPolicyIdResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModifyPolicyIdResponse that = (ModifyPolicyIdResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(policyId, that.policyId) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, policyId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId + ", policyId=" +
                policyId + "]";
    }

}
