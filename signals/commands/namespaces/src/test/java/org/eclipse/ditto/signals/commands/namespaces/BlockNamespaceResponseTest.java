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
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.signals.commands.namespaces.BlockNamespaceResponse}.
 */
public final class BlockNamespaceResponseTest {

    private static final String NAMESPACE = "com.example.test";
    private static final String RESOURCE_TYPE = "policy";

    private static JsonObject knownJsonRepresentation;
    private static DittoHeaders dittoHeaders;

    @BeforeClass
    public static void initTestConstants() {
        knownJsonRepresentation = JsonFactory.newObjectBuilder()
                .set(NamespaceCommandResponse.JsonFields.TYPE, BlockNamespaceResponse.TYPE)
                .set(NamespaceCommandResponse.JsonFields.STATUS, HttpStatusCode.OK.toInt())
                .set(NamespaceCommandResponse.JsonFields.NAMESPACE, NAMESPACE)
                .set(NamespaceCommandResponse.JsonFields.RESOURCE_TYPE, RESOURCE_TYPE)
                .build();

        dittoHeaders = DittoHeaders.newBuilder()
                .correlationId(String.valueOf(UUID.randomUUID()))
                .build();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(BlockNamespaceResponse.class, areImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(BlockNamespaceResponse.class)
                .withRedefinedSuperclass()
                .usingGetClass()
                .verify();
    }

    @Test
    public void fromJsonReturnsExpected() {
        final BlockNamespaceResponse responseFromJson =
                BlockNamespaceResponse.fromJson(knownJsonRepresentation, dittoHeaders);

        assertThat(responseFromJson).isEqualTo(
                BlockNamespaceResponse.getInstance(NAMESPACE, RESOURCE_TYPE, dittoHeaders));
    }

    @Test
    public void toJsonReturnsExpected() {
        final BlockNamespaceResponse underTest =
                BlockNamespaceResponse.getInstance(NAMESPACE, RESOURCE_TYPE, dittoHeaders);

        assertThat(underTest.toJson()).isEqualTo(knownJsonRepresentation);
    }

    @Test
    public void getIdReturnsNamespace() {
        final BlockNamespaceResponse underTest =
                BlockNamespaceResponse.getInstance(NAMESPACE, RESOURCE_TYPE, dittoHeaders);

        assertThat(underTest.getId()).isEqualTo(underTest.getNamespace());
    }

    @Test
    public void toStringContainsExpected() {
        final BlockNamespaceResponse underTest =
                BlockNamespaceResponse.getInstance(NAMESPACE, RESOURCE_TYPE, dittoHeaders);

        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains(NAMESPACE)
                .contains(RESOURCE_TYPE);
    }

}