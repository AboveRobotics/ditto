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

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAcl;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAclResponse;
import org.eclipse.ditto.signals.events.things.ThingEvent;

/**
 * This strategy handles the {@link RetrieveAcl} command.
 */
@Immutable
final class RetrieveAclStrategy extends AbstractThingCommandStrategy<RetrieveAcl> {

    /**
     * Constructs a new {@code RetrieveAclStrategy} object.
     */
    RetrieveAclStrategy() {
        super(RetrieveAcl.class);
    }

    @Override
    protected Result<ThingEvent<?>> doApply(final Context<ThingId> context,
            @Nullable final Thing thing,
            final long nextRevision,
            final RetrieveAcl command,
            @Nullable final Metadata metadata) {

        final JsonObject aclJson = extractAcl(thing)
                .map(acl -> acl.toJson(command.getImplementedSchemaVersion()))
                .orElseGet(JsonFactory::newObject);

        final WithDittoHeaders<?> response = appendETagHeaderIfProvided(command,
                RetrieveAclResponse.of(context.getState(), aclJson, command.getDittoHeaders()), thing);

        return ResultFactory.newQueryResult(command, response);
    }

    private Optional<AccessControlList> extractAcl(final @Nullable Thing thing) {
        return getEntityOrThrow(thing).getAccessControlList();
    }


    @Override
    public Optional<EntityTag> previousEntityTag(final RetrieveAcl command, @Nullable final Thing previousEntity) {
        return nextEntityTag(command, previousEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final RetrieveAcl command, @Nullable final Thing newEntity) {
        return extractAcl(newEntity).flatMap(EntityTag::fromEntity);
    }
}
