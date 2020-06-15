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
package org.eclipse.ditto.protocoladapter.policies;

import static java.util.Objects.requireNonNull;

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.protocoladapter.QueryCommandAdapter;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.protocoladapter.adaptables.MappingStrategiesFactory;
import org.eclipse.ditto.protocoladapter.signals.SignalMapperFactory;
import org.eclipse.ditto.signals.commands.policies.query.PolicyQueryCommand;

/**
 * Adapter for mapping a {@link PolicyQueryCommand} to and from an {@link Adaptable}.
 */
final class PolicyQueryCommandAdapter extends AbstractPolicyAdapter<PolicyQueryCommand<?>>
        implements QueryCommandAdapter<PolicyQueryCommand<?>> {

    private PolicyQueryCommandAdapter(final HeaderTranslator headerTranslator) {
        super(MappingStrategiesFactory.getPolicyQueryCommandMappingStrategies(),
                SignalMapperFactory.newPolicyQuerySignalMapper(), headerTranslator);
    }

    /**
     * Returns a new PolicyQueryCommandAdapter.
     *
     * @param headerTranslator translator between external and Ditto headers.
     * @return the adapter.
     */
    public static PolicyQueryCommandAdapter of(final HeaderTranslator headerTranslator) {
        return new PolicyQueryCommandAdapter(requireNonNull(headerTranslator));
    }

    protected String getTypeCriterionAsString(final TopicPath topicPath) {
        return topicPath.getCriterion().getName();
    }
}
