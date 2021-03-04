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
package org.eclipse.ditto.services.things.persistence.actors.strategies.commands;

import static org.eclipse.ditto.model.things.TestConstants.Feature.FLUX_CAPACITOR;
import static org.eclipse.ditto.model.things.TestConstants.Feature.FLUX_CAPACITOR_ID;
import static org.eclipse.ditto.model.things.TestConstants.Feature.FLUX_CAPACITOR_V2;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V2;
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.retrieveFeatureDesiredPropertyResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureDesiredPropertyResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link RetrieveFeatureDesiredPropertyStrategy}.
 */
public final class RetrieveFeatureDesiredPropertyStrategyTest extends AbstractCommandStrategyTest {

    private RetrieveFeatureDesiredPropertyStrategy underTest;

    @Before
    public void setUp() {
        underTest = new RetrieveFeatureDesiredPropertyStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrieveFeatureDesiredPropertyStrategy.class, areImmutable());
    }

    @Test
    public void getProperty() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final JsonPointer propertyPointer = JsonFactory.newPointer("target_year_1");
        final RetrieveFeatureDesiredProperty command =
                RetrieveFeatureDesiredProperty.of(context.getState(), FLUX_CAPACITOR_ID, propertyPointer,
                        DittoHeaders.empty());
        final RetrieveFeatureDesiredPropertyResponse expectedResponse =
                retrieveFeatureDesiredPropertyResponse(command.getThingEntityId(), command.getFeatureId(),
                        command.getDesiredPropertyPointer(), JsonFactory.newValue(1955), command.getDittoHeaders());

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void getPropertyFromThingWithoutFeatures() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeatureDesiredProperty command = RetrieveFeatureDesiredProperty.of(context.getState(), FLUX_CAPACITOR_ID,
                JsonFactory.newPointer("target_year_1"), DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureNotFound(command.getThingEntityId(), command.getFeatureId(),
                        command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.removeFeatures(), command, expectedException);
    }

    @Test
    public void getPropertyFromFeatureWithoutProperties() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final RetrieveFeatureDesiredProperty command =
                RetrieveFeatureDesiredProperty.of(context.getState(), FLUX_CAPACITOR_ID,
                        JsonFactory.newPointer("target_year_1"), DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureDesiredPropertiesNotFound(command.getThingEntityId(), command.getFeatureId(),
                        command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.setFeature(FLUX_CAPACITOR.removeDesiredProperties()), command,
                expectedException);
    }

    @Test
    public void getNonExistentProperty() {
        final JsonPointer propertyPointer = JsonFactory.newPointer("target_year_1");
        final CommandStrategy.Context<ThingId> context =
                getDefaultContext();
        final RetrieveFeatureDesiredProperty command =
                RetrieveFeatureDesiredProperty.of(context.getState(), FLUX_CAPACITOR_ID, propertyPointer,
                        DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureDesiredPropertyNotFound(command.getThingEntityId(), command.getFeatureId(),
                        command.getDesiredPropertyPointer(), command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.setFeature(FLUX_CAPACITOR_V2.removeDesiredProperty(propertyPointer)), command,
                expectedException);
    }

}
