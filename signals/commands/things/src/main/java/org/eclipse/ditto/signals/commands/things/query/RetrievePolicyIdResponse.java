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
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link RetrievePolicyId} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrievePolicyIdResponse.TYPE)
public final class RetrievePolicyIdResponse extends AbstractCommandResponse<RetrievePolicyIdResponse>
        implements ThingQueryCommandResponse<RetrievePolicyIdResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrievePolicyId.NAME;

    static final JsonFieldDefinition<String> JSON_POLICY_ID =
            JsonFactory.newStringFieldDefinition("policyId", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final PolicyId policyId;

    private RetrievePolicyIdResponse(final ThingId thingId,
            final HttpStatus httpStatus,
            final PolicyId policyId,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "thing ID");
        this.policyId = checkNotNull(policyId, "Policy ID");
    }

    /**
     * Creates a response to a {@link RetrievePolicyId} command.
     *
     * @param thingId the Thing ID of the retrieved Policy ID.
     * @param policyId the retrieved Policy ID.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #of(ThingId, PolicyId, DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrievePolicyIdResponse of(final String thingId, final String policyId,
            final DittoHeaders dittoHeaders) {

        return of(ThingId.of(thingId), PolicyId.of(policyId), dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrievePolicyId} command.
     *
     * @param thingId the Thing ID of the retrieved Policy ID.
     * @param policyId the retrieved Policy ID.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrievePolicyIdResponse of(final ThingId thingId, final PolicyId policyId,
            final DittoHeaders dittoHeaders) {

        return new RetrievePolicyIdResponse(thingId, HttpStatus.OK, policyId, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrievePolicyId} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrievePolicyIdResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrievePolicyId} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrievePolicyIdResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<RetrievePolicyIdResponse>(TYPE, jsonObject)
                .deserialize(httpStatus -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final String readPolicyId = jsonObject.getValueOrThrow(JSON_POLICY_ID);
                    final PolicyId policyId = PolicyId.of(readPolicyId);

                    return of(thingId, policyId, dittoHeaders);
                });
    }

    /**
     * RetrievePolicyIdResponse is only available in JsonSchemaVersion V_2.
     *
     * @return the supported JsonSchemaVersions.
     */
    @Override
    public JsonSchemaVersion[] getSupportedSchemaVersions() {
        return new JsonSchemaVersion[]{JsonSchemaVersion.V_2};
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the retrieved Policy ID.
     *
     * @return the retrieved Policy ID.
     * @deprecated Policy ID of the Thing is now typed. Use {@link #getPolicyEntityId()} instead.
     */
    @Deprecated
    public String getPolicyId() {
        return String.valueOf(getPolicyEntityId());
    }

    /**
     * Returns the retrieved Policy ID.
     *
     * @return the retrieved Policy ID.
     */
    public PolicyId getPolicyEntityId() {
        return policyId;
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return JsonValue.of(policyId);
    }

    @Override
    public RetrievePolicyIdResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(thingId, PolicyId.of(entity.asString()), getDittoHeaders());
    }

    @Override
    public RetrievePolicyIdResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(thingId, policyId, dittoHeaders);
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/policyId");
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, String.valueOf(thingId), predicate);
        jsonObjectBuilder.set(JSON_POLICY_ID, String.valueOf(policyId), predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrievePolicyIdResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetrievePolicyIdResponse that = (RetrievePolicyIdResponse) o;
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
