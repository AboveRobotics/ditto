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
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.modifyAttributeResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link ModifyAttributeStrategy}.
 */
public final class ModifyAttributeStrategyTest extends AbstractCommandStrategyTest {

    private static JsonPointer attributePointer;
    private static JsonValue attributeValue;

    private ModifyAttributeStrategy underTest;

    @BeforeClass
    public static void initTestFixture() {
        attributePointer = JsonFactory.newPointer("/foo/bar");
        attributeValue = JsonFactory.newValue("baz");
    }

    @Before
    public void setUp() {
        underTest = new ModifyAttributeStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ModifyAttributeStrategy.class, areImmutable());
    }

    @Test
    public void modifyAttributeOfThingWithoutAttributes() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyAttribute command =
                ModifyAttribute.of(context.getState(), attributePointer, attributeValue, DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2.removeAttributes(), command,
                AttributeCreated.class,
                modifyAttributeResponse(context.getState(), attributePointer, attributeValue,
                        command.getDittoHeaders(), true));
    }

    @Test
    public void modifyAttributeOfThingWithoutThatAttribute() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyAttribute command =
                ModifyAttribute.of(context.getState(), attributePointer, attributeValue, DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2, command,
                AttributeCreated.class,
                modifyAttributeResponse(context.getState(), attributePointer, attributeValue,
                        command.getDittoHeaders(), true));
    }

    @Test
    public void modifyExistingAttribute() {
        final JsonPointer existingAttributePointer = JsonFactory.newPointer("/location/latitude");
        final JsonValue newAttributeValue = JsonFactory.newValue(42.0D);

        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final ModifyAttribute command =
                ModifyAttribute.of(context.getState(), existingAttributePointer, newAttributeValue,
                        DittoHeaders.empty());

        assertModificationResult(underTest, THING_V2, command,
                AttributeModified.class,
                modifyAttributeResponse(context.getState(), existingAttributePointer, newAttributeValue,
                        command.getDittoHeaders(), false));
    }

}
