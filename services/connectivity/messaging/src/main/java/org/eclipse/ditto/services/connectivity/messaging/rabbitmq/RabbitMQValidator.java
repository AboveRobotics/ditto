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
package org.eclipse.ditto.services.connectivity.messaging.rabbitmq;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionType;
import org.eclipse.ditto.model.connectivity.Source;
import org.eclipse.ditto.model.connectivity.Target;
import org.eclipse.ditto.model.placeholders.PlaceholderFactory;
import org.eclipse.ditto.services.connectivity.messaging.Resolvers;
import org.eclipse.ditto.services.connectivity.messaging.validation.AbstractProtocolValidator;

import akka.actor.ActorSystem;

/**
 * Connection specification for RabbitMQ protocol.
 */
@Immutable
public final class RabbitMQValidator extends AbstractProtocolValidator {

    private static final Collection<String> ACCEPTED_SCHEMES =
            Collections.unmodifiableList(Arrays.asList("amqp", "amqps"));
    private static final Collection<String> SECURE_SCHEMES = Collections.singletonList("amqps");

    /**
     * Create a new {@code RabbitMQConnectionSpec}.
     *
     * @return a new instance.
     */
    public static RabbitMQValidator newInstance() {
        return new RabbitMQValidator();
    }

    @Override
    protected void validateSource(final Source source, final DittoHeaders dittoHeaders,
            final Supplier<String> sourceDescription) {

        source.getEnforcement().ifPresent(enforcement -> {
            validateTemplate(enforcement.getInput(), dittoHeaders, PlaceholderFactory.newHeadersPlaceholder());
            enforcement.getFilters().forEach(filterTemplate ->
                    validateTemplate(filterTemplate, dittoHeaders, PlaceholderFactory.newThingPlaceholder(),
                            PlaceholderFactory.newPolicyPlaceholder(), PlaceholderFactory.newEntityPlaceholder(),
                            PlaceholderFactory.newFeaturePlaceholder()));
        });
        source.getHeaderMapping().ifPresent(mapping -> validateHeaderMapping(mapping, dittoHeaders));
    }

    @Override
    protected void validateTarget(final Target target, final DittoHeaders dittoHeaders,
            final Supplier<String> targetDescription) {
        target.getHeaderMapping().ifPresent(mapping -> validateHeaderMapping(mapping, dittoHeaders));
        validateTemplate(target.getAddress(), dittoHeaders, Resolvers.getPlaceholders());
    }

    @Override
    public ConnectionType type() {
        return ConnectionType.AMQP_091;
    }

    @Override
    public void validate(final Connection connection, final DittoHeaders dittoHeaders, final ActorSystem actorSystem) {
        validateUriScheme(connection, dittoHeaders, ACCEPTED_SCHEMES, SECURE_SCHEMES, "AMQP 0.9.1");
        validateSourceConfigs(connection, dittoHeaders);
        validateTargetConfigs(connection, dittoHeaders);
        validatePayloadMappings(connection, actorSystem, dittoHeaders);
    }
}
