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
package org.eclipse.ditto.things.model.signals.commands.modify;

import static org.eclipse.ditto.things.model.signals.commands.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonKeyInvalidException;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.assertions.DittoJsonAssertions;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.json.FieldType;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.ThingCommandResponse;
import org.junit.Ignore;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link DeleteFeatureDesiredPropertyResponse}.
 */
public final class DeleteFeatureDesiredPropertyResponseTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(ThingCommandResponse.JsonFields.TYPE, DeleteFeatureDesiredPropertyResponse.TYPE)
            .set(ThingCommandResponse.JsonFields.STATUS, HttpStatus.NO_CONTENT.getCode())
            .set(ThingCommandResponse.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(DeleteFeatureDesiredPropertyResponse.JSON_FEATURE_ID, TestConstants.Feature.HOVER_BOARD_ID)
            .set(DeleteFeatureDesiredPropertyResponse.JSON_DESIRED_PROPERTY,
                    TestConstants.Feature.HOVER_BOARD_DESIRED_PROPERTY_POINTER.toString())
            .build();


    @Test
    public void assertImmutability() {
        assertInstancesOf(DeleteFeatureDesiredPropertyResponse.class,
                areImmutable(),
                provided(JsonPointer.class, ThingId.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DeleteFeatureDesiredPropertyResponse.class)
                .withRedefinedSuperclass()
                .verify();
    }

    @Test
    public void toJsonReturnsExpected() {
        final DeleteFeatureDesiredPropertyResponse underTest =
                DeleteFeatureDesiredPropertyResponse.of(TestConstants.Thing.THING_ID,
                        TestConstants.Feature.HOVER_BOARD_ID,
                        TestConstants.Feature.HOVER_BOARD_DESIRED_PROPERTY_POINTER, DittoHeaders.empty());
        final JsonObject actualJson = underTest.toJson(FieldType.regularOrSpecial());

        DittoJsonAssertions.assertThat(actualJson).isEqualTo(KNOWN_JSON);
    }

    @Test
    public void createInstanceFromValidJson() {
        final DeleteFeatureDesiredPropertyResponse underTest =
                DeleteFeatureDesiredPropertyResponse.fromJson(KNOWN_JSON, DittoHeaders.empty());

        assertThat(underTest).isNotNull();
    }

    @Test(expected = JsonKeyInvalidException.class)
    public void createInstanceWithInvalidArguments() {
        DeleteFeatureDesiredPropertyResponse.of(TestConstants.Thing.THING_ID, TestConstants.Feature.HOVER_BOARD_ID,
                TestConstants.Pointer.INVALID_JSON_POINTER, TestConstants.EMPTY_DITTO_HEADERS);
    }
}
