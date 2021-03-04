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
package org.eclipse.ditto.signals.events.connectivity;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.signals.events.base.Event;

/**
 * Interface for all {@link Connection} related events.
 *
 * @param <T> the type of the implementing class.
 */
public interface ConnectivityEvent<T extends ConnectivityEvent<T>> extends Event<T> {

    /**
     * Type Prefix of Connectivity events.
     */
    String TYPE_PREFIX = "connectivity." + TYPE_QUALIFIER + ":";

    /**
     * Connectivity resource type.
     */
    String RESOURCE_TYPE = "connectivity";

    /**
     * Returns the identifier of the modified {@code Connection}.
     *
     * @return the identifier.
     * @deprecated entity IDs are now typed. Use {@link #getConnectionEntityId()} instead.
     */
    @Deprecated
    default String getConnectionId() {
        return String.valueOf(getConnectionEntityId());
    }

    ConnectionId getConnectionEntityId();

    @Override
    default ConnectionId getEntityId() {
        return getConnectionEntityId();
    }

    @Override
    default JsonPointer getResourcePath() {
        return JsonFactory.emptyPointer();
    }

    @Override
    default String getResourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * A {@code ConnectivityEvent} doesn't have a revision. Thus this implementation always throws an {@code
     * UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    default long getRevision() {
        throw new UnsupportedOperationException("An ConnectivityEvent doesn't have a revision!");
    }

    /**
     * A {@code ConnectivityEvent} doesn't have a revision. Thus this implementation always throws an {@code
     * UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException if invoked.
     */
    @Override
    default T setRevision(final long revision) {
        throw new UnsupportedOperationException("An ConnectivityEvent doesn't have a revision!");
    }

    @Override
    T setDittoHeaders(DittoHeaders dittoHeaders);

    /**
     * An enumeration of the known {@link org.eclipse.ditto.json.JsonField}s of an event.
     */
    @Immutable
    final class JsonFields {

        /**
         * Payload JSON field containing the Connection ID.
         */
        public static final JsonFieldDefinition<String> CONNECTION_ID =
                JsonFactory.newStringFieldDefinition("connectionId", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        /**
         * Payload JSON field containing the Connection.
         */
        public static final JsonFieldDefinition<JsonObject> CONNECTION =
                JsonFactory.newJsonObjectFieldDefinition("connection", FieldType.REGULAR, JsonSchemaVersion.V_1,
                        JsonSchemaVersion.V_2);

        private JsonFields() {
            throw new AssertionError();
        }

    }

}
