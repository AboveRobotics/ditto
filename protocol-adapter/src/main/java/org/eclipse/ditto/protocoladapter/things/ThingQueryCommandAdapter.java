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
package org.eclipse.ditto.protocoladapter.things;

import static java.util.Objects.requireNonNull;

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.protocoladapter.QueryCommandAdapter;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.protocoladapter.adaptables.MappingStrategiesFactory;
import org.eclipse.ditto.protocoladapter.signals.SignalMapper;
import org.eclipse.ditto.protocoladapter.signals.SignalMapperFactory;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThings;
import org.eclipse.ditto.signals.commands.things.query.ThingQueryCommand;

/**
 * Adapter for mapping a {@link ThingQueryCommand} to and from an {@link Adaptable}.
 */
final class ThingQueryCommandAdapter extends AbstractThingAdapter<ThingQueryCommand<?>>
        implements QueryCommandAdapter<ThingQueryCommand<?>> {

    private final SignalMapper<ThingQueryCommand<?>> thingQuerySignalMapper =
            SignalMapperFactory.newThingQuerySignalMapper();
    private final SignalMapper<RetrieveThings> retrieveThingsSignalMapper =
            SignalMapperFactory.newRetrieveThingsSignalMapper();

    private ThingQueryCommandAdapter(final HeaderTranslator headerTranslator) {
        super(MappingStrategiesFactory.getThingQueryCommandMappingStrategies(), headerTranslator);
    }

    /**
     * Returns a new ThingQueryCommandAdapter.
     *
     * @param headerTranslator translator between external and Ditto headers.
     * @return the adapter.
     */
    public static ThingQueryCommandAdapter of(final HeaderTranslator headerTranslator) {
        return new ThingQueryCommandAdapter(requireNonNull(headerTranslator));
    }

    @Override
    protected String getType(final Adaptable adaptable) {
        final TopicPath topicPath = adaptable.getTopicPath();
        if (topicPath.isWildcardTopic()) {
            return RetrieveThings.TYPE;
        } else {
            // use default for none wildcard topics
            return super.getType(adaptable);
        }
    }

    @Override
    public Adaptable mapSignalToAdaptable(final ThingQueryCommand<?> command, final TopicPath.Channel channel) {
        if (command instanceof RetrieveThings) {
            return retrieveThingsSignalMapper.mapSignalToAdaptable((RetrieveThings) command, channel);
        } else {
            return thingQuerySignalMapper.mapSignalToAdaptable(command, channel);
        }
    }
}