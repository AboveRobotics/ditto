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
package org.eclipse.ditto.services.connectivity.messaging.mqtt.hivemq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.ditto.services.connectivity.messaging.TestConstants.header;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.acks.FilteredAcknowledgementRequest;
import org.eclipse.ditto.model.base.common.ResponseType;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.PayloadMapping;
import org.eclipse.ditto.model.connectivity.ReplyTarget;
import org.eclipse.ditto.services.connectivity.messaging.AbstractConsumerActorTest;
import org.eclipse.ditto.services.connectivity.messaging.TestConstants;
import org.eclipse.ditto.services.connectivity.messaging.mqtt.MqttSpecificConfig;
import org.junit.Ignore;

import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

/**
 * Unit test for {@link HiveMqtt3ConsumerActor}.
 */
public final class HiveMqtt3ConsumerActorTest extends AbstractConsumerActorTest<Mqtt3Publish> {

    final CountDownLatch confirmLatch = new CountDownLatch(1);

    private static final ConnectionId CONNECTION_ID = TestConstants.createRandomConnectionId();
    private static final Connection CONNECTION = TestConstants.createConnection(CONNECTION_ID);
    private static final MqttSpecificConfig SPECIFIC_CONFIG =
            MqttSpecificConfig.fromConnection(CONNECTION);

    @Override
    protected Props getConsumerActorProps(final ActorRef mappingActor,
            final Set<AcknowledgementRequest> acknowledgementRequests) {
        return HiveMqtt3ConsumerActor.props(CONNECTION, mappingActor, ConnectivityModelFactory.newSourceBuilder()
                .authorizationContext(TestConstants.Authorization.AUTHORIZATION_CONTEXT)
                .headerMapping(TestConstants.MQTT3_HEADER_MAPPING)
                .acknowledgementRequests(FilteredAcknowledgementRequest.of(acknowledgementRequests, null))
                .replyTarget(ReplyTarget.newBuilder()
                        .address("foo")
                        .expectedResponseTypes(ResponseType.ERROR, ResponseType.RESPONSE, ResponseType.NACK)
                        .build())
                .build(), false, SPECIFIC_CONFIG);
    }

    @Override
    protected Props getConsumerActorProps(final ActorRef mappingActor, final PayloadMapping payloadMapping) {
        return HiveMqtt3ConsumerActor.props(CONNECTION, mappingActor, ConnectivityModelFactory.newSourceBuilder()
                .authorizationContext(TestConstants.Authorization.AUTHORIZATION_CONTEXT)
                .headerMapping(TestConstants.MQTT3_HEADER_MAPPING)
                .payloadMapping(payloadMapping)
                .replyTarget(ReplyTarget.newBuilder()
                        .address("foo")
                        .expectedResponseTypes(ResponseType.ERROR, ResponseType.RESPONSE, ResponseType.NACK)
                        .build())
                .build(), false, SPECIFIC_CONFIG);
    }

    @Override
    protected Mqtt3Publish getInboundMessage(final String payload, final Map.Entry<String, Object> header) {
        final Mqtt3Publish mqtt3Publish = Mqtt3Publish.builder()
                .topic("org.eclipse.ditto.test/testThing/things/twin/commands/modify")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .build();
        final MqttPublish mqttPublish = ((Mqtt3PublishView) mqtt3Publish).getDelegate()
                .withConfirmable(new HiveMqtt5ConsumerActorTest.MockConfirmable(confirmLatch));
        return Mqtt3PublishView.of(mqttPublish);
    }

    @Override
    protected void verifyMessageSettlement(final TestKit testKit, final boolean isSuccessExpected,
            final boolean shouldRedeliver) throws Exception {
        if (isSuccessExpected || !shouldRedeliver) {
            assertThat(confirmLatch.await(3L, TimeUnit.SECONDS))
                    .describedAs("Expect MQTT confirmation")
                    .isTrue();
        } else {
            // NAck with redeliver -> reconnect request to client actor
            testKit.expectMsg(AbstractMqttClientActor.Control.RECONNECT_CONSUMER_CLIENT);
            assertThat(confirmLatch.getCount())
                    .describedAs("Expect no confirmation to get a redelivery on reconnect")
                    .isEqualTo(1L);
        }
    }

    @Ignore("no custom headers")
    @Override
    public void testInboundMessageWithHeaderMappingThrowsUnresolvedPlaceholderException() {
    }

    @Ignore("no custom headers")
    @Override
    public void testInboundMessageFails() {
        // not possible to test - no custom header
    }

    @Ignore("no custom headers")
    @Override
    public void testInboundMessageFailsIfHeaderIsMissing() {
    }

    @Override
    protected void testHeaderMapping() {
        testInboundMessage(header("device_id", TestConstants.Things.THING_ID), true, msg -> {
            assertThat(msg.getDittoHeaders()).containsEntry("mqtt.qos", "0");
            assertThat(msg.getDittoHeaders()).containsEntry("mqtt.topic",
                    "org.eclipse.ditto.test/testThing/things/twin/commands/modify");
            assertThat(msg.getDittoHeaders()).containsEntry("mqtt.retain", "false");

        }, response -> fail("not expected"));
    }
}
