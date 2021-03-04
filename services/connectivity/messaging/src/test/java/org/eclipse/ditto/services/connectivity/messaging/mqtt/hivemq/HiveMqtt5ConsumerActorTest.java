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
package org.eclipse.ditto.services.connectivity.messaging.mqtt.hivemq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.ditto.services.connectivity.messaging.TestConstants.header;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.acks.FilteredAcknowledgementRequest;
import org.eclipse.ditto.model.base.common.DittoConstants;
import org.eclipse.ditto.model.base.common.ResponseType;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.HeaderMapping;
import org.eclipse.ditto.model.connectivity.PayloadMapping;
import org.eclipse.ditto.model.connectivity.ReplyTarget;
import org.eclipse.ditto.services.connectivity.messaging.AbstractConsumerActorTest;
import org.eclipse.ditto.services.connectivity.messaging.TestConstants;
import org.eclipse.ditto.services.connectivity.messaging.mqtt.MqttSpecificConfig;

import com.hivemq.client.internal.checkpoint.Confirmable;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

/**
 * Unit test for {@link HiveMqtt5ConsumerActor}.
 */
public final class HiveMqtt5ConsumerActorTest extends AbstractConsumerActorTest<Mqtt5Publish> {

    private static final HeaderMapping MQTT5_HEADER_MAPPING;

    static {
        final HeaderMapping baseMapping = TestConstants.HEADER_MAPPING;
        final HashMap<String, String> enhancedMapping = new HashMap<>(baseMapping.getMapping());
        enhancedMapping.put("mqtt.topic", "{{ header:mqtt.topic }}");
        enhancedMapping.put("mqtt.qos", "{{ header:mqtt.qos }}");
        enhancedMapping.put("mqtt.retain", "{{ header:mqtt.retain }}");
        MQTT5_HEADER_MAPPING = ConnectivityModelFactory.newHeaderMapping(enhancedMapping);
    }


    final CountDownLatch confirmLatch = new CountDownLatch(1);

    private static final ConnectionId CONNECTION_ID = TestConstants.createRandomConnectionId();
    private static final Connection CONNECTION = TestConstants.createConnection(CONNECTION_ID);
    private static final MqttSpecificConfig SPECIFIC_CONFIG = MqttSpecificConfig.fromConnection(CONNECTION);

    @Override
    protected Props getConsumerActorProps(final ActorRef mappingActor,
            final Set<AcknowledgementRequest> acknowledgementRequests) {
        return HiveMqtt5ConsumerActor.props(CONNECTION, mappingActor, ConnectivityModelFactory.newSourceBuilder()
                .authorizationContext(TestConstants.Authorization.AUTHORIZATION_CONTEXT)
                .enforcement(ENFORCEMENT)
                .headerMapping(MQTT5_HEADER_MAPPING)
                .acknowledgementRequests(FilteredAcknowledgementRequest.of(acknowledgementRequests, null))
                .replyTarget(ReplyTarget.newBuilder()
                        .address("foo")
                        .expectedResponseTypes(ResponseType.ERROR, ResponseType.RESPONSE, ResponseType.NACK)
                        .build())
                .build(), false, SPECIFIC_CONFIG);
    }

    @Override
    protected void testHeaderMapping() {
        testInboundMessage(header("device_id", TestConstants.Things.THING_ID), true, msg -> {
            assertThat(msg.getDittoHeaders()).containsEntry("mqtt.qos", "0");
            assertThat(msg.getDittoHeaders()).containsEntry("mqtt.topic",
                    "org.eclipse.ditto.test/testThing/things/twin/commands/modify");
            assertThat(msg.getDittoHeaders()).containsEntry("mqtt.retain", "false");
            assertThat(msg.getDittoHeaders()).containsEntry("eclipse", "ditto");
            assertThat(msg.getDittoHeaders()).containsEntry("thing_id", TestConstants.Things.THING_ID.toString());
            assertThat(msg.getDittoHeaders()).containsEntry("device_id", TestConstants.Things.THING_ID.toString());
            assertThat(msg.getDittoHeaders()).containsEntry("prefixed_thing_id",
                    "some.prefix." + TestConstants.Things.THING_ID);
            assertThat(msg.getDittoHeaders()).containsEntry("suffixed_thing_id",
                    TestConstants.Things.THING_ID + ".some.suffix");
        }, response -> fail("not expected"));
    }

    @Override
    protected Props getConsumerActorProps(final ActorRef mappingActor, final PayloadMapping payloadMapping) {
        return HiveMqtt5ConsumerActor.props(CONNECTION, mappingActor, ConnectivityModelFactory.newSourceBuilder()
                .authorizationContext(TestConstants.Authorization.AUTHORIZATION_CONTEXT)
                .enforcement(ENFORCEMENT)
                .headerMapping(MQTT5_HEADER_MAPPING)
                .payloadMapping(payloadMapping)
                .replyTarget(ReplyTarget.newBuilder()
                        .address("foo")
                        .expectedResponseTypes(ResponseType.ERROR, ResponseType.RESPONSE, ResponseType.NACK)
                        .build())
                .build(), false, SPECIFIC_CONFIG);
    }

    @Override
    protected Mqtt5Publish getInboundMessage(final String payload, final Map.Entry<String, Object> header) {
        final Mqtt5Publish mqtt5Publish = Mqtt5Publish.builder()
                .topic("org.eclipse.ditto.test/testThing/things/twin/commands/modify")
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .contentType(DittoConstants.DITTO_PROTOCOL_CONTENT_TYPE)
                .userProperties(Mqtt5UserProperties.builder()
                        .add(header.getKey(), header.getValue().toString())
                        .build())
                .responseTopic(REPLY_TO_HEADER.getValue())
                .build();
        final MqttPublish mqttPublish = (MqttPublish) mqtt5Publish;
        return mqttPublish.withConfirmable(new MockConfirmable(confirmLatch));
    }

    @Override
    protected void verifyMessageSettlement(final TestKit testKit, final boolean isSuccessExpected,
            final boolean shouldRedeliver)
            throws Exception {
        if (isSuccessExpected || !shouldRedeliver) {
            assertThat(confirmLatch.await(3L, TimeUnit.SECONDS))
                    .describedAs("Expect MQTT confirmation")
                    .isTrue();
        } else {
            testKit.expectMsg(AbstractMqttClientActor.Control.RECONNECT_CONSUMER_CLIENT);
            assertThat(confirmLatch.getCount())
                    .describedAs("Expect no confirmation to get a redelivery on reconnect")
                    .isEqualTo(1L);
        }
    }

    final static class MockConfirmable implements Confirmable {

        private final CountDownLatch confirmLatch;

        MockConfirmable(final CountDownLatch confirmLatch) {
            this.confirmLatch = confirmLatch;
        }

        @Override
        public long getId() {
            return 1234L;
        }

        @Override
        public boolean confirm() {
            confirmLatch.countDown();
            return true;
        }
    }
}
