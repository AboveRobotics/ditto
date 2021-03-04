/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingDefinition;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link ModifyThingDefinition} command.
 */
@Immutable
@JsonParsableCommandResponse(type = ModifyThingDefinitionResponse.TYPE)
public final class ModifyThingDefinitionResponse extends AbstractCommandResponse<ModifyThingDefinitionResponse>
        implements ThingModifyCommandResponse<ModifyThingDefinitionResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + ModifyThingDefinition.NAME;

    static final JsonFieldDefinition<String> JSON_DEFINITION =
            JsonFactory.newStringFieldDefinition("definition", FieldType.REGULAR, JsonSchemaVersion.V_2);


    private final ThingId thingId;
    @Nullable private final ThingDefinition definition;

    private ModifyThingDefinitionResponse(final ThingId thingId,
            final HttpStatus httpStatus,
            @Nullable final ThingDefinition definition,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "Thing ID");
        this.definition = definition;
    }


    /**
     * Returns a new {@code ModifyThingDefinitionResponse} for a created definition. This corresponds to the HTTP
     * status {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created definition.
     * @param definition the created definition.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created definition.
     * @throws NullPointerException if {@code thingId} or {@code dittoHeaders} is {@code null}.
     */
    public static ModifyThingDefinitionResponse created(final ThingId thingId,
            @Nullable final ThingDefinition definition, final DittoHeaders dittoHeaders) {

        return new ModifyThingDefinitionResponse(thingId, HttpStatus.CREATED, definition, dittoHeaders);
    }


    /**
     * Returns a new {@code ModifyThingDefinitionResponse} for a modified definition. This corresponds to the HTTP
     * status {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified definition.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified definition.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyThingDefinitionResponse modified(final ThingId thingId, final DittoHeaders dittoHeaders) {
        return new ModifyThingDefinitionResponse(thingId, HttpStatus.NO_CONTENT, null, dittoHeaders);
    }

    /**
     * Creates a response to a {@link org.eclipse.ditto.signals.commands.things.modify.ModifyThingDefinition} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static ModifyThingDefinitionResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyThingDefinition} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static ModifyThingDefinitionResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<ModifyThingDefinitionResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);

                    final ThingDefinition definition = jsonObject.getValue(JSON_DEFINITION)
                            .map(ThingsModelFactory::newDefinition)
                            .orElse(null);

                    return new ModifyThingDefinitionResponse(thingId, httpStatus, definition, dittoHeaders);
                });
    }

    /**
     * ModifyThingDefinitionResponse is only available in JsonSchemaVersion V_2.
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
     * Returns the created definition.
     *
     * @return the created definition.
     */
    public Optional<ThingDefinition> getDefinition() {
        return Optional.ofNullable(definition);
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.ofNullable(definition).map(JsonValue::of);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = Thing.JsonFields.DEFINITION.getPointer().toString();
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        if (definition != null) {
            jsonObjectBuilder.set(JSON_DEFINITION, String.valueOf(definition), predicate);
        }
    }

    @Override
    public ModifyThingDefinitionResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return definition != null ? created(thingId, definition, dittoHeaders) : modified(thingId, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ModifyThingDefinitionResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModifyThingDefinitionResponse that = (ModifyThingDefinitionResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(definition, that.definition) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, definition);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId + ", definition=" +
                definition + "]";
    }

}
