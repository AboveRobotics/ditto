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

import static org.eclipse.ditto.model.things.TestConstants.Authorization.AUTH_SUBJECT_GRIMES;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V1;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V2;
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.retrieveAclEntryResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.TestConstants;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAclEntry;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAclEntryResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link RetrieveAclEntryStrategy}.
 */
public final class RetrieveAclEntryStrategyTest extends AbstractCommandStrategyTest {

    private RetrieveAclEntryStrategy underTest;

    @Before
    public void setUp() {
        underTest = new RetrieveAclEntryStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrieveAclEntryStrategy.class, areImmutable());
    }

    @Test
    public void retrieveAclEntryFromThingWithoutAcl() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveAclEntry command =
                RetrieveAclEntry.of(context.getState(), AUTH_SUBJECT_GRIMES, DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.aclEntryNotFound(command.getThingEntityId(), command.getAuthorizationSubject(),
                        command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2, command, expectedException);
    }

    @Test
    public void retrieveAclEntryFromThingWithoutThatAclEntry() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveAclEntry command =
                RetrieveAclEntry.of(context.getState(), AUTH_SUBJECT_GRIMES, DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.aclEntryNotFound(command.getThingEntityId(), command.getAuthorizationSubject(),
                        command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2, command, expectedException);
    }

    @Test
    public void retrieveExistingAclEntry() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveAclEntry command =
                RetrieveAclEntry.of(context.getState(), AUTH_SUBJECT_GRIMES, DittoHeaders.empty());
        final RetrieveAclEntryResponse expectedResponse =
                retrieveAclEntryResponse(command.getThingEntityId(), TestConstants.Authorization.ACL_ENTRY_GRIMES,
                        command.getDittoHeaders());

        assertQueryResult(underTest, THING_V1, command, expectedResponse);
    }

}
