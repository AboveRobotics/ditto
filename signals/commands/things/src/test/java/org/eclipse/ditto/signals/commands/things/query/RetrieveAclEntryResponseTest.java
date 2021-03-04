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
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.things.TestConstants;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link RetrieveAclEntryResponse}.
 */
public class RetrieveAclEntryResponseTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(ThingCommandResponse.JsonFields.TYPE, RetrieveAclEntryResponse.TYPE)
            .set(ThingCommandResponse.JsonFields.STATUS, HttpStatus.OK.getCode())
            .set(ThingCommandResponse.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(RetrieveAclEntryResponse.JSON_ACL_ENTRY_SUBJECT,
                    TestConstants.Authorization.ACL_ENTRY_OLDMAN.getAuthorizationSubject().getId())
            .set(RetrieveAclEntryResponse.JSON_ACL_ENTRY_PERMISSIONS,
                    TestConstants.Authorization.ACL_ENTRY_OLDMAN.getPermissions().toJson())
            .build();


    @Test
    public void assertImmutability() {
        assertInstancesOf(RetrieveAclEntryResponse.class, areImmutable(),
                provided(JsonObject.class, ThingId.class).isAlsoImmutable());
    }


    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveAclEntryResponse.class)
                .withRedefinedSuperclass()
                .verify();
    }


    @Test(expected = NullPointerException.class)
    public void tryToCreateInstanceWithNullAclEntry() {
        RetrieveAclEntryResponse.of(TestConstants.Thing.THING_ID, null, TestConstants.EMPTY_DITTO_HEADERS);
    }


    @Test
    public void toJsonReturnsExpected() {
        final RetrieveAclEntryResponse underTest =
                RetrieveAclEntryResponse.of(TestConstants.Thing.THING_ID, TestConstants.Authorization.ACL_ENTRY_OLDMAN,
                        TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJson = underTest.toJson(FieldType.regularOrSpecial());

        assertThat(actualJson).isEqualTo(KNOWN_JSON);
    }


    @Test
    public void createInstanceFromValidJson() {
        final RetrieveAclEntryResponse underTest =
                RetrieveAclEntryResponse.fromJson(KNOWN_JSON.toString(), TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTest).isNotNull();
        assertThat(underTest.getAclEntry()).isEqualTo(TestConstants.Authorization.ACL_ENTRY_OLDMAN);
    }

}
