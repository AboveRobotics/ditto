/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.services.connectivity.messaging.rabbitmq;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionType;
import org.eclipse.ditto.services.connectivity.messaging.validation.AbstractProtocolValidator;

/**
 * Connection specification for RabbitMQ protocol.
 */
@Immutable
public final class RabbitMQValidator extends AbstractProtocolValidator {

    private static final Collection<String> ACCEPTED_SCHEMES =
            Collections.unmodifiableList(Arrays.asList("amqp", "amqps"));

    /**
     * Create a new {@code RabbitMQConnectionSpec}.
     *
     * @return a new instance.
     */
    public static RabbitMQValidator newInstance() {
        return new RabbitMQValidator();
    }

    @Override
    public ConnectionType type() {
        return ConnectionType.AMQP_091;
    }

    @Override
    public void validate(final Connection connection, final DittoHeaders dittoHeaders) throws DittoRuntimeException {
        AbstractProtocolValidator.validateUriScheme(connection, dittoHeaders, ACCEPTED_SCHEMES, "AMQP 0.9.1");
    }
}
