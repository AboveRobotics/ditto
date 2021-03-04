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
package org.eclipse.ditto.services.connectivity.mapping;

import java.util.Collection;
import java.util.Map;

import org.eclipse.ditto.services.connectivity.config.mapping.MappingConfig;

/**
 * Abstract implementation of {@link MessageMapper} which adds an id field and also its initialization from mapping
 * configuration (id is not passed as constructor argument because the mappers are created by reflection).
 */
public abstract class AbstractMessageMapper implements MessageMapper {

    private String id;
    private Map<String, String> incomingConditions;
    private Map<String, String> outgoingConditions;
    private Collection<String> contentTypeBlocklist;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, String> getIncomingConditions() {
        return incomingConditions;
    }

    @Override
    public Map<String, String> getOutgoingConditions() {
        return outgoingConditions;
    }

    @Override
    public Collection<String> getContentTypeBlocklist() {
        return contentTypeBlocklist;
    }

    @Override
    public final void configure(final MappingConfig mappingConfig, final MessageMapperConfiguration configuration) {
        this.id = configuration.getId();
        this.incomingConditions = configuration.getIncomingConditions();
        this.outgoingConditions = configuration.getOutgoingConditions();
        this.contentTypeBlocklist = configuration.getContentTypeBlocklist();
        doConfigure(mappingConfig, configuration);
    }

    /**
     * Applies the mapper specific configuration.
     *
     * @param mappingConfig the service configuration for the mapping.
     * @param configuration the mapper specific configuration configured in scope of a single connection.
     */
    protected void doConfigure(final MappingConfig mappingConfig, final MessageMapperConfiguration configuration) {
        // noop default
    }
}
