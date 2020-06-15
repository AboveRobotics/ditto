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
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.AccessControlListModelFactory;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;

/**
 * Response to a {@link RetrieveAcl} command.
 *
 * @deprecated AccessControlLists belong to deprecated API version 1. Use API version 2 with policies instead.
 */
@Deprecated
@Immutable
@JsonParsableCommandResponse(type = RetrieveAclResponse.TYPE)
public final class RetrieveAclResponse extends AbstractCommandResponse<RetrieveAclResponse> implements
        ThingQueryCommandResponse<RetrieveAclResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrieveAcl.NAME;

    static final JsonFieldDefinition<JsonObject> JSON_ACL =
            JsonFactory.newJsonObjectFieldDefinition("acl", FieldType.REGULAR, JsonSchemaVersion.V_1);

    private final ThingId thingId;
    private final JsonObject acl;

    private RetrieveAclResponse(final ThingId thingId, final HttpStatusCode statusCode, final JsonObject acl,
            final DittoHeaders dittoHeaders) {
        super(TYPE, statusCode, dittoHeaders);
        this.thingId = checkNotNull(thingId, "thing ID");
        this.acl = checkNotNull(acl, "AccessControlList");
    }

    /**
     * Creates a response to a {@link RetrieveAcl} command.
     *
     * @param thingId the Thing ID of the retrieved Acl.
     * @param acl the retrieved AccessControlList.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.json.JsonObject, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrieveAclResponse of(final String thingId, final JsonObject acl,
            final DittoHeaders dittoHeaders) {

        return of(ThingId.of(thingId), acl, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAcl} command.
     *
     * @param thingId the Thing ID of the retrieved Acl.
     * @param acl the retrieved AccessControlList.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrieveAclResponse of(final ThingId thingId, final JsonObject acl,
            final DittoHeaders dittoHeaders) {
        return new RetrieveAclResponse(thingId, HttpStatusCode.OK, acl, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAcl} command.
     *
     * @param thingId the Thing ID of the retrieved Acl.
     * @param acl the retrieved AccessControlList.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #of(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.model.things.AccessControlList, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static RetrieveAclResponse of(final String thingId, final AccessControlList acl,
            final DittoHeaders dittoHeaders) {
        return of(ThingId.of(thingId), acl, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAcl} command.
     *
     * @param thingId the Thing ID of the retrieved Acl.
     * @param acl the retrieved AccessControlList.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrieveAclResponse of(final ThingId thingId, final AccessControlList acl,
            final DittoHeaders dittoHeaders) {
        return new RetrieveAclResponse(thingId, HttpStatusCode.OK,
                checkNotNull(acl, "AccessControlList")
                        .toJson(dittoHeaders.getSchemaVersion().orElse(acl.getLatestSchemaVersion())),
                dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAcl} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrieveAclResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveAcl} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrieveAclResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<RetrieveAclResponse>(TYPE, jsonObject)
                .deserialize((statusCode) -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingQueryCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final JsonObject aclJsonObject = jsonObject.getValueOrThrow(JSON_ACL);
                    final AccessControlList extractedAcl = ThingsModelFactory.newAcl(aclJsonObject);

                    return of(thingId, extractedAcl, dittoHeaders);
                });
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the retrieved AccessControlList.
     *
     * @return the retrieved AccessControlList.
     */
    public AccessControlList getAcl() {
        return AccessControlListModelFactory.newAcl(acl);
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return acl;
    }

    @Override
    public RetrieveAclResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(thingId, entity.asObject(), getDittoHeaders());
    }

    @Override
    public RetrieveAclResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(thingId, acl, dittoHeaders);
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/acl");
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingQueryCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        jsonObjectBuilder.set(JSON_ACL, acl, predicate);
    }

    /**
     * RetrieveAclResponse is only available in JsonSchemaVersion V_1.
     *
     * @return the supported JsonSchemaVersions of RetrieveAclResponse.
     */
    @Override
    public JsonSchemaVersion[] getSupportedSchemaVersions() {
        return new JsonSchemaVersion[]{JsonSchemaVersion.V_1};
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrieveAclResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetrieveAclResponse that = (RetrieveAclResponse) o;
        return that.canEqual(this) && Objects.equals(thingId, that.thingId) && Objects.equals(acl, that.acl)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, acl);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId + ", acl=" + acl + "]";
    }

}
