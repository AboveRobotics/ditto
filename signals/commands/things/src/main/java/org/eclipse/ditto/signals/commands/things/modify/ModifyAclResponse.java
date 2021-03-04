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
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link ModifyAcl} command.
 *
 * @deprecated AccessControlLists belong to deprecated API version 1. Use API version 2 with policies instead.
 */
@Deprecated
@Immutable
@JsonParsableCommandResponse(type = ModifyAclResponse.TYPE)
public final class ModifyAclResponse extends AbstractCommandResponse<ModifyAclResponse>
        implements ThingModifyCommandResponse<ModifyAclResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + ModifyAcl.NAME;

    static final JsonFieldDefinition<JsonObject> JSON_ACL =
            JsonFactory.newJsonObjectFieldDefinition("acl", FieldType.REGULAR, JsonSchemaVersion.V_1);

    private final ThingId thingId;
    private final AccessControlList modifiedAcl;

    private ModifyAclResponse(final ThingId thingId,
            final HttpStatus httpStatus,
            final AccessControlList modifiedAcl,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "Thing ID");
        this.modifiedAcl = modifiedAcl;
    }

    /**
     * Returns a new {@code ModifyAclResponse} for a created AccessControlList. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created ACL.
     * @param acl the created AccessControlList.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created Thing.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #created(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.model.things.AccessControlList, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static ModifyAclResponse created(final String thingId, final AccessControlList acl,
            final DittoHeaders dittoHeaders) {

        return created(ThingId.of(thingId), acl, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyAclResponse} for a created AccessControlList. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created ACL.
     * @param acl the created AccessControlList.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created Thing.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyAclResponse created(final ThingId thingId, final AccessControlList acl,
            final DittoHeaders dittoHeaders) {

        return new ModifyAclResponse(thingId, HttpStatus.CREATED, checkNotNull(acl, "created ACL"), dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyAclResponse} for a modified AccessControlList. This corresponds to the HTTP status
     * code {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified ACL.
     * @param acl the modified ACL.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified Thing.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use
     * {@link #modified(org.eclipse.ditto.model.things.ThingId, org.eclipse.ditto.model.things.AccessControlList, org.eclipse.ditto.model.base.headers.DittoHeaders)}
     * instead.
     */
    @Deprecated
    public static ModifyAclResponse modified(final String thingId, final AccessControlList acl,
            final DittoHeaders dittoHeaders) {

        return modified(ThingId.of(thingId), acl, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyAclResponse} for a modified AccessControlList. This corresponds to the HTTP status
     * code {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified ACL.
     * @param acl the modified ACL.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified Thing.
     * @throws NullPointerException if {@code dittoHeaders} is {@code null}.
     */
    public static ModifyAclResponse modified(final ThingId thingId, final AccessControlList acl,
            final DittoHeaders dittoHeaders) {

        return new ModifyAclResponse(thingId, HttpStatus.NO_CONTENT, checkNotNull(acl, "modified ACL"),
                dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyAcl} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static ModifyAclResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyAcl} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static ModifyAclResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<ModifyAclResponse>(TYPE, jsonObject).deserialize(httpStatus -> {
            final String extractedThingId = jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
            final ThingId thingId = ThingId.of(extractedThingId);
            final JsonObject aclJsonObject = jsonObject.getValueOrThrow(JSON_ACL);
            final AccessControlList extractedAcl = ThingsModelFactory.newAcl(aclJsonObject);

            return new ModifyAclResponse(thingId, httpStatus, extractedAcl, dittoHeaders);
        });
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    /**
     * Returns the modified ACL.
     *
     * @return the ACL
     */
    public AccessControlList getAcl() {
        return modifiedAcl;
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        if (!isCreated()) {
            return Optional.empty();
        }
        return Optional.of(modifiedAcl.toJson(schemaVersion, FieldType.notHidden()));
    }

    private boolean isCreated() {
        return HttpStatus.CREATED.equals(getHttpStatus());
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/acl");
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> predicate) {

        final Predicate<JsonField> p = schemaVersion.and(predicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), p);
        jsonObjectBuilder.set(JSON_ACL, modifiedAcl.toJson(schemaVersion, predicate), p);
    }

    @Override
    public ModifyAclResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new ModifyAclResponse(thingId, getHttpStatus(), modifiedAcl, dittoHeaders);
    }

    /**
     * ModifyAclResponse is only available in JsonSchemaVersion V_1.
     *
     * @return the supported JsonSchemaVersions of ModifyAclResponse.
     */
    @Override
    public JsonSchemaVersion[] getSupportedSchemaVersions() {
        return new JsonSchemaVersion[]{JsonSchemaVersion.V_1};
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModifyAclResponse that = (ModifyAclResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(modifiedAcl, that.modifiedAcl) &&
                super.equals(o);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ModifyAclResponse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, modifiedAcl);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId + ", aclCreated=" +
                modifiedAcl + "]";
    }

}
