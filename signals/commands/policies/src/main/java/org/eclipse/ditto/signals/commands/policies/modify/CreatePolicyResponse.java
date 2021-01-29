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
package org.eclipse.ditto.signals.commands.policies.modify;

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
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandResponse;

/**
 * Response to a {@link CreatePolicy} command.
 */
@Immutable
@JsonParsableCommandResponse(type = CreatePolicyResponse.TYPE)
public final class CreatePolicyResponse extends AbstractCommandResponse<CreatePolicyResponse>
        implements PolicyModifyCommandResponse<CreatePolicyResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + CreatePolicy.NAME;

    static final JsonFieldDefinition<JsonValue> JSON_POLICY =
            JsonFactory.newJsonValueFieldDefinition("policy", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyId policyId;
    @Nullable private final Policy policyCreated;

    private CreatePolicyResponse(final PolicyId policyId,
            final HttpStatus httpStatus,
            @Nullable final Policy policyCreated,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.policyId = checkNotNull(policyId, "Policy ID");
        this.policyCreated = policyCreated;
    }

    /**
     * Returns a new {@code CreatePolicyResponse} for a created Policy. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param policyId the Policy ID of the created Policy.
     * @param policy the created Policy.
     * @param dittoHeaders the headers of the PolicyCommand which caused the new response.
     * @return a command response for a created Policy.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static CreatePolicyResponse of(final PolicyId policyId, @Nullable final Policy policy,
            final DittoHeaders dittoHeaders) {

        return new CreatePolicyResponse(policyId, HttpStatus.CREATED, policy, dittoHeaders);
    }

    /**
     * Returns a new {@code CreatePolicyResponse} for a created Policy. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param policyId the Policy ID of the created Policy.
     * @param policy the created Policy.
     * @param dittoHeaders the headers of the PolicyCommand which caused the new response.
     * @return a command response for a created Policy.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Policy ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.policies.PolicyId, org.eclipse.ditto.model.policies.Policy, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static CreatePolicyResponse of(final String policyId, @Nullable final Policy policy,
            final DittoHeaders dittoHeaders) {

        return new CreatePolicyResponse(PolicyId.of(policyId), HttpStatus.CREATED, policy, dittoHeaders);
    }

    /**
     * Creates a response to a {@code CreatePolicy} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static CreatePolicyResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@code CreatePolicy} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static CreatePolicyResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<CreatePolicyResponse>(TYPE, jsonObject).deserialize(httpStatus -> {
            final String extractedPolicyId =
                    jsonObject.getValueOrThrow(PolicyCommandResponse.JsonFields.JSON_POLICY_ID);
            final PolicyId policyId = PolicyId.of(extractedPolicyId);
            final Policy extractedPolicyCreated = jsonObject.getValue(JSON_POLICY)
                    .map(JsonValue::asObject)
                    .map(PoliciesModelFactory::newPolicy)
                    .orElse(null);

            return new CreatePolicyResponse(policyId, httpStatus, extractedPolicyCreated, dittoHeaders);
        });
    }

    @Override
    public PolicyId getEntityId() {
        return policyId;
    }

    /**
     * Returns the created {@code Policy}.
     *
     * @return the created Policy.
     */
    public Optional<Policy> getPolicyCreated() {
        return Optional.ofNullable(policyCreated);
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.ofNullable(policyCreated).map(obj -> obj.toJson(schemaVersion, FieldType.notHidden()));
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.empty();
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(PolicyCommandResponse.JsonFields.JSON_POLICY_ID, String.valueOf(policyId), predicate);
        if (null != policyCreated) {
            jsonObjectBuilder.set(JSON_POLICY, policyCreated.toJson(schemaVersion, thePredicate), predicate);
        }
    }

    @Override
    public CreatePolicyResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(policyId, policyCreated, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof CreatePolicyResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CreatePolicyResponse that = (CreatePolicyResponse) o;
        return that.canEqual(this) &&
                Objects.equals(policyId, that.policyId) &&
                Objects.equals(policyCreated, that.policyCreated) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), policyId, policyCreated);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", policyId=" + policyId + ", policyCreated=" +
                policyCreated + "]";
    }

}
