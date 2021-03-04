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
package org.eclipse.ditto.signals.commands.things.modify;

import static org.eclipse.ditto.signals.commands.things.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.things.TestConstants;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ModifyFeatureDesiredPropertiesResponse}.
 */
public class ModifyFeatureDesiredPropertiesResponseTest {

    private static final JsonObject KNOWN_JSON_CREATED = JsonFactory.newObjectBuilder()
            .set(ThingCommandResponse.JsonFields.TYPE, ModifyFeatureDesiredPropertiesResponse.TYPE)
            .set(ThingCommandResponse.JsonFields.STATUS, HttpStatus.CREATED.getCode())
            .set(ThingCommandResponse.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(ModifyFeatureDesiredPropertiesResponse.JSON_FEATURE_ID, TestConstants.Feature.HOVER_BOARD_ID)
            .set(ModifyFeatureDesiredPropertiesResponse.JSON_DESIRED_PROPERTIES,
                    TestConstants.Feature.HOVER_BOARD_DESIRED_PROPERTIES.toJson(FieldType.regularOrSpecial()))
            .build();

    private static final JsonObject KNOWN_JSON_UPDATED = JsonFactory.newObjectBuilder()
            .set(ThingCommandResponse.JsonFields.TYPE, ModifyFeatureDesiredPropertiesResponse.TYPE)
            .set(ThingCommandResponse.JsonFields.STATUS, HttpStatus.NO_CONTENT.getCode())
            .set(ThingCommandResponse.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(ModifyFeatureDesiredPropertiesResponse.JSON_FEATURE_ID, TestConstants.Feature.HOVER_BOARD_ID)
            .build();

    @Test
    public void assertImmutability() {
        assertInstancesOf(ModifyFeatureDesiredPropertiesResponse.class,
                areImmutable(),
                provided(FeatureProperties.class, ThingId.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ModifyFeatureDesiredPropertiesResponse.class)
                .withRedefinedSuperclass()
                .verify();
    }

    @Test
    public void toJsonReturnsExpected() {
        final ModifyFeatureDesiredPropertiesResponse underTestCreated =
                ModifyFeatureDesiredPropertiesResponse
                        .created(TestConstants.Thing.THING_ID,
                                TestConstants.Feature.HOVER_BOARD_ID,
                                TestConstants.Feature.HOVER_BOARD_DESIRED_PROPERTIES,
                                TestConstants.EMPTY_DITTO_HEADERS);

        final JsonObject actualJsonCreated = underTestCreated.toJson(FieldType.regularOrSpecial());

        assertThat(actualJsonCreated).isEqualTo(KNOWN_JSON_CREATED);

        final ModifyFeatureDesiredPropertiesResponse underTestUpdated =
                ModifyFeatureDesiredPropertiesResponse
                        .modified(TestConstants.Thing.THING_ID,
                                TestConstants.Feature.HOVER_BOARD_ID,
                                TestConstants.EMPTY_DITTO_HEADERS);

        final JsonObject actualJsonUpdated = underTestUpdated.toJson(FieldType.regularOrSpecial());

        assertThat(actualJsonUpdated).isEqualTo(KNOWN_JSON_UPDATED);
    }

    @Test
    public void createInstanceFromValidJson() {
        final ModifyFeatureDesiredPropertiesResponse underTestCreated =
                ModifyFeatureDesiredPropertiesResponse.fromJson(KNOWN_JSON_CREATED, TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTestCreated).isNotNull();
        assertThat(underTestCreated.getDesiredPropertiesCreated())
                .hasValue(TestConstants.Feature.HOVER_BOARD_DESIRED_PROPERTIES);

        final ModifyFeatureDesiredPropertiesResponse underTestUpdated =
                ModifyFeatureDesiredPropertiesResponse.fromJson(KNOWN_JSON_UPDATED, TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTestUpdated).isNotNull();
        assertThat(underTestUpdated.getDesiredPropertiesCreated()).isEmpty();
    }

}
