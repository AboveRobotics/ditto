/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.events;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyBuilder;
import org.eclipse.ditto.services.utils.persistentactors.events.EventStrategy;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

/**
 * This abstract implementation of {@code EventStrategy} checks if the Policy to be handled is {@code null}.
 * If the Policy is {@code null} the {@code handle} method returns with {@code null}; otherwise a PolicyBuilder
 * will be derived from the Policy with the revision and modified timestamp set.
 * This builder is then passed to the
 * {@link #applyEvent(T, org.eclipse.ditto.model.policies.PolicyBuilder)}
 * method for further handling.
 * However, sub-classes are free to implement the {@code handle} method directly and thus completely circumvent the
 * {@code applyEvent} method.
 *
 * @param <T> the type of the handled PolicyEvent.
 */
@Immutable
abstract class AbstractPolicyEventStrategy<T extends PolicyEvent<T>> implements EventStrategy<T, Policy> {

    /**
     * Constructs a new {@code AbstractPolicyEventStrategy} object.
     */
    protected AbstractPolicyEventStrategy() {
        super();
    }

    @Nullable
    @Override
    public Policy handle(final T event, @Nullable final Policy policy, final long revision) {
        if (null != policy) {
            PolicyBuilder policyBuilder = policy.toBuilder()
                    .setRevision(revision)
                    .setModified(event.getTimestamp().orElse(null));
            policyBuilder = applyEvent(event, policy, policyBuilder);
            return policyBuilder.build();
        }
        return null;
    }

    /**
     * Apply the specified event to the also specified PolicyBuilder. The builder has already the specified revision
     * set as well as the event's timestamp.
     *
     * @param event the ThingEvent to be applied.
     * @param policy the {@link org.eclipse.ditto.model.policies.Policy} to apply the event to.
     * @param policyBuilder builder which is derived from the {@code event}'s Policy with the revision and event
     * timestamp already set.
     * @return the updated {@code policyBuilder} after applying {@code event}.
     */
    protected PolicyBuilder applyEvent(final T event, final Policy policy, final PolicyBuilder policyBuilder) {
        return applyEvent(event, policyBuilder);
    }


    /**
     * Apply the specified event to the also specified PolicyBuilder. The builder has already the specified revision
     * set as well as the event's timestamp.
     *
     * @param event the ThingEvent to be applied.
     * @param policyBuilder builder which is derived from the {@code event}'s Policy with the revision and event
     * timestamp already set.
     * @return the updated {@code policyBuilder} after applying {@code event}.
     */
    protected PolicyBuilder applyEvent(final T event, final PolicyBuilder policyBuilder) {
        return policyBuilder;
    }
}
