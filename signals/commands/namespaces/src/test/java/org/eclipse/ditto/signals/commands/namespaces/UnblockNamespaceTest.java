/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.signals.commands.namespaces;

import static org.eclipse.ditto.signals.commands.base.assertions.CommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.UUID;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Tests {@link org.eclipse.ditto.signals.commands.namespaces.UnblockNamespace}.
 */
public final class UnblockNamespaceTest {

    private static final String NAMESPACE = "com.example.test";

    private static JsonObject knownJsonRepresentation;
    private static DittoHeaders dittoHeaders;

    private UnblockNamespace underTest;

    @BeforeClass
    public static void initTestConstants() {
        knownJsonRepresentation = JsonFactory.newObjectBuilder()
                .set(NamespaceCommand.JsonFields.TYPE, UnblockNamespace.TYPE)
                .set(NamespaceCommand.JsonFields.NAMESPACE, NAMESPACE)
                .build();

        dittoHeaders = DittoHeaders.newBuilder()
                .correlationId(String.valueOf(UUID.randomUUID()))
                .build();
    }

    @Before
    public void setUp() {
        underTest = UnblockNamespace.of(NAMESPACE, dittoHeaders);
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(UnblockNamespace.class, areImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(UnblockNamespace.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .verify();
    }

    @Test
    public void fromJsonReturnsExpected() {
        final UnblockNamespace commandFromJson = UnblockNamespace.fromJson(knownJsonRepresentation, dittoHeaders);

        assertThat(commandFromJson).isEqualTo(underTest);
    }

    @Test
    public void toJsonReturnsExpected() {
        assertThat(underTest.toJson()).isEqualTo(knownJsonRepresentation);
    }

    @Test
    public void getIdReturnsNamespace() {
        assertThat(underTest.getId()).isEqualTo(underTest.getNamespace());
    }

    @Test
    public void toStringContainsExpected() {
        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains(NAMESPACE);
    }

}
