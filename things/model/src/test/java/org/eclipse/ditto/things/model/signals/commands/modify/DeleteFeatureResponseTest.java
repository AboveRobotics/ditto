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
package org.eclipse.ditto.things.model.signals.commands.modify;

import static org.eclipse.ditto.things.model.signals.commands.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.assertions.DittoJsonAssertions;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.json.FieldType;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.ThingCommandResponse;
import org.junit.Ignore;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link DeleteFeatureResponse}.
 */
public final class DeleteFeatureResponseTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(ThingCommandResponse.JsonFields.TYPE, DeleteFeatureResponse.TYPE)
            .set(ThingCommandResponse.JsonFields.STATUS, HttpStatus.NO_CONTENT.getCode())
            .set(ThingCommandResponse.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(DeleteFeatureResponse.JSON_FEATURE_ID, TestConstants.Feature.FLUX_CAPACITOR_ID)
            .build();

    @Test
    public void assertImmutability() {
        assertInstancesOf(DeleteFeatureResponse.class,
                areImmutable(),
                provided(Feature.class, ThingId.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DeleteFeatureResponse.class)
                .withRedefinedSuperclass()
                .verify();
    }

    @Test
    public void toJsonReturnsExpected() {
        final DeleteFeatureResponse underTest =
                DeleteFeatureResponse.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJsonUpdated = underTest.toJson(FieldType.regularOrSpecial());

        DittoJsonAssertions.assertThat(actualJsonUpdated).isEqualTo(KNOWN_JSON);
    }

    @Test
    public void createInstanceFromValidJson() {
        final DeleteFeatureResponse underTest =
                DeleteFeatureResponse.fromJson(KNOWN_JSON, TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTest).isNotNull();
    }

}
