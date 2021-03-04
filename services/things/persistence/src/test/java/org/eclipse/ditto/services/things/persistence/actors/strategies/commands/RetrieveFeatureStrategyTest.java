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

import static org.eclipse.ditto.model.things.TestConstants.Feature.FLUX_CAPACITOR;
import static org.eclipse.ditto.model.things.TestConstants.Feature.FLUX_CAPACITOR_ID;
import static org.eclipse.ditto.model.things.TestConstants.Feature.FLUX_CAPACITOR_V2;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V2;
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.retrieveFeatureResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeature;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link RetrieveFeatureStrategy}.
 */
public final class RetrieveFeatureStrategyTest extends AbstractCommandStrategyTest {

    private RetrieveFeatureStrategy underTest;

    @Before
    public void setUp() {
        underTest = new RetrieveFeatureStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrieveFeatureStrategy.class, areImmutable());
    }

    @Test
    public void retrieveExistingFeature() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeature command =
                RetrieveFeature.of(context.getState(), FLUX_CAPACITOR_ID, DittoHeaders.empty());
        final RetrieveFeatureResponse expectedResponse =
                retrieveFeatureResponse(command.getThingEntityId(), FLUX_CAPACITOR_V2, FLUX_CAPACITOR_V2.toJson(),
                        command.getDittoHeaders());

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void retrieveFeatureFromThingWithoutFeatures() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeature command =
                RetrieveFeature.of(context.getState(), FLUX_CAPACITOR_ID, DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureNotFound(command.getThingEntityId(), command.getFeatureId(),
                        command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.removeFeatures(), command, expectedException);
    }

    @Test
    public void retrieveNonExistingFeature() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeature command =
                RetrieveFeature.of(context.getState(), FLUX_CAPACITOR_ID, DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureNotFound(command.getThingEntityId(), command.getFeatureId(),
                        command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.removeFeature(FLUX_CAPACITOR_ID), command, expectedException);
    }

}
