/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.connectivity.messaging;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.ditto.model.connectivity.MessageMappingFailedException;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.connectivity.config.mapping.MappingConfig;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapperConfiguration;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

final class ThrowingMapper implements MessageMapper {

    static final MessageMappingFailedException EXCEPTION =
            MessageMappingFailedException.newBuilder("text/plain")
                    .message("Expected failure.")
                    .description("For tests.")
                    .build();

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public Collection<String> getContentTypeBlocklist() {
        return List.of();
    }

    @Override
    public void configure(final MappingConfig mappingConfig, final MessageMapperConfiguration configuration) {
        // nothing to configure
    }

    @Override
    public List<Adaptable> map(final ExternalMessage message) {
        throw EXCEPTION;
    }

    @Override
    public List<ExternalMessage> map(final Adaptable adaptable) {
        throw EXCEPTION;
    }

    @Override
    public Map<String, String> getIncomingConditions() {
        return Map.of();
    }

    @Override
    public Map<String, String> getOutgoingConditions() {
        return Map.of();
    }
}
