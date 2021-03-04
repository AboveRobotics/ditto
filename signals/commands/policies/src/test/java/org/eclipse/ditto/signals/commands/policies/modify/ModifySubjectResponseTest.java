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
package org.eclipse.ditto.signals.commands.policies.modify;

import static org.eclipse.ditto.json.assertions.DittoJsonAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.Subject;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandResponse;
import org.eclipse.ditto.signals.commands.policies.TestConstants;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ModifySubjectResponse}.
 */
public class ModifySubjectResponseTest {

    private static final JsonObject KNOWN_JSON_CREATED = JsonFactory.newObjectBuilder()
            .set(PolicyCommandResponse.JsonFields.TYPE, ModifySubjectResponse.TYPE)
            .set(PolicyCommandResponse.JsonFields.STATUS, HttpStatus.CREATED.getCode())
            .set(PolicyCommandResponse.JsonFields.JSON_POLICY_ID, TestConstants.Policy.POLICY_ID.toString())
            .set(ModifySubjectResponse.JSON_LABEL, TestConstants.Policy.LABEL.toString())
            .set(ModifySubjectResponse.JSON_SUBJECT_ID, TestConstants.Policy.SUBJECT.getId().toString())
            .set(ModifySubjectResponse.JSON_SUBJECT,
                    TestConstants.Policy.SUBJECT.toJson(FieldType.regularOrSpecial()))
            .build();

    private static final JsonObject KNOWN_JSON_UPDATED = JsonFactory.newObjectBuilder()
            .set(PolicyCommandResponse.JsonFields.TYPE, ModifySubjectResponse.TYPE)
            .set(PolicyCommandResponse.JsonFields.STATUS, HttpStatus.NO_CONTENT.getCode())
            .set(PolicyCommandResponse.JsonFields.JSON_POLICY_ID, TestConstants.Policy.POLICY_ID.toString())
            .set(ModifySubjectResponse.JSON_LABEL, TestConstants.Policy.LABEL.toString())
            .set(ModifySubjectResponse.JSON_SUBJECT_ID, TestConstants.Policy.SUBJECT.getId().toString())
            .build();


    @Test
    public void assertImmutability() {
        assertInstancesOf(ModifySubjectResponse.class,
                areImmutable(),
                provided(Label.class, Subject.class, PolicyId.class, SubjectId.class).areAlsoImmutable());
    }


    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ModifySubjectResponse.class)
                .withRedefinedSuperclass()
                .verify();
    }


    @Test
    public void toJsonReturnsExpected() {
        final ModifySubjectResponse underTestCreated =
                ModifySubjectResponse.created(TestConstants.Policy.POLICY_ID, TestConstants.Policy.LABEL,
                TestConstants.Policy.SUBJECT, TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJsonCreated = underTestCreated.toJson(FieldType.regularOrSpecial());

        assertThat(actualJsonCreated).isEqualTo(KNOWN_JSON_CREATED);

        final ModifySubjectResponse underTestUpdated =
                ModifySubjectResponse.modified(TestConstants.Policy.POLICY_ID, TestConstants.Policy.LABEL,
                        TestConstants.Policy.SUBJECT_ID, TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJsonUpdated = underTestUpdated.toJson(FieldType.regularOrSpecial());

        assertThat(actualJsonUpdated).isEqualTo(KNOWN_JSON_UPDATED);
    }


    @Test
    public void createInstanceFromValidJson() {
        final ModifySubjectResponse underTestCreated =
                ModifySubjectResponse.fromJson(KNOWN_JSON_CREATED, TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTestCreated).isNotNull();
        assertThat(underTestCreated.getSubjectCreated()).hasValue(TestConstants.Policy.SUBJECT);

        final ModifySubjectResponse underTestUpdated =
                ModifySubjectResponse.fromJson(KNOWN_JSON_UPDATED, TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTestUpdated).isNotNull();
        assertThat(underTestUpdated.getSubjectCreated()).isEmpty();
    }

}
