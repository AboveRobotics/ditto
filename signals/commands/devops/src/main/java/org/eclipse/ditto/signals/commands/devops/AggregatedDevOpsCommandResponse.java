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
package org.eclipse.ditto.signals.commands.devops;

import java.util.List;
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
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.base.WithEntity;

/**
 * A {@link DevOpsCommandResponse} aggregating multiple {@link CommandResponse}s.
 */
@Immutable
@JsonParsableCommandResponse(type = AggregatedDevOpsCommandResponse.TYPE)
public final class AggregatedDevOpsCommandResponse
        extends AbstractDevOpsCommandResponse<AggregatedDevOpsCommandResponse>
        implements WithEntity<AggregatedDevOpsCommandResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + "aggregatedResponse";

    private static final JsonFieldDefinition<String> JSON_RESPONSES_TYPE =
            JsonFactory.newStringFieldDefinition("responsesType", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);
    private static final JsonFieldDefinition<JsonObject> JSON_AGGREGATED_RESPONSES =
            JsonFactory.newJsonObjectFieldDefinition("responses", FieldType.REGULAR, JsonSchemaVersion.V_1,
                    JsonSchemaVersion.V_2);

    private final JsonObject aggregatedResponses;
    private final String responsesType;

    private AggregatedDevOpsCommandResponse(final JsonObject aggregatedResponses,
            final String responsesType,
            final HttpStatus httpStatus,
            final DittoHeaders dittoHeaders) {

        super(TYPE, null, null, httpStatus, dittoHeaders);
        this.aggregatedResponses = aggregatedResponses;
        this.responsesType = responsesType;
    }

    /**
     * Returns a new instance of {@code AggregatedDevOpsCommandResponse}.
     *
     * @param commandResponses the aggregated {@link DevOpsCommandResponse}s.
     * @param responsesType the responses type of the responses to expect.
     * @param httpStatusCode the {@link HttpStatusCode} to send back as response status.
     * @param dittoHeaders the headers of the request.
     * @return the new RetrieveLoggerConfigResponse response.
     * @deprecated as of 2.0.0 please use {@link #of(List, String, HttpStatus, DittoHeaders)} instead.
     */
    @Deprecated
    public static AggregatedDevOpsCommandResponse of(final List<CommandResponse<?>> commandResponses,
            final String responsesType,
            final HttpStatusCode httpStatusCode,
            final DittoHeaders dittoHeaders) {

        return of(commandResponses, responsesType, httpStatusCode.getAsHttpStatus(), dittoHeaders);
    }

    /**
     * Returns a new instance of {@code AggregatedDevOpsCommandResponse}.
     *
     * @param commandResponses the aggregated {@link DevOpsCommandResponse}s.
     * @param responsesType the responses type of the responses to expect.
     * @param httpStatus the HTTP status to send back as response status.
     * @param dittoHeaders the headers of the request.
     * @return the new RetrieveLoggerConfigResponse response.
     * @since 2.0.0
     */
    public static AggregatedDevOpsCommandResponse of(final List<CommandResponse<?>> commandResponses,
            final String responsesType,
            final HttpStatus httpStatus,
            final DittoHeaders dittoHeaders) {

        final JsonObject jsonRepresentation = buildJsonRepresentation(commandResponses, dittoHeaders);
        return new AggregatedDevOpsCommandResponse(jsonRepresentation, responsesType, httpStatus, dittoHeaders);
    }

    /**
     * Returns a new instance of {@code AggregatedDevOpsCommandResponse}.
     *
     * @param aggregatedResponses the aggregated {@link DevOpsCommandResponse}s as a JsonObject.
     * @param responsesType the responses type of the responses to expect.
     * @param httpStatusCode the {@link HttpStatusCode} to send back as response status.
     * @param dittoHeaders the headers of the request.
     * @return the new RetrieveLoggerConfigResponse response.
     * @deprecated as of 2.0.0 please use {@link #of(JsonObject, String, HttpStatus, DittoHeaders)} instead.
     */
    @Deprecated
    public static AggregatedDevOpsCommandResponse of(final JsonObject aggregatedResponses,
            final String responsesType,
            final HttpStatusCode httpStatusCode,
            final DittoHeaders dittoHeaders) {

        return of(aggregatedResponses, responsesType, httpStatusCode.getAsHttpStatus(), dittoHeaders);
    }

    /**
     * Returns a new instance of {@code AggregatedDevOpsCommandResponse}.
     *
     * @param aggregatedResponses the aggregated {@link DevOpsCommandResponse}s as a JsonObject.
     * @param responsesType the responses type of the responses to expect.
     * @param httpStatus the HTTP status to send back as response status.
     * @param dittoHeaders the headers of the request.
     * @return the new RetrieveLoggerConfigResponse response.
     * @since 2.0.0
     */
    public static AggregatedDevOpsCommandResponse of(final JsonObject aggregatedResponses,
            final String responsesType,
            final HttpStatus httpStatus,
            final DittoHeaders dittoHeaders) {

        return new AggregatedDevOpsCommandResponse(aggregatedResponses, responsesType, httpStatus, dittoHeaders);
    }

    /**
     * Creates a response to a {@code AggregatedDevOpsCommandResponse} command from a JSON string.
     *
     * @param jsonString contains the data of the AggregatedDevOpsCommandResponse command.
     * @param dittoHeaders the headers of the request.
     * @return the AggregatedDevOpsCommandResponse command which is based on the dta of {@code jsonString}.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static AggregatedDevOpsCommandResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@code AggregatedDevOpsCommandResponse} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static AggregatedDevOpsCommandResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<AggregatedDevOpsCommandResponse>(TYPE, jsonObject)
                .deserialize(httpStatus -> {
                    final JsonObject aggregatedResponsesJsonObj = jsonObject.getValueOrThrow(JSON_AGGREGATED_RESPONSES);
                    final String theResponsesType = jsonObject.getValueOrThrow(JSON_RESPONSES_TYPE);
                    return of(aggregatedResponsesJsonObj, theResponsesType, httpStatus, dittoHeaders);
                });
    }

    @Override
    public AggregatedDevOpsCommandResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(aggregatedResponses, responsesType, getHttpStatus(), dittoHeaders);
    }

    /**
     * @return the responses type of the responses to expect.
     */
    public String getResponsesType() {
        return responsesType;
    }

    @Override
    public AggregatedDevOpsCommandResponse setEntity(final JsonValue entity) {
        throw new UnsupportedOperationException("Setting entity on AggregatedDevOpsCommandResponse is not supported");
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return aggregatedResponses;
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        super.appendPayload(jsonObjectBuilder, schemaVersion, thePredicate);

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(JSON_RESPONSES_TYPE, responsesType, predicate);
        jsonObjectBuilder.set(JSON_AGGREGATED_RESPONSES, aggregatedResponses, predicate);
    }

    private static JsonObject buildJsonRepresentation(final List<CommandResponse<?>> commandResponses,
            final DittoHeaders dittoHeaders) {

        final JsonSchemaVersion schemaVersion = dittoHeaders.getSchemaVersion().orElse(JsonSchemaVersion.LATEST);
        final JsonObjectBuilder builder = JsonObject.newBuilder();

        int i = 0;
        for (final CommandResponse<?> cmdR : commandResponses) {
            final String key = String.format("/%s/%s", calculateServiceName(cmdR), calculateInstance(cmdR, i++));
            // include both regular and special fields for devops command responses
            final JsonObject responseJson = cmdR.toJson(schemaVersion, FieldType.regularOrSpecial());
            builder.set(key, responseJson);
        }

        if (builder.isEmpty()) {
            return JsonFactory.nullObject();
        } else {
            return builder.build();
        }
    }

    private static String calculateServiceName(final CommandResponse<?> commandResponse) {
        if (commandResponse instanceof DevOpsCommandResponse) {
            return ((DevOpsCommandResponse<?>) commandResponse).getServiceName().orElse("?");
        } else {
            return "?";
        }
    }

    private static String calculateInstance(final CommandResponse<?> commandResponse, final int i) {
        if (commandResponse instanceof DevOpsCommandResponse) {
            return ((DevOpsCommandResponse<?>) commandResponse).getInstance()
                    .orElse("?" + (i == 0 ? "" : String.valueOf(i)));
        } else {
            return "?" + (i == 0 ? "" : String.valueOf(i));
        }
    }

    @SuppressWarnings("squid:MethodCyclomaticComplexity")
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final AggregatedDevOpsCommandResponse that = (AggregatedDevOpsCommandResponse) o;
        return that.canEqual(this) &&
                Objects.equals(responsesType, that.responsesType) &&
                Objects.equals(aggregatedResponses, that.aggregatedResponses) &&
                super.equals(that);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof AggregatedDevOpsCommandResponse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), responsesType, aggregatedResponses);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", responsesType=" + responsesType +
                ", aggregatedResponses=" + aggregatedResponses + "]";
    }

}
