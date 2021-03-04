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
package org.eclipse.ditto.signals.commands.connectivity.query;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
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
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.ConnectivityStatus;
import org.eclipse.ditto.model.connectivity.ResourceStatus;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.connectivity.ConnectivityCommandResponse;

/**
 * Response to a {@link RetrieveConnection} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrieveConnectionStatusResponse.TYPE)
public final class RetrieveConnectionStatusResponse extends AbstractCommandResponse<RetrieveConnectionStatusResponse>
        implements ConnectivityQueryCommandResponse<RetrieveConnectionStatusResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrieveConnectionStatus.NAME;

    private final ConnectionId connectionId;
    private final JsonObject jsonObject;

    private RetrieveConnectionStatusResponse(final ConnectionId connectionId, final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        super(TYPE, HttpStatus.OK, dittoHeaders);
        this.connectionId = checkNotNull(connectionId, "Connection ID");
        this.jsonObject = jsonObject;
    }

    /**
     * Returns a new instance of {@code RetrieveConnectionStatusResponse}.
     *
     * @param connectionId the identifier of the connection.
     * @param jsonObject the retrieved connection status jsonObject.
     * @return a new RetrieveConnectionStatusResponse response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrieveConnectionStatusResponse of(final ConnectionId connectionId,
            final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {
        return new RetrieveConnectionStatusResponse(connectionId, jsonObject, dittoHeaders);
    }

    /**
     * Returns a new instance of {@code RetrieveConnectionStatusResponse}.
     *
     * @param connectionId the identifier of the connection.
     * @param connectionClosedAt the instant when the connection was closed
     * @param clientStatus the {@link ConnectivityStatus} of the client
     * @param statusDetails the details string for the {@code clientStatus}
     * @param dittoHeaders the headers of the request.
     * @return a new RetrieveConnectionStatusResponse response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrieveConnectionStatusResponse closedResponse(final ConnectionId connectionId,
            final String address,
            final Instant connectionClosedAt,
            final ConnectivityStatus clientStatus,
            final String statusDetails,
            final DittoHeaders dittoHeaders) {

        checkNotNull(connectionId, "Connection ID");
        checkNotNull(connectionClosedAt, "connectionClosedAt");
        final ResourceStatus resourceStatus =
                ConnectivityModelFactory.newClientStatus(address, clientStatus, statusDetails, connectionClosedAt);

        return getBuilder(connectionId, dittoHeaders)
                .connectionStatus(clientStatus)
                .liveStatus(clientStatus)
                .connectedSince(null)
                .clientStatus(Collections.singletonList(resourceStatus))
                .sourceStatus(Collections.emptyList())
                .targetStatus(Collections.emptyList())
                .build();
    }

    /**
     * Creates a new {@code RetrieveConnectionStatusResponse} from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be retrieved.
     * @param dittoHeaders the headers of the response.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrieveConnectionStatusResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code RetrieveConnectionStatusResponse} from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the response.
     * @return the response.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrieveConnectionStatusResponse fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<RetrieveConnectionStatusResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String readConnectionId =
                            jsonObject.getValueOrThrow(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID);
                    final ConnectionId connectionId = ConnectionId.of(readConnectionId);

                    return of(connectionId, jsonObject, dittoHeaders);
                });
    }

    private static List<ResourceStatus> readAddressStatus(final JsonArray jsonArray) {
        return jsonArray.stream()
                .filter(JsonValue::isObject)
                .map(JsonValue::asObject)
                .map(ConnectivityModelFactory::resourceStatusFromJson)
                .collect(Collectors.toList());
    }

    /**
     * @return the current ConnectionStatus of the related {@link Connection}.
     */
    public ConnectivityStatus getConnectionStatus() {
        return ConnectivityStatus.forName(jsonObject.getValue(JsonFields.CONNECTION_STATUS).orElse("UNKNOWN"))
                .orElse(ConnectivityStatus.UNKNOWN);
    }

    /**
     * @return the current live ConnectionStatus of the related {@link Connection}.
     */
    public ConnectivityStatus getLiveStatus() {
        return ConnectivityStatus.forName(jsonObject.getValue(JsonFields.LIVE_STATUS).orElse("UNKNOWN"))
                .orElse(ConnectivityStatus.UNKNOWN);
    }

    /**
     * @return the Instant since when the earliest client of the connection was connected.
     */
    public Optional<Instant> getConnectedSince() {
        final String connSinceStr = jsonObject.getValue(JsonFields.CONNECTED_SINCE).orElse(null);
        return Optional.ofNullable(connSinceStr != null ? Instant.parse(connSinceStr) : null);
    }

    /**
     * @return in which state the client handling the {@link Connection} currently is.
     */
    public List<ResourceStatus> getClientStatus() {
        return readAddressStatus(jsonObject.getValue(JsonFields.CLIENT_STATUS).orElse(JsonArray.empty()));
    }

    /**
     * @return the source {@link ResourceStatus}.
     */
    public List<ResourceStatus> getSourceStatus() {
        return readAddressStatus(jsonObject.getValue(JsonFields.SOURCE_STATUS).orElse(JsonArray.empty()));
    }

    /**
     * @return the target {@link ResourceStatus}.
     */
    public List<ResourceStatus> getTargetStatus() {
        return readAddressStatus(jsonObject.getValue(JsonFields.TARGET_STATUS).orElse(JsonArray.empty()));
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID, String.valueOf(connectionId),
                predicate);

        jsonObjectBuilder.setAll(jsonObject);
    }

    @Override
    public ConnectionId getConnectionEntityId() {
        return connectionId;
    }

    @Override
    public RetrieveConnectionStatusResponse setEntity(final JsonValue entity) {
        final JsonObject jsonEntity = entity.asObject();
        final String readConnectionId =
                jsonEntity.getValueOrThrow(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID);

        return of(ConnectionId.of(readConnectionId), jsonEntity, getDittoHeaders());
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return jsonObject;
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/status");
    }

    @Override
    public RetrieveConnectionStatusResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(connectionId, jsonObject, dittoHeaders);
    }

    public static Builder getBuilder(final ConnectionId connectionId, final DittoHeaders dittoHeaders) {
        return new Builder(connectionId, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrieveConnectionStatusResponse;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final RetrieveConnectionStatusResponse that = (RetrieveConnectionStatusResponse) o;
        return connectionId.equals(that.connectionId) && jsonObject.equals(that.jsonObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connectionId, jsonObject);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                " connectionId=" + connectionId +
                ", jsonObject=" + jsonObject +
                "]";
    }

    /**
     * This class contains definitions for all specific fields of a {@code ConnectivityCommandResponse}'s JSON
     * representation.
     */
    public static final class JsonFields extends CommandResponse.JsonFields {

        public static final JsonFieldDefinition<String> CONNECTION_STATUS =
                JsonFactory.newStringFieldDefinition("connectionStatus", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        public static final JsonFieldDefinition<String> LIVE_STATUS =
                JsonFactory.newStringFieldDefinition("liveStatus", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        public static final JsonFieldDefinition<String> CONNECTED_SINCE =
                JsonFactory.newStringFieldDefinition("connectedSince", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        public static final JsonFieldDefinition<JsonArray> CLIENT_STATUS =
                JsonFactory.newJsonArrayFieldDefinition("clientStatus", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);
        public static final JsonFieldDefinition<JsonArray> SOURCE_STATUS =
                JsonFactory.newJsonArrayFieldDefinition("sourceStatus", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);
        public static final JsonFieldDefinition<JsonArray> TARGET_STATUS =
                JsonFactory.newJsonArrayFieldDefinition("targetStatus", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);
    }

    /**
     * Builder for {@code RetrieveConnectionStatusResponse}.
     */
    @NotThreadSafe
    public static final class Builder {

        private final ConnectionId connectionId;
        private final DittoHeaders dittoHeaders;
        private ConnectivityStatus connectionStatus;
        private ConnectivityStatus liveStatus;
        @Nullable private Instant connectedSince;
        private List<ResourceStatus> clientStatus;
        private List<ResourceStatus> sourceStatus;
        private List<ResourceStatus> targetStatus;

        private Builder(final ConnectionId connectionId, final DittoHeaders dittoHeaders) {
            this.connectionId = connectionId;
            this.dittoHeaders = dittoHeaders;
        }

        public Builder connectionStatus(final ConnectivityStatus connectionStatus) {
            this.connectionStatus = checkNotNull(connectionStatus, "Connection Status");
            return this;
        }

        public Builder liveStatus(final ConnectivityStatus liveStatus) {
            this.liveStatus = checkNotNull(liveStatus, "Live Connection Status");
            return this;
        }

        public Builder connectedSince(@Nullable final Instant connectedSince) {
            this.connectedSince = connectedSince;
            return this;
        }

        public Builder clientStatus(final List<ResourceStatus> clientStatus) {
            this.clientStatus = clientStatus;
            return this;
        }

        public Builder sourceStatus(final List<ResourceStatus> sourceStatus) {
            this.sourceStatus = sourceStatus;
            return this;
        }

        public Builder targetStatus(final List<ResourceStatus> targetStatus) {
            this.targetStatus = targetStatus;
            return this;
        }

        public Builder withAddressStatus(final ResourceStatus resourceStatus) {
            final List<ResourceStatus> newClientStatus;
            final List<ResourceStatus> newSourceStatus;
            final List<ResourceStatus> newTargetStatus;
            switch (resourceStatus.getResourceType()) {
                case SOURCE:
                    newClientStatus = this.clientStatus;
                    newSourceStatus = addToList(this.sourceStatus, resourceStatus);
                    newTargetStatus = this.targetStatus;
                    break;
                case TARGET:
                    newClientStatus = this.clientStatus;
                    newSourceStatus = this.sourceStatus;
                    newTargetStatus = addToList(this.targetStatus, resourceStatus);
                    break;
                case CLIENT:
                    newClientStatus = addToList(this.clientStatus, resourceStatus);
                    newSourceStatus = this.sourceStatus;
                    newTargetStatus = this.targetStatus;
                    break;
                default:
                    newClientStatus = this.clientStatus;
                    newSourceStatus = this.sourceStatus;
                    newTargetStatus = this.targetStatus;
                    break;
            }
            this.clientStatus = newClientStatus;
            this.sourceStatus = newSourceStatus;
            this.targetStatus = newTargetStatus;
            return this;
        }

        private List<ResourceStatus> addToList(List<ResourceStatus> existing,
                final ResourceStatus resourceStatus) {
            final List<ResourceStatus> list = new ArrayList<>(existing);
            list.add(resourceStatus);
            return list;
        }

        public RetrieveConnectionStatusResponse build() {
            final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder();
            jsonObjectBuilder.set(CommandResponse.JsonFields.TYPE, TYPE);
            jsonObjectBuilder.set(CommandResponse.JsonFields.STATUS, HttpStatus.OK.getCode());
            jsonObjectBuilder.set(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID,
                    String.valueOf(connectionId));

            if (connectionStatus != null) {
                jsonObjectBuilder.set(JsonFields.CONNECTION_STATUS, connectionStatus.toString());
            }

            if (liveStatus != null) {
                jsonObjectBuilder.set(JsonFields.LIVE_STATUS, liveStatus.toString());
            }

            if (connectedSince != null) {
                jsonObjectBuilder.set(JsonFields.CONNECTED_SINCE, connectedSince.toString());
            }

            if (clientStatus != null) {
                jsonObjectBuilder.set(JsonFields.CLIENT_STATUS, clientStatus.stream()
                        .map(ResourceStatus::toJson)
                        .collect(JsonCollectors.valuesToArray()));
            }

            if (sourceStatus != null) {
                jsonObjectBuilder.set(JsonFields.SOURCE_STATUS, sourceStatus.stream()
                        .map(ResourceStatus::toJson)
                        .collect(JsonCollectors.valuesToArray()));
            }

            if (targetStatus != null) {
                jsonObjectBuilder.set(JsonFields.TARGET_STATUS, targetStatus.stream()
                        .map(ResourceStatus::toJson)
                        .collect(JsonCollectors.valuesToArray()));
            }

            return new RetrieveConnectionStatusResponse(connectionId, jsonObjectBuilder.build(), dittoHeaders);
        }
    }

}
