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

import static org.eclipse.ditto.model.things.TestConstants.Feature.FEATURES;
import static org.eclipse.ditto.model.things.TestConstants.Feature.FEATURES_V2;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V2;
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.retrieveFeaturesResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatures;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeaturesResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link RetrieveFeaturesStrategy}.
 */
public final class RetrieveFeaturesStrategyTest extends AbstractCommandStrategyTest {

    private RetrieveFeaturesStrategy underTest;

    @Before
    public void setUp() {
        underTest = new RetrieveFeaturesStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrieveFeaturesStrategy.class, areImmutable());
    }

    @Test
    public void retrieveFeaturesWithoutSelectedFields() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeatures command = RetrieveFeatures.of(context.getState(), DittoHeaders.empty());
        final RetrieveFeaturesResponse expectedResponse = retrieveFeaturesResponse(command.getThingEntityId(), FEATURES_V2,
                FEATURES_V2.toJson(command.getImplementedSchemaVersion()), command.getDittoHeaders());

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void retrieveFeaturesWithSelectedFields() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final JsonFieldSelector selectedFields = JsonFactory.newFieldSelector("maker");
        final RetrieveFeatures command =
                RetrieveFeatures.of(context.getState(), selectedFields, DittoHeaders.empty());
        final RetrieveFeaturesResponse expectedResponse = retrieveFeaturesResponse(command.getThingEntityId(), FEATURES_V2,
                FEATURES_V2.toJson(command.getImplementedSchemaVersion(), selectedFields), command.getDittoHeaders());

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void retrieveFeaturesFromThingWithoutFeatures() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeatures command = RetrieveFeatures.of(context.getState(), DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featuresNotFound(command.getThingEntityId(), command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.removeFeatures(), command, expectedException);
    }

}
