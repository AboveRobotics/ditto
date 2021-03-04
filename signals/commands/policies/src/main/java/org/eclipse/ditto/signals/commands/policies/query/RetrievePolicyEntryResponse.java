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
 * Response to a {@link RetrievePolicyEntry} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrievePolicyEntryResponse.TYPE)
public final class RetrievePolicyEntryResponse extends AbstractCommandResponse<RetrievePolicyEntryResponse>
        implements PolicyQueryCommandResponse<RetrievePolicyEntryResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrievePolicyEntry.NAME;

    static final JsonFieldDefinition<String> JSON_LABEL =
            JsonFactory.newStringFieldDefinition("label", FieldType.REGULAR, JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<JsonObject> JSON_POLICY_ENTRY =
            JsonFactory.newJsonObjectFieldDefinition("policyEntry", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyId policyId;
    private final String policyEntryLabel;
    private final JsonObject policyEntry;

    private RetrievePolicyEntryResponse(final PolicyId policyId,
            final HttpStatus httpStatus,
            final String policyEntryLabel,
            final JsonObject policyEntry,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.policyId = checkNotNull(policyId, "Policy ID");
        this.policyEntryLabel = checkNotNull(policyEntryLabel, "Policy entry label");
        this.policyEntry = checkNotNull(policyEntry, "Policy entry");
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntry} command.
     *
     * @param policyId the Policy ID of the retrieved policy entry.
     * @param policyEntry the retrieved Policy entry.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Policy ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.policies.PolicyId, org.eclipse.ditto.model.policies.PolicyEntry, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrievePolicyEntryResponse of(final String policyId, final PolicyEntry policyEntry,
            final DittoHeaders dittoHeaders) {

        return of(PolicyId.of(policyId), policyEntry, dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntry} command.
     *
     * @param policyId the Policy ID of the retrieved policy entry.
     * @param policyEntry the retrieved Policy entry.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrievePolicyEntryResponse of(final PolicyId policyId, final PolicyEntry policyEntry,
            final DittoHeaders dittoHeaders) {

        final String policyEntryLabel = policyEntry.getLabel().toString();
        final JsonObject jsonPolicyEntry = checkNotNull(policyEntry, "Policy Entry")
                .toJson(dittoHeaders.getSchemaVersion().orElse(policyEntry.getLatestSchemaVersion()));

        return of(policyId, policyEntryLabel, jsonPolicyEntry, dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntry} command.
     *
     * @param policyId the Policy ID of the retrieved policy entry.
     * @param policyEntryLabel the Label for the PolicyEntry to create.
     * @param policyEntry the retrieved Policy entry.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Policy ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.policies.PolicyId, String, org.eclipse.ditto.json.JsonObject, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrievePolicyEntryResponse of(final String policyId,
            final String policyEntryLabel,
            final JsonObject policyEntry,
            final DittoHeaders dittoHeaders) {

        return of(PolicyId.of(policyId), policyEntryLabel, policyEntry, dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntry} command.
     *
     * @param policyId the Policy ID of the retrieved policy entry.
     * @param policyEntryLabel the Label for the PolicyEntry to create.
     * @param policyEntry the retrieved Policy entry.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrievePolicyEntryResponse of(final PolicyId policyId,
            final String policyEntryLabel,
            final JsonObject policyEntry,
            final DittoHeaders dittoHeaders) {

        return new RetrievePolicyEntryResponse(policyId, HttpStatus.OK, policyEntryLabel, policyEntry,
                dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntry} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrievePolicyEntryResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@code RetrievePolicyEntry} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrievePolicyEntryResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<RetrievePolicyEntryResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String extractedPolicyId =
                            jsonObject.getValueOrThrow(PolicyCommandResponse.JsonFields.JSON_POLICY_ID);
                    final PolicyId policyId = PolicyId.of(extractedPolicyId);
                    final String extractedLabel = jsonObject.getValueOrThrow(JSON_LABEL);
                    final JsonObject extractedPolicyEntry = jsonObject.getValueOrThrow(JSON_POLICY_ENTRY);

                    return of(policyId, extractedLabel, extractedPolicyEntry, dittoHeaders);
                });
    }

    @Override
    public PolicyId getEntityId() {
        return policyId;
    }

    /**
     * Returns the retrieved Policy entry.
     *
     * @return the retrieved Policy entry.
     */
    public PolicyEntry getPolicyEntry() {
        return PoliciesModelFactory.newPolicyEntry(policyEntryLabel, policyEntry);
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return policyEntry;
    }

    @Override
    public RetrievePolicyEntryResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(policyId, policyEntryLabel, entity.asObject(), getDittoHeaders());
    }

    @Override
    public RetrievePolicyEntryResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(policyId, policyEntryLabel, policyEntry, dittoHeaders);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/entries/" + policyEntryLabel;
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(PolicyCommandResponse.JsonFields.JSON_POLICY_ID, String.valueOf(policyId), predicate);
        jsonObjectBuilder.set(JSON_LABEL, policyEntryLabel, predicate);
        jsonObjectBuilder.set(JSON_POLICY_ENTRY, policyEntry, predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrievePolicyEntryResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetrievePolicyEntryResponse that = (RetrievePolicyEntryResponse) o;
        return that.canEqual(this) &&
                Objects.equals(policyId, that.policyId) &&
                Objects.equals(policyEntryLabel, that.policyEntryLabel) &&
                Objects.equals(policyEntry, that.policyEntry) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), policyId, policyEntryLabel, policyEntry);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", policyId=" + policyId
                + ", policyEntryLabel=" + policyEntryLabel + ", policyEntry=" + policyEntry + "]";
    }

}
