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
package org.eclipse.ditto.signals.commands.policies.query;

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
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandResponse;

/**
 * Response to a {@link RetrievePolicyEntries} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrievePolicyEntriesResponse.TYPE)
public final class RetrievePolicyEntriesResponse extends AbstractCommandResponse<RetrievePolicyEntriesResponse>
        implements PolicyQueryCommandResponse<RetrievePolicyEntriesResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrievePolicyEntries.NAME;

    static final JsonFieldDefinition<JsonObject> JSON_POLICY_ENTRIES =
            JsonFactory.newJsonObjectFieldDefinition("policyEntries", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyId policyId;
    private final JsonObject policyEntries;

    private RetrievePolicyEntriesResponse(final PolicyId policyId,
            final HttpStatus httpStatus,
            final JsonObject policyEntries,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.policyId = checkNotNull(policyId, "Policy ID");
        this.policyEntries = checkNotNull(policyEntries, "Policy entry");
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntries} command.
     *
     * @param policyId the Policy ID of the retrieved policy entries.
     * @param policyEntries the retrieved Policy entries.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Policy ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.policies.PolicyId, Iterable, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrievePolicyEntriesResponse of(final String policyId, final Iterable<PolicyEntry> policyEntries,
            final DittoHeaders dittoHeaders) {

        return of(PolicyId.of(policyId), policyEntries, dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntries} command.
     *
     * @param policyId the Policy ID of the retrieved policy entries.
     * @param policyEntries the retrieved Policy entries.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrievePolicyEntriesResponse of(final PolicyId policyId, final Iterable<PolicyEntry> policyEntries,
            final DittoHeaders dittoHeaders) {

        final JsonObjectBuilder objectBuilder = JsonFactory.newObjectBuilder();
        checkNotNull(policyEntries, "Policy Entries").forEach(entry -> objectBuilder
                .set(entry.getLabel().toString(),
                        entry.toJson(dittoHeaders.getSchemaVersion().orElse(JsonSchemaVersion.LATEST))));
        return of(policyId, objectBuilder.build(), dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntries} command.
     *
     * @param policyId the Policy ID of the retrieved policy entries.
     * @param policyEntries the retrieved Policy entries.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Policy ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.policies.PolicyId, org.eclipse.ditto.json.JsonObject, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrievePolicyEntriesResponse of(final String policyId, final JsonObject policyEntries,
            final DittoHeaders dittoHeaders) {

        return of(PolicyId.of(policyId), policyEntries, dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntries} command.
     *
     * @param policyId the Policy ID of the retrieved policy entries.
     * @param policyEntries the retrieved Policy entries.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrievePolicyEntriesResponse of(final PolicyId policyId, final JsonObject policyEntries,
            final DittoHeaders dittoHeaders) {

        return new RetrievePolicyEntriesResponse(policyId, HttpStatus.OK, policyEntries, dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntries} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected format.
     */
    public static RetrievePolicyEntriesResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntries} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected format.
     */
    public static RetrievePolicyEntriesResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<RetrievePolicyEntriesResponse>(TYPE, jsonObject)
                .deserialize(httpStatus -> {
                    final String extractedPolicyId =
                            jsonObject.getValueOrThrow(PolicyCommandResponse.JsonFields.JSON_POLICY_ID);
                    final PolicyId policyId = PolicyId.of(extractedPolicyId);
                    final JsonObject extractedPolicyEntries = jsonObject.getValueOrThrow(JSON_POLICY_ENTRIES);

                    return of(policyId, extractedPolicyEntries, dittoHeaders);
                });
    }

    @Override
    public PolicyId getEntityId() {
        return policyId;
    }

    /**
     * Returns the retrieved Policy entries.
     *
     * @return the retrieved Policy entries.
     */
    public Iterable<PolicyEntry> getPolicyEntries() {
        return PoliciesModelFactory.newPolicyEntries(policyEntries);
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return policyEntries;
    }

    @Override
    public RetrievePolicyEntriesResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(policyId, entity.asObject(), getDittoHeaders());
    }

    @Override
    public RetrievePolicyEntriesResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(policyId, policyEntries, dittoHeaders);
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/entries");
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(PolicyCommandResponse.JsonFields.JSON_POLICY_ID, String.valueOf(policyId), predicate);
        jsonObjectBuilder.set(JSON_POLICY_ENTRIES, policyEntries, predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrievePolicyEntriesResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetrievePolicyEntriesResponse that = (RetrievePolicyEntriesResponse) o;
        return that.canEqual(this) &&
                Objects.equals(policyId, that.policyId) &&
                Objects.equals(policyEntries, that.policyEntries) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), policyId, policyEntries);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", policyId=" + policyId + ", policyEntries=" +
                policyEntries + "]";
    }

}
