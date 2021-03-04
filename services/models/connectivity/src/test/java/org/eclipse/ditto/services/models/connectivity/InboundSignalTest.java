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
package org.eclipse.ditto.services.models.connectivity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;

import java.time.Instant;
import java.util.jar.Manifest;

import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.junit.Test;
import org.mutabilitydetector.unittesting.MutabilityAssert;
import org.mutabilitydetector.unittesting.MutabilityMatchers;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import akka.serialization.Serializers;
import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Tests {@link org.eclipse.ditto.services.models.connectivity.InboundSignal}.
 */
public final class InboundSignalTest {

    @Test
    public void assertImmutability() {
        MutabilityAssert.assertInstancesOf(InboundSignal.class, MutabilityMatchers.areImmutable(),
                provided(Signal.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(InboundSignal.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void serialization() {
        ActorSystem actorSystem = null;
        try {
            actorSystem = ActorSystem.create(getClass().getSimpleName(), ConfigFactory.load("test"));
            final ThingDeleted thingDeleted = ThingDeleted.of(ThingId.of("thing:id"), 9L, Instant.now(),
                    DittoHeaders.newBuilder().randomCorrelationId().build(), null);
            final InboundSignal underTest = InboundSignal.of(thingDeleted);

            final Serialization serialization = SerializationExtension.get(actorSystem);
            final Serializer serializer = serialization.findSerializerFor(underTest);
            final String manifest = Serializers.manifestFor(serializer, underTest);
            assertThat(manifest).isEqualTo(underTest.getClass().getSimpleName());

            final byte[] bytes = serialization.serialize(underTest).get();
            final Object deserialized = serialization.deserialize(bytes, serializer.identifier(), manifest).get();
            assertThat(deserialized).isEqualTo(underTest);
        } finally {
            if (actorSystem != null) {
                actorSystem.terminate();
            }
        }
    }

}
