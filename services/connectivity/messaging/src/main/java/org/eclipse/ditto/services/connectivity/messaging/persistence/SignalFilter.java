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
package org.eclipse.ditto.services.connectivity.messaging.persistence;

import static org.eclipse.ditto.protocoladapter.TopicPath.Criterion.COMMANDS;
import static org.eclipse.ditto.protocoladapter.TopicPath.Criterion.EVENTS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.FilteredTopic;
import org.eclipse.ditto.model.connectivity.Target;
import org.eclipse.ditto.model.connectivity.Topic;
import org.eclipse.ditto.model.namespaces.NamespaceReader;
import org.eclipse.ditto.model.query.criteria.Criteria;
import org.eclipse.ditto.model.query.filter.QueryFilterCriteriaFactory;
import org.eclipse.ditto.model.things.WithThingId;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.services.connectivity.messaging.monitoring.ConnectionMonitor;
import org.eclipse.ditto.services.connectivity.messaging.monitoring.ConnectionMonitorRegistry;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.base.WithId;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.eclipse.ditto.signals.events.things.ThingEventToThingConverter;

/**
 * Filters a set of targets by
 * <ul>
 * <li>removing those targets that do not want to receive a signal</li>
 * <li>removing those targets that are not allowed to read a signal</li>
 * </ul>
 */
final class SignalFilter {

    private final Connection connection;
    private final ConnectionMonitorRegistry<ConnectionMonitor> connectionMonitorRegistry;

    /**
     * Constructs a new SignalFilter instance with the given {@code connection}.
     *
     * @param connection the connection to filter the signals on.
     */
    SignalFilter(final Connection connection,
            final ConnectionMonitorRegistry<ConnectionMonitor> connectionMonitorRegistry) {
        this.connection = connection;
        this.connectionMonitorRegistry = connectionMonitorRegistry;
    }

    /**
     * Filters the passed {@code signal} by extracting those {@link Target}s which should receive the signal.
     * Fields are ignored if they occur as "extra targets" to be evaluated later after signal enrichment.
     *
     * @param signal the signal to filter / determine the {@link Target}s for
     * @return the determined Targets for the passed in {@code signal}
     * @throws org.eclipse.ditto.model.base.exceptions.InvalidRqlExpressionException if the optional filter string of a
     * Target cannot be mapped to a valid criterion
     */
    @SuppressWarnings("squid:S3864")
    List<Target> filter(final Signal<?> signal) {
        return connection.getTargets().stream()
                .filter(t -> isTargetAuthorized(t, signal)) // this is cheaper, so check this first
                // count authorized targets
                .peek(authorizedTarget -> connectionMonitorRegistry.forOutboundDispatched(connection.getId(),
                        authorizedTarget.getAddress())
                        .success(signal))
                .filter(t -> isTargetSubscribedForTopic(t, signal))
                // count authorized + filtered targets
                .peek(filteredTarget -> connectionMonitorRegistry.forOutboundFiltered(connection.getId(),
                        filteredTarget.getAddress())
                        .success(signal))
                .collect(Collectors.toList());
    }

    private static boolean isTargetAuthorized(final Target target, final Signal<?> signal) {
        final AuthorizationContext authorizationContext = target.getAuthorizationContext();
        final DittoHeaders headers = signal.getDittoHeaders();
        return authorizationContext.isAuthorized(headers.getReadGrantedSubjects(), headers.getReadRevokedSubjects());
    }

    private boolean isTargetSubscribedForTopic(final Target target, final Signal<?> signal) {
        return target.getTopics().stream()
                .filter(applyTopicFilter(signal))
                .filter(applyNamespaceFilter(signal))
                .anyMatch(filteredTopic -> matchesFilterBeforeEnrichment(filteredTopic, signal));
    }

    private static Predicate<FilteredTopic> applyTopicFilter(final Signal<?> signal) {
        return t -> t.getTopic().equals(topicFromSignal(signal).orElse(null));
    }

    private static Predicate<FilteredTopic> applyNamespaceFilter(final WithId signal) {
        return t -> t.getNamespaces().isEmpty() || t.getNamespaces().contains(namespaceFromId(signal));
    }

    @Nullable
    private static String namespaceFromId(final WithId withId) {
        return NamespaceReader.fromEntityId(withId.getEntityId()).orElse(null);
    }

    private boolean matchesFilterBeforeEnrichment(final FilteredTopic filteredTopic, final Signal<?> signal) {
        final Optional<String> filterOptional = filteredTopic.getFilter();
        if (filterOptional.isPresent() && signal instanceof ThingEvent) {
            // match filter ignoring "extraFields"
            return ThingEventToThingConverter.thingEventToThing((ThingEvent<?>) signal)
                    .filter(thing -> {
                        final Criteria criteria = parseCriteria(filterOptional.get(), signal.getDittoHeaders());
                        final Set<JsonPointer> extraFields = filteredTopic.getExtraFields()
                                .map(JsonFieldSelector::getPointers)
                                .orElse(Collections.emptySet());
                        return Thing3ValuePredicateVisitor.couldBeTrue(criteria, extraFields, thing);
                    })
                    .isPresent();
        } else {
            return true;
        }
    }

    /**
     * @throws org.eclipse.ditto.model.base.exceptions.InvalidRqlExpressionException if the filter string cannot be
     * mapped to a valid criterion
     */
    private Criteria parseCriteria(final String filter, final DittoHeaders dittoHeaders) {
        return QueryFilterCriteriaFactory.modelBased().filterCriteria(filter, dittoHeaders);
    }

    private static Optional<Topic> topicFromSignal(final Signal<?> signal) {
        // only things as group supported
        final TopicPath.Group group = signal instanceof WithThingId ? TopicPath.Group.THINGS : null;
        final TopicPath.Channel channel = signal.getDittoHeaders()
                .getChannel()
                .flatMap(TopicPath.Channel::forName)
                .orElse(TopicPath.Channel.TWIN);
        final TopicPath.Criterion criterion = getCriterionOfSignal(signal);

        if (TopicPath.Group.THINGS.equals(group)) {
            if (TopicPath.Channel.TWIN.equals(channel)) {
                if (EVENTS.equals(criterion)) {
                    return Optional.of(Topic.TWIN_EVENTS);
                }
            } else if (TopicPath.Channel.LIVE.equals(channel) && criterion != null) {
                switch (criterion) {
                    case COMMANDS:
                        return Optional.of(Topic.LIVE_COMMANDS);
                    case EVENTS:
                        return Optional.of(Topic.LIVE_EVENTS);
                    case MESSAGES:
                        return Optional.of(Topic.LIVE_MESSAGES);
                    default:
                        return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    @Nullable
    private static TopicPath.Criterion getCriterionOfSignal(final Signal<?> signal) {
        final TopicPath.Criterion criterion;
        if (signal instanceof MessageCommand || signal instanceof MessageCommandResponse) {
            criterion = TopicPath.Criterion.MESSAGES;
        } else if (signal instanceof Command || signal instanceof CommandResponse) {
            criterion = COMMANDS;
        } else if (signal instanceof Event) {
            criterion = EVENTS;
        } else {
            criterion = null;
        }
        return criterion;
    }

}
