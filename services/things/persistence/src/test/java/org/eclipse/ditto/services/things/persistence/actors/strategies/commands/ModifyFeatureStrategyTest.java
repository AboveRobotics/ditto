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
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.modifyFeatureResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.TestConstants;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeature;
import org.eclipse.ditto.signals.events.things.FeatureCreated;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link ModifyFeatureStrategy}.
 */
public final class ModifyFeatureStrategyTest extends AbstractCommandStrategyTest {

    private static Feature modifiedFeature;

    private ModifyFeatureStrategy underTest;

    @BeforeClass
    public static void initTestFixture() {
        modifiedFeature = TestConstants.Feature.FLUX_CAPACITOR.setProperty("speed/km", 300000);
    }

    @Before
    public void setUp() {
        underTest = new ModifyFeatureStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ModifyFeatureStrategy.class, areImmutable());
    }

    @Test
    public void modifyFeatureOnThingWithoutFeatures() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeature command = ModifyFeature.of(context.getState(), modifiedFeature, DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2.removeFeatures(), command,
                FeatureCreated.class,
                modifyFeatureResponse(context.getState(), command.getFeature(), command.getDittoHeaders(), true));
    }

    @Test
    public void modifyFeatureOnThingWithoutThatFeature() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeature command = ModifyFeature.of(context.getState(), modifiedFeature, DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2.removeFeature(modifiedFeature.getId()), command,
                FeatureCreated.class,
                modifyFeatureResponse(context.getState(), command.getFeature(), command.getDittoHeaders(), true));
    }

    @Test
    public void modifyExistingFeature() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyFeature command = ModifyFeature.of(context.getState(), modifiedFeature, DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2, command,
                FeatureModified.class,
                modifyFeatureResponse(context.getState(), command.getFeature(), command.getDittoHeaders(), false));
    }

}
