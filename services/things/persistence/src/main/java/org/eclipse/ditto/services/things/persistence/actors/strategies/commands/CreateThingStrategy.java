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

import static org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory.newErrorResult;
import static org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory.newMutationResult;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.common.Validator;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.AclInvalidException;
import org.eclipse.ditto.model.things.AclNotAllowedException;
import org.eclipse.ditto.model.things.AclValidator;
import org.eclipse.ditto.model.things.PolicyIdMissingException;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingBuilder;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.CreateThingResponse;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingEvent;

/**
 * This strategy handles the {@link CreateThingStrategy} command.
 */
@Immutable
final class CreateThingStrategy extends AbstractThingCommandStrategy<CreateThing> {

    private static final CreateThingStrategy INSTANCE = new CreateThingStrategy();

    /**
     * Constructs a new {@link CreateThingStrategy} object.
     */
    private CreateThingStrategy() {
        super(CreateThing.class);
    }

    public static CreateThingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isDefined(final CreateThing command) {
        return true;
    }

    @Override
    public boolean isDefined(final Context<ThingId> context, @Nullable final Thing thing, final CreateThing command) {
        final boolean thingExists = Optional.ofNullable(thing)
                .map(t -> !t.isDeleted())
                .orElse(false);

        return !thingExists && Objects.equals(context.getState(), command.getEntityId());
    }

    @Override
    protected Result<ThingEvent<?>> doApply(final Context<ThingId> context,
            @Nullable final Thing thing,
            final long nextRevision,
            final CreateThing command,
            @Nullable final Metadata metadata) {

        final DittoHeaders commandHeaders = command.getDittoHeaders();

        // Thing not yet created - do so ..
        Thing newThing;
        try {
            newThing =
                    handleCommandVersion(context, command.getImplementedSchemaVersion(), command.getThing(),
                            commandHeaders);
        } catch (final DittoRuntimeException e) {
            return newErrorResult(e, command);
        }

        // before persisting, check if the Thing is valid and reject if not:
        final Result<ThingEvent<?>> validateThingError =
                validateThing(context, command.getImplementedSchemaVersion(), newThing, command);
        if (validateThingError != null) {
            return validateThingError;
        }

        // for v2 upwards, set the policy-id to the thing-id if none is specified:
        final boolean isV2Upwards = !JsonSchemaVersion.V_1.equals(command.getImplementedSchemaVersion());
        if (isV2Upwards && newThing.getPolicyEntityId().isEmpty()) {
            newThing = newThing.setPolicyId(PolicyId.of(context.getState()));
        }

        final Instant now = Instant.now();
        final Thing newThingWithImplicits = newThing.toBuilder()
                .setModified(now)
                .setCreated(now)
                .setRevision(nextRevision)
                .setMetadata(metadata)
                .build();
        final ThingCreated thingCreated = ThingCreated.of(newThingWithImplicits, nextRevision, now, commandHeaders,
                metadata);
        final WithDittoHeaders<?> response = appendETagHeaderIfProvided(command,
                CreateThingResponse.of(newThingWithImplicits, commandHeaders),
                newThingWithImplicits);

        return newMutationResult(command, thingCreated, response, true, false);
    }

    private Thing handleCommandVersion(final Context<ThingId> context, final JsonSchemaVersion version,
            final Thing thing,
            final DittoHeaders dittoHeaders) {

        if (JsonSchemaVersion.V_1.equals(version)) {
            return enhanceNewThingWithFallbackAcl(setLifecycleActive(thing),
                    dittoHeaders.getAuthorizationContext());
        }
        // default case handle as v2 and upwards:
        else {
            //acl is not allowed to be set in v2
            if (thing.getAccessControlList().isPresent()) {
                throw AclNotAllowedException.newBuilder(context.getState()).dittoHeaders(dittoHeaders).build();
            }

            // policyId is required for v2
            if (thing.getPolicyEntityId().isEmpty()) {
                throw PolicyIdMissingException.fromThingIdOnCreate(context.getState(), dittoHeaders);
            }

            return setLifecycleActive(thing);
        }
    }

    private static Thing setLifecycleActive(final Thing thing) {
        if (ThingLifecycle.ACTIVE.equals(thing.getLifecycle().orElse(null))) {
            return thing;
        }
        return ThingsModelFactory.newThingBuilder(thing)
                .setLifecycle(ThingLifecycle.ACTIVE)
                .build();
    }

    /**
     * Retrieves the Thing with first authorization subjects as fallback for the ACL of the Thing if the passed
     * {@code newThing} has no ACL set.
     *
     * @param newThing the new Thing to take as a "base" and to check for presence of ACL inside.
     * @param authContext the AuthorizationContext to take the first AuthorizationSubject as fallback from.
     * @return the really new Thing with guaranteed ACL.
     */
    private Thing enhanceNewThingWithFallbackAcl(final Thing newThing, final AuthorizationContext authContext) {
        final ThingBuilder.FromCopy newThingBuilder = ThingsModelFactory.newThingBuilder(newThing);

        final Boolean isAclEmpty = newThing.getAccessControlList()
                .map(AccessControlList::isEmpty)
                .orElse(true);
        if (isAclEmpty) {
            // do the fallback and use the first authorized subject and give all permissions to it:
            final AuthorizationSubject authorizationSubject = authContext.getFirstAuthorizationSubject()
                    .orElseThrow(() -> new NullPointerException("AuthorizationContext does not contain an " +
                            "AuthorizationSubject!"));
            newThingBuilder.setPermissions(authorizationSubject, Thing.MIN_REQUIRED_PERMISSIONS);
        }

        return newThingBuilder.build();
    }

    @Nullable
    private Result<ThingEvent<?>> validateThing(final Context<ThingId> context, final JsonSchemaVersion version,
            final Thing thing, final CreateThing command) {
        final DittoHeaders headers = command.getDittoHeaders();
        final Optional<AccessControlList> accessControlList = thing.getAccessControlList();
        if (JsonSchemaVersion.V_1.equals(version)) {
            if (accessControlList.isPresent()) {
                final Validator aclValidator =
                        AclValidator.newInstance(accessControlList.get(), Thing.MIN_REQUIRED_PERMISSIONS);
                // before persisting, check if the ACL is valid and reject if not:
                if (!aclValidator.isValid()) {
                    final AclInvalidException aclInvalidException =
                            AclInvalidException.newBuilder(context.getState())
                                    .dittoHeaders(headers)
                                    .build();
                    return newErrorResult(aclInvalidException, command);
                }
            } else {
                final AclInvalidException aclInvalidException =
                        AclInvalidException.newBuilder(context.getState())
                                .dittoHeaders(headers)
                                .build();
                return newErrorResult(aclInvalidException, command);
            }
        }
        return null;
    }

    @Override
    public Optional<EntityTag> previousEntityTag(final CreateThing command, @Nullable final Thing previousEntity) {
        return Optional.empty();
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final CreateThing command, @Nullable final Thing newEntity) {
        return Optional.ofNullable(newEntity).flatMap(EntityTag::fromEntity);
    }
}
