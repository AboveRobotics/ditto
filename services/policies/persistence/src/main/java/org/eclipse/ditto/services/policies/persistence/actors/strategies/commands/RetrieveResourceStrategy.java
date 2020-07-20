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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.commands;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.Resource;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveResource;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveResourceResponse;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.query.RetrieveResource} command.
 */
final class RetrieveResourceStrategy extends AbstractPolicyQueryCommandStrategy<RetrieveResource> {

    RetrieveResourceStrategy() {
        super(RetrieveResource.class);
    }

    @Override
    protected Result<PolicyEvent> doApply(final Context<PolicyId> context, @Nullable final Policy policy,
            final long nextRevision, final RetrieveResource command) {
        final Policy nonNullPolicy = checkNotNull(policy, "policy");
        final PolicyId policyId = context.getState();
        final Optional<PolicyEntry> optionalEntry = nonNullPolicy.getEntryFor(command.getLabel());
        if (optionalEntry.isPresent()) {
            final PolicyEntry policyEntry = optionalEntry.get();

            final Optional<Resource> optionalResource =
                    policyEntry.getResources().getResource(command.getResourceKey());
            if (optionalResource.isPresent()) {
                final RetrieveResourceResponse rawResponse =
                        RetrieveResourceResponse.of(policyId, command.getLabel(), optionalResource.get(),
                                command.getDittoHeaders());
                return ResultFactory.newQueryResult(command,
                        appendETagHeaderIfProvided(command, rawResponse, nonNullPolicy));
            } else {
                return ResultFactory.newErrorResult(
                        resourceNotFound(policyId, command.getLabel(), command.getResourceKey(),
                                command.getDittoHeaders()), command);
            }
        } else {
            return ResultFactory.newErrorResult(
                    policyEntryNotFound(policyId, command.getLabel(), command.getDittoHeaders()), command);
        }
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final RetrieveResource command, @Nullable final Policy newEntity) {
        return Optional.ofNullable(newEntity)
                .flatMap(p -> p.getEntryFor(command.getLabel()))
                .map(PolicyEntry::getResources)
                .flatMap(r -> r.getResource(command.getResourceKey()))
                .flatMap(EntityTag::fromEntity);
    }
}
