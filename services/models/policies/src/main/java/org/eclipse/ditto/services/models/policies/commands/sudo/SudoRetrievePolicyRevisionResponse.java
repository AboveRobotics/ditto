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
package org.eclipse.ditto.services.models.policies.commands.sudo;

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
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;

/**
 * Response to a {@link org.eclipse.ditto.services.models.policies.commands.sudo.SudoRetrievePolicyRevisionResponse} command.
 */
@Immutable
@JsonParsableCommandResponse(type = SudoRetrievePolicyRevisionResponse.TYPE)
public final class SudoRetrievePolicyRevisionResponse
        extends AbstractCommandResponse<SudoRetrievePolicyRevisionResponse>
        implements SudoCommandResponse<SudoRetrievePolicyRevisionResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + SudoRetrievePolicyRevision.NAME;

    static final JsonFieldDefinition<Long> JSON_REVISION =
            JsonFactory.newLongFieldDefinition("payload/revision", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyId policyId;
    private final long revision;

    private SudoRetrievePolicyRevisionResponse(final PolicyId policyId, final long revision,
            final DittoHeaders dittoHeaders) {

        super(TYPE, HttpStatus.OK, dittoHeaders);
        this.policyId = checkNotNull(policyId, "Policy ID");
        this.revision = revision;
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyRevision} command.
     *
     * @param policyId the policy ID.
     * @param revision the policy revision.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     */
    public static SudoRetrievePolicyRevisionResponse of(final PolicyId policyId, final long revision,
            final DittoHeaders dittoHeaders) {

        return new SudoRetrievePolicyRevisionResponse(policyId, revision, dittoHeaders);
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyResponse} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static SudoRetrievePolicyRevisionResponse fromJson(final String jsonString,
            final DittoHeaders dittoHeaders) {

        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyResponse} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static SudoRetrievePolicyRevisionResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<SudoRetrievePolicyRevisionResponse>(TYPE, jsonObject)
                .deserialize(httpStatus -> {
                    final var extractedPolicyId =
                            jsonObject.getValueOrThrow(SudoCommandResponse.JsonFields.JSON_POLICY_ID);
                    final var policyId = PolicyId.of(extractedPolicyId);
                    final long revision = jsonObject.getValueOrThrow(JSON_REVISION);

                    return of(policyId, revision, dittoHeaders);
                });
    }

    @Override
    public PolicyId getEntityId() {
        return policyId;
    }

    /**
     * Returns the retrieved Policy.
     *
     * @return the retrieved Policy.
     */
    public long getRevision() {
        return revision;
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return JsonValue.of(revision);
    }

    @Override
    public SudoRetrievePolicyRevisionResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(policyId, entity.asLong(), getDittoHeaders());
    }

    @Override
    public SudoRetrievePolicyRevisionResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(policyId, revision, dittoHeaders);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(SudoCommandResponse.JsonFields.JSON_POLICY_ID, String.valueOf(policyId), predicate);
        jsonObjectBuilder.set(JSON_REVISION, revision, predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof SudoRetrievePolicyRevisionResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SudoRetrievePolicyRevisionResponse that = (SudoRetrievePolicyRevisionResponse) o;
        return that.canEqual(this) &&
                Objects.equals(policyId, that.policyId) &&
                revision == that.revision &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), policyId, revision);
    }

    @Override
    public String toString() {
        return super.toString() + "policyId=" + policyId + "revision=" + revision + "]";
    }

}
