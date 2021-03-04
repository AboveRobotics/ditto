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
package org.eclipse.ditto.signals.commands.things.query;

import static org.eclipse.ditto.signals.commands.things.TestConstants.Pointer.INVALID_JSON_POINTER;
import static org.eclipse.ditto.signals.commands.things.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonKeyInvalidException;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.things.TestConstants;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link RetrieveFeaturePropertyResponse}.
 */
public class RetrieveFeaturePropertyResponseTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(ThingCommandResponse.JsonFields.TYPE, RetrieveFeaturePropertyResponse.TYPE)
            .set(ThingCommandResponse.JsonFields.STATUS, HttpStatus.OK.getCode())
            .set(ThingCommandResponse.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(RetrieveFeaturePropertiesResponse.JSON_FEATURE_ID, TestConstants.Feature.FLUX_CAPACITOR_ID)
            .set(RetrieveFeaturePropertyResponse.JSON_PROPERTY,
                    TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER.toString())
            .set(RetrieveFeaturePropertyResponse.JSON_VALUE, TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_VALUE)
            .build();

    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrieveFeaturePropertyResponse.class, areImmutable(),
                provided(JsonPointer.class, JsonValue.class, ThingId.class).areAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveFeaturePropertyResponse.class)
                .withRedefinedSuperclass()
                .verify();
    }

    @Test(expected = NullPointerException.class)
    public void tryToCreateInstanceWithNullFeatureProperty() {
        RetrieveFeaturePropertyResponse.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER, null, TestConstants.EMPTY_DITTO_HEADERS);
    }

    @Test
    public void toJsonReturnsExpected() {
        final RetrieveFeaturePropertyResponse underTest = RetrieveFeaturePropertyResponse.of(
                TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER,
                TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_VALUE, TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJson = underTest.toJson(FieldType.regularOrSpecial());

        assertThat(actualJson).isEqualTo(KNOWN_JSON);
    }

    @Test
    public void createInstanceFromValidJson() {
        final RetrieveFeaturePropertyResponse underTest =
                RetrieveFeaturePropertyResponse.fromJson(KNOWN_JSON.toString(), TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTest).isNotNull();
        assertThat(underTest.getPropertyValue()).isEqualTo(TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_VALUE);
    }

    @Test(expected = JsonKeyInvalidException.class)
    public void tryToCreateInstanceWithInvalidArguments() {
        RetrieveFeaturePropertyResponse.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                INVALID_JSON_POINTER, TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_VALUE,
                TestConstants.EMPTY_DITTO_HEADERS);
    }

    @Test(expected = JsonKeyInvalidException.class)
    public void createInstanceFromInvalidJson() {
        final JsonObject invalidJson = KNOWN_JSON.toBuilder()
                .set(RetrieveFeatureProperty.JSON_PROPERTY_JSON_POINTER, INVALID_JSON_POINTER.toString())
                .build();
        RetrieveFeaturePropertyResponse.fromJson(invalidJson, TestConstants.EMPTY_DITTO_HEADERS);
    }
}
