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

import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.services.policies.common.config.PolicyConfig;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveSubjects;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveSubjectsResponse;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.query.RetrieveSubjects} command.
 */
final class RetrieveSubjectsStrategy extends AbstractPolicyQueryCommandStrategy<RetrieveSubjects> {

    RetrieveSubjectsStrategy(final PolicyConfig policyConfig) {
        super(RetrieveSubjects.class, policyConfig);
    }

    @Override
    protected Result<PolicyEvent<?>> doApply(final Context<PolicyId> context,
            @Nullable final Policy policy,
            final long nextRevision,
            final RetrieveSubjects command,
            @Nullable final Metadata metadata) {

        final Policy nonNullPolicy = checkNotNull(policy, "policy");
        final PolicyId policyId = context.getState();
        final Optional<PolicyEntry> optionalEntry = nonNullPolicy.getEntryFor(command.getLabel());
        if (optionalEntry.isPresent()) {
            final WithDittoHeaders response = appendETagHeaderIfProvided(command,
                    RetrieveSubjectsResponse.of(policyId, command.getLabel(), optionalEntry.get().getSubjects(),
                            command.getDittoHeaders()),
                    nonNullPolicy);
            return ResultFactory.newQueryResult(command, response);
        } else {
            return ResultFactory.newErrorResult(
                    policyEntryNotFound(policyId, command.getLabel(), command.getDittoHeaders()), command);
        }
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final RetrieveSubjects command, @Nullable final Policy newEntity) {
        return Optional.ofNullable(newEntity)
                .flatMap(p -> p.getEntryFor(command.getLabel()))
                .map(PolicyEntry::getSubjects)
                .flatMap(EntityTag::fromEntity);
    }
}
