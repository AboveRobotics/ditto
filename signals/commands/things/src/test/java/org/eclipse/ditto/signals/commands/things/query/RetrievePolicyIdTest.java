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

import static org.eclipse.ditto.signals.commands.things.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingIdInvalidException;
import org.eclipse.ditto.signals.commands.things.TestConstants;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link RetrievePolicyId}.
 */
public final class RetrievePolicyIdTest {

    private static final JsonSchemaVersion KNOWN_SCHEMA_VERSION = JsonSchemaVersion.V_2;

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(ThingCommand.JsonFields.TYPE, RetrievePolicyId.TYPE)
            .set(ThingCommand.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .build();


    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrievePolicyId.class, areImmutable(), provided(ThingId.class).isAlsoImmutable());
    }


    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrievePolicyId.class)
                .withRedefinedSuperclass()
                .verify();
    }


    @Test(expected = ThingIdInvalidException.class)
    public void tryToCreateInstanceWithNullThingIdString() {
        RetrievePolicyId.of((String) null, TestConstants.EMPTY_DITTO_HEADERS);
    }


    @Test(expected = NullPointerException.class)
    public void tryToCreateInstanceWithNullThingId() {
        RetrievePolicyId.of((ThingId) null, TestConstants.EMPTY_DITTO_HEADERS);
    }


    @Test
    public void toJsonReturnsExpected() {
        final RetrievePolicyId underTest =
                RetrievePolicyId.of(TestConstants.Thing.THING_ID, TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJson = underTest.toJson(KNOWN_SCHEMA_VERSION, FieldType.regularOrSpecial());

        assertThat(actualJson).isEqualTo(KNOWN_JSON);
    }


    @Test
    public void createInstanceFromValidJson() {
        final RetrievePolicyId underTest =
                RetrievePolicyId.fromJson(KNOWN_JSON.toString(), TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTest).isNotNull();
        assertThat((CharSequence) underTest.getEntityId()).isEqualTo(TestConstants.Thing.THING_ID);
    }

}
