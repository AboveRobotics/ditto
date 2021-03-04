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
package org.eclipse.ditto.signals.events.things;

import static org.eclipse.ditto.json.assertions.DittoJsonAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.ThingIdInvalidException;
import org.eclipse.ditto.signals.events.base.Event;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link PolicyIdModified}
 */
public class PolicyIdModifiedTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(Event.JsonFields.TIMESTAMP, TestConstants.TIMESTAMP.toString())
            .set(Event.JsonFields.TYPE, PolicyIdModified.TYPE)
            .set(Event.JsonFields.REVISION, TestConstants.Thing.REVISION_NUMBER)
            .set(Event.JsonFields.METADATA, TestConstants.METADATA.toJson())
            .set(ThingEvent.JsonFields.THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(PolicyIdModified.JSON_POLICY_ID, TestConstants.Thing.THING_ID.toString())
            .build();


    @Test
    public void assertImmutability() {
        assertInstancesOf(PolicyIdModified.class, areImmutable(), provided(PolicyId.class).isAlsoImmutable());
    }


    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PolicyIdModified.class)
                .withRedefinedSuperclass()
                .verify();
    }

    @Test(expected = ThingIdInvalidException.class)
    public void tryToCreateInstanceWithNullThingIdString() {
        PolicyIdModified.of(null, TestConstants.Thing.THING_ID.toString(), TestConstants.Thing.REVISION_NUMBER,
                TestConstants.EMPTY_DITTO_HEADERS);
    }


    @Test(expected = NullPointerException.class)
    public void tryToCreateInstanceWithNullThingId() {
        PolicyIdModified.of(null, PolicyId.of(TestConstants.Thing.THING_ID), TestConstants.Thing.REVISION_NUMBER,
                TestConstants.EMPTY_DITTO_HEADERS);
    }


    @Test
    public void toJsonReturnsExpected() {
        final PolicyIdModified underTest =
                PolicyIdModified.of(TestConstants.Thing.THING_ID, PolicyId.of(TestConstants.Thing.THING_ID),
                        TestConstants.Thing.REVISION_NUMBER, TestConstants.TIMESTAMP,
                        TestConstants.EMPTY_DITTO_HEADERS, TestConstants.METADATA);
        final JsonObject actualJson = underTest.toJson(FieldType.regularOrSpecial());

        assertThat(actualJson).isEqualTo(KNOWN_JSON);
    }


    @Test
    public void createInstanceFromValidJson() {
        final PolicyIdModified underTest =
                PolicyIdModified.fromJson(KNOWN_JSON.toString(), TestConstants.EMPTY_DITTO_HEADERS);

        Assertions.assertThat(underTest).isNotNull();
        Assertions.assertThat(underTest.getPolicyEntityId().toString())
                .isEqualTo(TestConstants.Thing.THING_ID.toString());
    }

}
