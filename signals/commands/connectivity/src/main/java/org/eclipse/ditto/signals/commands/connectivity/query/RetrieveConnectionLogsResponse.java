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
package org.eclipse.ditto.signals.commands.connectivity.query;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonParseException;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.LogEntry;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.connectivity.ConnectivityCommandResponse;

/**
 * Response to a {@link org.eclipse.ditto.signals.commands.connectivity.query.RetrieveConnectionLogs} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrieveConnectionLogsResponse.TYPE)
public final class RetrieveConnectionLogsResponse
        extends AbstractCommandResponse<RetrieveConnectionLogsResponse>
        implements ConnectivityQueryCommandResponse<RetrieveConnectionLogsResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = ConnectivityCommandResponse.TYPE_PREFIX + RetrieveConnectionLogs.NAME;

    private final ConnectionId connectionId;
    private final Collection<LogEntry> connectionLogs;

    @Nullable private final Instant enabledSince;
    @Nullable private final Instant enabledUntil;

    private RetrieveConnectionLogsResponse(final ConnectionId connectionId,
            final Collection<LogEntry> connectionLogs,
            @Nullable final Instant enabledSince,
            @Nullable final Instant enabledUntil,
            final DittoHeaders dittoHeaders) {

        super(TYPE, HttpStatus.OK, dittoHeaders);

        this.connectionId = connectionId;
        this.connectionLogs = Collections.unmodifiableList(new ArrayList<>(connectionLogs));
        this.enabledSince = enabledSince;
        this.enabledUntil = enabledUntil;
    }

    /**
     * Returns a new instance of {@code RetrieveConnectionLogsResponse}.
     *
     * @param connectionId the identifier of the connection.
     * @param connectionLogs the connection logs.
     * @param enabledSince when the logging was enabled.
     * @param enabledUntil until when the logging will stay enabled.
     * @param dittoHeaders the headers of the request.
     * @return a new RetrieveConnectionLogsResponse response.
     * @throws NullPointerException if {@code connectionId}, {@code connectionLogs} or {@code dittoHeaders} are {@code
     * null}.
     */
    public static RetrieveConnectionLogsResponse of(final ConnectionId connectionId,
            final Collection<LogEntry> connectionLogs, @Nullable final Instant enabledSince,
            @Nullable final Instant enabledUntil, final DittoHeaders dittoHeaders) {
        checkNotNull(connectionId, "Connection ID");
        checkNotNull(connectionLogs, "Connection Logs");

        return new RetrieveConnectionLogsResponse(connectionId, connectionLogs, enabledSince,
                enabledUntil, dittoHeaders);
    }

    /**
     * Merges the passed in {@code RetrieveConnectionLogsResponse}s into each other returning a new {@link
     * RetrieveConnectionLogsResponse} containing the merged information. The result will contain all logs of each
     * response, the connection id and the ditto headers of the first response, as well as the timestamps of the first
     * response (or {@code null} if they don't exist).
     *
     * @param first the first RetrieveConnectionMetricsResponse to merge.
     * @param second the second RetrieveConnectionMetricsResponse to merge.
     * @return the new merged RetrieveConnectionMetricsResponse.
     */
    public static RetrieveConnectionLogsResponse mergeRetrieveConnectionLogsResponse(
            final RetrieveConnectionLogsResponse first,
            final RetrieveConnectionLogsResponse second) {

        final Collection<LogEntry> mergedEntries = new ArrayList<>();
        mergedEntries.addAll(first.getConnectionLogs());
        mergedEntries.addAll(second.getConnectionLogs());

        return of(first.getConnectionEntityId(),
                mergedEntries,
                first.getEnabledSince().orElse(null),
                first.getEnabledUntil().orElse(null),
                first.getDittoHeaders());
    }

    /**
     * Creates a new {@code RetrieveConnectionLogsResponse} from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be retrieved.
     * @param dittoHeaders the headers of the response.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrieveConnectionLogsResponse fromJson(final String jsonString,
            final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code RetrieveConnectionLogsResponse} from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the response.
     * @return the response.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrieveConnectionLogsResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<RetrieveConnectionLogsResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> getRetrieveConnectionLogsResponse(jsonObject, dittoHeaders));
    }

    private static RetrieveConnectionLogsResponse getRetrieveConnectionLogsResponse(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        final String readConnectionId =
                jsonObject.getValueOrThrow(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID);
        final ConnectionId connectionId = ConnectionId.of(readConnectionId);
        final List<LogEntry> readConnectionLogs = parseConnectionLogs(jsonObject);
        final Instant readEnabledSince = parseInstantOrNull(jsonObject, JsonFields.ENABLED_SINCE);
        final Instant readEnabledUntil = parseInstantOrNull(jsonObject, JsonFields.ENABLED_UNTIL);

        return of(connectionId, readConnectionLogs, readEnabledSince, readEnabledUntil, dittoHeaders);
    }

    /**
     * @param jsonObject the object containing an instant.
     * @param fieldDefinition the definition where to find the instant in the {@code jsonObject}.
     * @return the parsed {@link java.time.Instant} or {@code null} if the field was null or not part of the {@code
     * jsonObject}.
     * @throws org.eclipse.ditto.json.JsonParseException if {@code jsonObject} contains a non-null field under {@code
     * fieldDefinition} that is not in ISO-8601 format.
     */
    @Nullable
    private static Instant parseInstantOrNull(final JsonObject jsonObject,
            final JsonFieldDefinition<String> fieldDefinition) {
        return jsonObject.getValue(fieldDefinition)
                .map(field -> RetrieveConnectionLogsResponse.tryToParseInstant(field, fieldDefinition))
                .orElse(null);
    }

    private static Instant tryToParseInstant(final CharSequence dateTime,
            final JsonFieldDefinition<String> fieldDefinition) {
        try {
            return Instant.parse(dateTime);
        } catch (final DateTimeParseException e) {
            final String msgPattern = "The JSON object''s field <{0}> is not in ISO-8601 format as expected!";
            throw JsonParseException.newBuilder()
                    .message(MessageFormat.format(msgPattern, fieldDefinition.getPointer()))
                    .cause(e)
                    .build();
        }
    }

    private static List<LogEntry> parseConnectionLogs(final JsonObject jsonObject) {
        final Optional<JsonArray> logsArray = jsonObject.getValue(JsonFields.CONNECTION_LOGS);
        if (logsArray.isPresent()) {
            final JsonArray values = logsArray.get();
            return values.stream()
                    .filter(JsonValue::isObject)
                    .map(JsonValue::asObject)
                    .map(ConnectivityModelFactory::logEntryFromJson)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @return the log entries for the connection.
     */
    public Collection<LogEntry> getConnectionLogs() {
        return connectionLogs;
    }

    /**
     * @return since when logging is enabled for the connection, or empty if it isn't enabled.
     */
    public Optional<Instant> getEnabledSince() {
        return Optional.ofNullable(enabledSince);
    }

    /**
     * @return until when logging is enabled for the connection, or empty if it isn't enabled.
     */
    public Optional<Instant> getEnabledUntil() {
        return Optional.ofNullable(enabledUntil);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID, String.valueOf(connectionId),
                predicate);

        jsonObjectBuilder.set(JsonFields.CONNECTION_LOGS, connectionLogs.stream()
                .map(logEntry -> logEntry.toJson(schemaVersion, thePredicate))
                .collect(JsonCollectors.valuesToArray()), predicate.and(Objects::nonNull));
        jsonObjectBuilder.set(JsonFields.ENABLED_SINCE, getEnabledSince().map(Instant::toString).orElse(null),
                predicate);
        jsonObjectBuilder.set(JsonFields.ENABLED_UNTIL, getEnabledUntil().map(Instant::toString).orElse(null),
                predicate);
    }

    @Override
    public ConnectionId getConnectionEntityId() {
        return connectionId;
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder();
        appendPayload(jsonObjectBuilder, schemaVersion, field -> true);
        return jsonObjectBuilder.build();
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/logs");
    }

    @Override
    public RetrieveConnectionLogsResponse setEntity(final JsonValue entity) {
        return getRetrieveConnectionLogsResponse(entity.asObject(), getDittoHeaders());
    }

    @Override
    public RetrieveConnectionLogsResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(connectionId, connectionLogs, enabledSince, enabledUntil, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrieveConnectionLogsResponse;
    }

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
        final RetrieveConnectionLogsResponse that = (RetrieveConnectionLogsResponse) o;
        return Objects.equals(connectionId, that.connectionId) &&
                Objects.equals(connectionLogs, that.connectionLogs) &&
                Objects.equals(enabledSince, that.enabledSince) &&
                Objects.equals(enabledUntil, that.enabledUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connectionId, connectionLogs, enabledSince, enabledUntil);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", connectionId=" + connectionId +
                ", connectionLogs=" + connectionLogs +
                ", enabledSince=" + enabledSince +
                ", enabledUntil=" + enabledUntil +
                "]";
    }

    /**
     * An enumeration of the known {@code JsonField}s of a {@code RetrieveConnectionLogsResponse}.
     */
    @Immutable
    static final class JsonFields {

        /**
         * JSON field containing when logging was enabled.
         */
        public static final JsonFieldDefinition<String> ENABLED_SINCE =
                JsonFactory.newStringFieldDefinition("enabledSince", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        /**
         * JSON field containing when logging gets disabled.
         */
        public static final JsonFieldDefinition<String> ENABLED_UNTIL =
                JsonFactory.newStringFieldDefinition("enabledUntil", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        /**
         * JSON field containing the logs.
         */
        public static final JsonFieldDefinition<JsonArray> CONNECTION_LOGS =
                JsonFactory.newJsonArrayFieldDefinition("connectionLogs", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        private JsonFields() {
            throw new AssertionError();
        }

    }

}
