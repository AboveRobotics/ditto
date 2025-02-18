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
package org.eclipse.ditto.connectivity.service.mapping;

import static java.util.Collections.singletonList;

import java.util.List;

import org.eclipse.ditto.base.model.common.DittoConstants;
import org.eclipse.ditto.base.model.exceptions.DittoJsonException;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.connectivity.api.ExternalMessageFactory;
import org.eclipse.ditto.connectivity.model.ConnectivityModelFactory;
import org.eclipse.ditto.connectivity.model.MappingContext;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.JsonifiableAdaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;

/**
 * A message mapper implementation for the Ditto Protocol.
 * Expects messages to contain a JSON serialized Ditto Protocol message.
 */
@PayloadMapper(
        alias = {"Ditto",
                // legacy full qualified name
                "org.eclipse.ditto.connectivity.service.mapping.DittoMessageMapper"})
public final class DittoMessageMapper extends AbstractMessageMapper {


    static final JsonObject DEFAULT_OPTIONS = JsonObject.newBuilder()
            .set(MessageMapperConfiguration.CONTENT_TYPE_BLOCKLIST,
                    String.join(",", "application/vnd.eclipse-hono-empty-notification",
                            "application/vnd.eclipse-hono-device-provisioning-notification",
                            "application/vnd.eclipse-hono-dc-notification+json",
                            "application/vnd.eclipse-hono-delivery-failure-notification+json"
                            ))
            .build();

    /**
     * The context representing this mapper
     */
    public static final MappingContext CONTEXT = ConnectivityModelFactory.newMappingContextBuilder(
            DittoMessageMapper.class.getCanonicalName(),
            DEFAULT_OPTIONS
    ).build();

    @Override
    public List<Adaptable> map(final ExternalMessage message) {
        final String payload = extractPayloadAsString(message);
        final JsonifiableAdaptable jsonifiableAdaptable = DittoJsonException.wrapJsonRuntimeException(() ->
                ProtocolFactory.jsonifiableAdaptableFromJson(JsonFactory.newObject(payload))
        );

        final DittoHeaders mergedHeaders = jsonifiableAdaptable.getDittoHeaders();
        return singletonList(
                ProtocolFactory.newAdaptableBuilder(jsonifiableAdaptable).withHeaders(mergedHeaders).build());
    }

    @Override
    public List<ExternalMessage> map(final Adaptable adaptable) {
        return List.of(ExternalMessageFactory.newExternalMessageBuilder(getExternalDittoHeaders(adaptable))
                .withTopicPath(adaptable.getTopicPath())
                .withText(getJsonString(adaptable))
                .asResponse(isResponse(adaptable))
                .asError(isError(adaptable))
                .build());
    }

    private static DittoHeaders getExternalDittoHeaders(final Adaptable adaptable) {
        return DittoHeaders.newBuilder()
                .contentType(DittoConstants.DITTO_PROTOCOL_CONTENT_TYPE)
                .correlationId(adaptable.getDittoHeaders().getCorrelationId().orElse(null))
                .build();
    }

    private static String getJsonString(final Adaptable adaptable) {
        final var jsonifiableAdaptable = ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable);
        return jsonifiableAdaptable.toJsonString();
    }

    @Override
    public JsonObject getDefaultOptions() {
        return DEFAULT_OPTIONS;
    }

}
