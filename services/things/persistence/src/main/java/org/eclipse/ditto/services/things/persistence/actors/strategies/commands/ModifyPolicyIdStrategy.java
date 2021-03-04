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
package org.eclipse.ditto.services.things.persistence.actors.strategies.commands;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyIdResponse;
import org.eclipse.ditto.signals.events.things.PolicyIdCreated;
import org.eclipse.ditto.signals.events.things.PolicyIdModified;
import org.eclipse.ditto.signals.events.things.ThingEvent;

/**
 * This strategy handles the {@link ModifyPolicyId} command.
 */
@Immutable
final class ModifyPolicyIdStrategy extends AbstractThingCommandStrategy<ModifyPolicyId> {

    /**
     * Constructs a new {@code ModifyPolicyIdStrategy} object.
     */
    ModifyPolicyIdStrategy() {
        super(ModifyPolicyId.class);
    }

    @Override
    protected Result<ThingEvent<?>> doApply(final Context<ThingId> context,
            @Nullable final Thing thing,
            final long nextRevision,
            final ModifyPolicyId command,
            @Nullable final Metadata metadata) {

        return extractPolicyId(thing)
                .map(policyId -> getModifyResult(context, nextRevision, command, thing, metadata))
                .orElseGet(() -> getCreateResult(context, nextRevision, command, thing, metadata));
    }

    private Optional<PolicyId> extractPolicyId(final @Nullable Thing thing) {
        return getEntityOrThrow(thing).getPolicyEntityId();
    }

    private Result<ThingEvent<?>> getModifyResult(final Context<ThingId> context, final long nextRevision,
            final ModifyPolicyId command, @Nullable final Thing thing, @Nullable final Metadata metadata) {

        final ThingId thingId = context.getState();
        final DittoHeaders dittoHeaders = command.getDittoHeaders();

        final ThingEvent<?> event =
                PolicyIdModified.of(thingId, command.getPolicyEntityId(), nextRevision, getEventTimestamp(),
                        dittoHeaders, metadata);
        final WithDittoHeaders<?> response = appendETagHeaderIfProvided(command,
                ModifyPolicyIdResponse.modified(thingId, dittoHeaders), thing);

        return ResultFactory.newMutationResult(command, event, response);
    }

    private Result<ThingEvent<?>> getCreateResult(final Context<ThingId> context, final long nextRevision,
            final ModifyPolicyId command, @Nullable final Thing thing, @Nullable final Metadata metadata) {

        final ThingId thingId = context.getState();
        final PolicyId policyId = command.getPolicyEntityId();
        final DittoHeaders dittoHeaders = command.getDittoHeaders();

        final ThingEvent<?> event =
                PolicyIdCreated.of(thingId, policyId, nextRevision, getEventTimestamp(), dittoHeaders, metadata);
        final WithDittoHeaders<?> response = appendETagHeaderIfProvided(command,
                ModifyPolicyIdResponse.created(thingId, policyId, dittoHeaders), thing);

        return ResultFactory.newMutationResult(command, event, response);
    }

    @Override
    public Optional<EntityTag> previousEntityTag(final ModifyPolicyId command, @Nullable final Thing previousEntity) {
        return extractPolicyId(previousEntity).flatMap(EntityTag::fromEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final ModifyPolicyId command, @Nullable final Thing newEntity) {
        return Optional.of(command.getPolicyEntityId()).flatMap(EntityTag::fromEntity);
    }
}
