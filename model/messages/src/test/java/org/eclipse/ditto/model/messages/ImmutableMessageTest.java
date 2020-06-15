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
package org.eclipse.ditto.model.messages;

import static org.mutabilitydetector.unittesting.AllowedReason.assumingFields;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.nio.ByteBuffer;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.junit.Ignore;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableMessage}.
 */
public final class ImmutableMessageTest {

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableMessage.class,
                areImmutable(),
                assumingFields("payload", "rawPayload").areNotModifiedAndDoNotEscape(),
                provided(MessageHeaders.class, ByteBuffer.class, AuthorizationContext.class,
                        MessageResponseConsumer.class, JsonObject.class).areAlsoImmutable());
    }

    @Test
    @Ignore("Somehow EqualsVerifier has a problem with this class")
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableMessage.class)
                .usingGetClass()
                // actually equals() and hashCode() both use rawPayload.
                .withIgnoredFields("responseConsumer")
                .verify();
    }

}
