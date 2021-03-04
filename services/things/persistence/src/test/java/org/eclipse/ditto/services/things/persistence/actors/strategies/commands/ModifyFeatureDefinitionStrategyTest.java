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

import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V2;
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.modifyFeatureDefinitionResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.TestConstants;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionModified;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link ModifyFeatureDefinitionStrategy}.
 */
public final class ModifyFeatureDefinitionStrategyTest extends AbstractCommandStrategyTest {

    private static String featureId;
    private static FeatureDefinition modifiedFeatureDefinition;

    private ModifyFeatureDefinitionStrategy underTest;

    @BeforeClass
    public static void initTestFixture() {
        featureId = TestConstants.Feature.FLUX_CAPACITOR_ID;
        modifiedFeatureDefinition = FeatureDefinition.fromIdentifier("org.example:my-feature:23.42.1337");
    }

    @Before
    public void setUp() {
        underTest = new ModifyFeatureDefinitionStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ModifyFeatureDefinitionStrategy.class, areImmutable());
    }

    @Test
    public void modifyFeatureDefinitionOfThingWithoutFeatures() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeatureDefinition command =
                ModifyFeatureDefinition.of(context.getState(), featureId, modifiedFeatureDefinition,
                        DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureNotFound(context.getState(), featureId, command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.removeFeatures(), command, expectedException);
    }

    @Test
    public void modifyFeatureDefinitionOfThingWithoutThatFeature() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeatureDefinition command =
                ModifyFeatureDefinition.of(context.getState(), featureId, modifiedFeatureDefinition,
                        DittoHeaders.empty());
        final DittoRuntimeException expectedException =
                ExceptionFactory.featureNotFound(context.getState(), featureId, command.getDittoHeaders());

        assertErrorResult(underTest, THING_V2.removeFeature(featureId), command, expectedException);
    }

    @Test
    public void modifyFeatureDefinitionOfFeatureWithoutDefinition() {
        final Feature featureWithoutDefinition = TestConstants.Feature.FLUX_CAPACITOR.removeDefinition();
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeatureDefinition command =
                ModifyFeatureDefinition.of(context.getState(), featureId, modifiedFeatureDefinition,
                        DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2.setFeature(featureWithoutDefinition), command,
                FeatureDefinitionCreated.class,
                modifyFeatureDefinitionResponse(context.getState(), featureId, command.getDefinition(),
                        command.getDittoHeaders(), true));
    }

    @Test
    public void modifyExistingFeatureDefinition() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeatureDefinition command =
                ModifyFeatureDefinition.of(context.getState(), featureId, modifiedFeatureDefinition,
                        DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2, command,
                FeatureDefinitionModified.class,
                modifyFeatureDefinitionResponse(context.getState(), featureId, modifiedFeatureDefinition,
                        command.getDittoHeaders(), false));
    }

}
