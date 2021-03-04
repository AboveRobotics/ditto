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
package org.eclipse.ditto.services.models.acks;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.function.Consumer;

import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.common.ResponseType;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.services.models.acks.config.DefaultAcknowledgementConfig;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.acks.base.Acknowledgements;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayCommandTimeoutException;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

/**
 * Tests {@link org.eclipse.ditto.services.models.acks.AcknowledgementAggregatorActor}.
 */
public final class AcknowledgementAggregatorActorTest {

    private ActorSystem actorSystem;
    private HeaderTranslator headerTranslator;

    @Before
    public void init() {
        actorSystem = ActorSystem.create();
        headerTranslator = HeaderTranslator.of();
    }

    @After
    public void cleanUp() {
        if (actorSystem != null) {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }

    @Test
    public void returnSingleResponseForTwin() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String correlationId = "singleResponseTwin";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("twin-persisted");
            final DeleteThing command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1))
                    .build());
            final ActorRef underTest = childActorOf(getAcknowledgementAggregatorProps(command, this));

            // WHEN
            final DeleteThingResponse response = DeleteThingResponse.of(thingId, command.getDittoHeaders());
            underTest.tell(response, ActorRef.noSender());

            // THEN
            expectMsg(response);
        }};
    }

    @Test
    public void returnSingleResponseForLive() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String correlationId = "singleResponseLive";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("live-response");
            final DeleteThing command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1))
                    .channel("live")
                    .build());
            final ActorRef underTest = childActorOf(getAcknowledgementAggregatorProps(command, this));

            // WHEN
            final DeleteThingResponse response = DeleteThingResponse.of(thingId, command.getDittoHeaders());
            underTest.tell(response, ActorRef.noSender());

            // THEN
            expectMsg(response);
        }};
    }

    @Test
    public void returnErrorResponseForTwin() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String correlationId = "errorResponseTwin";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("twin-persisted");
            final DeleteThing command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1))
                    .timeout(Duration.ofMillis(1L))
                    .build());

            // WHEN
            childActorOf(getAcknowledgementAggregatorProps(command, this));

            // THEN
            assertThat(expectMsgClass(ThingErrorResponse.class).getDittoRuntimeException())
                    .isInstanceOf(GatewayCommandTimeoutException.class);
        }};
    }

    @Test
    public void returnErrorResponseForLive() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String correlationId = "errorResponseLive";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("live-response");
            final DeleteThing command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1))
                    .channel("live")
                    .timeout(Duration.ofMillis(1L))
                    .build());

            // WHEN
            childActorOf(getAcknowledgementAggregatorProps(command, this));

            // THEN
            assertThat(expectMsgClass(ThingErrorResponse.class).getDittoRuntimeException())
                    .isInstanceOf(GatewayCommandTimeoutException.class);
        }};
    }

    @Test
    public void keepCommandHeaders() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String tag = "tag";
            final String correlationId = "keepCommandHeaders";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("ack1");
            final AcknowledgementLabel label2 = AcknowledgementLabel.of("ack2");
            final ThingModifyCommand<?> command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2))
                    .putHeader(tag, DeleteThing.class.getSimpleName())
                    .build());
            final ActorRef underTest = childActorOf(getAcknowledgementAggregatorProps(command, this));

            // WHEN
            final Acknowledgement ack1 = Acknowledgement.of(label1, thingId, HttpStatus.UNAUTHORIZED,
                    DittoHeaders.newBuilder().correlationId(correlationId).putHeader(tag, label1.toString()).build());
            final Acknowledgement ack2 = Acknowledgement.of(label2, thingId, HttpStatus.PAYMENT_REQUIRED,
                    DittoHeaders.newBuilder().correlationId(correlationId).putHeader(tag, label2.toString()).build());
            underTest.tell(ack1, ActorRef.noSender());
            underTest.tell(ack2, ActorRef.noSender());

            // THEN
            final Acknowledgements acks = expectMsgClass(Acknowledgements.class);
            assertThat(acks.getDittoHeaders()).isEqualTo(
                    command.getDittoHeaders().toBuilder()
                            .responseRequired(false)
                            .removeHeader(DittoHeaderDefinition.REQUESTED_ACKS.getKey())
                            .build()
            );
            assertThat(acks.getSize()).isEqualTo(2);
        }};
    }

    @Test
    public void discardDuplicateAndUnsolicitedAcknowledgements() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String tag = "tag";
            final String correlationId = "discardDuplicateAndUnsolicitedAcknowledgements";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("ack1");
            final AcknowledgementLabel label2 = AcknowledgementLabel.of("ack2");
            final AcknowledgementLabel label3 = AcknowledgementLabel.of("ack3");
            final ThingModifyCommand<?> command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2))
                    .putHeader(tag, DeleteThing.class.getSimpleName())
                    .build());
            final ActorRef underTest = childActorOf(getAcknowledgementAggregatorProps(command, this));

            // WHEN
            final Acknowledgement ack1 = Acknowledgement.of(label1, thingId, HttpStatus.UNAUTHORIZED,
                    DittoHeaders.newBuilder().correlationId(correlationId).putHeader(tag, label1.toString()).build());
            final Acknowledgement ack2 = Acknowledgement.of(label2, thingId, HttpStatus.PAYMENT_REQUIRED,
                    DittoHeaders.newBuilder().correlationId(correlationId).putHeader(tag, label2.toString()).build());
            final Acknowledgement ack3 = Acknowledgement.of(label3, thingId, HttpStatus.OK,
                    DittoHeaders.newBuilder().correlationId(correlationId).putHeader(tag, "unsolicited").build());
            final Acknowledgement ack4 = Acknowledgement.of(label1, thingId, HttpStatus.UNAUTHORIZED,
                    DittoHeaders.newBuilder().correlationId(correlationId).putHeader(tag, "duplicate").build());
            underTest.tell(ack1, ActorRef.noSender());
            underTest.tell(ack3, ActorRef.noSender());
            underTest.tell(ack4, ActorRef.noSender());
            underTest.tell(ack2, ActorRef.noSender());

            // THEN
            final Acknowledgements acks = expectMsgClass(Acknowledgements.class);
            assertThat(acks.getSize()).isEqualTo(2);
            assertThat(acks.getAcknowledgement(label1)).contains(ack1);
            assertThat(acks.getAcknowledgement(label2)).contains(ack2);
        }};
    }

    @Test
    public void resetConnectivityHeadersForLive() {
        new TestKit(actorSystem) {{
            // GIVEN
            final String correlationId = "singleResponseLive";
            final ThingId thingId = ThingId.of("thing:id");
            final AcknowledgementLabel label1 = AcknowledgementLabel.of("live-response");
            final DeleteThing command = DeleteThing.of(thingId, DittoHeaders.newBuilder()
                    .correlationId(correlationId)
                    .acknowledgementRequest(AcknowledgementRequest.of(label1))
                    .channel("live")
                    .replyTarget(0)
                    .build());
            final ActorRef underTest = childActorOf(getAcknowledgementAggregatorProps(command, this));

            // WHEN
            final DeleteThingResponse response = DeleteThingResponse.of(thingId, command.getDittoHeaders()
                    .toBuilder()
                    .replyTarget(1)
                    .expectedResponseTypes(ResponseType.RESPONSE)
                    .build());
            underTest.tell(response, ActorRef.noSender());

            // THEN
            final DeleteThingResponse processedResponse = expectMsgClass(DeleteThingResponse.class);
            assertThat(processedResponse.getDittoHeaders().getReplyTarget()).contains(0);
            assertThat(processedResponse.isOfExpectedResponseType()).isFalse();
        }};
    }

    private Props getAcknowledgementAggregatorProps(final ThingModifyCommand<?> command, final TestKit testKit) {
        return AcknowledgementAggregatorActor.props(command, DefaultAcknowledgementConfig.of(ConfigFactory.empty()),
                headerTranslator, tellThis(testKit));
    }

    private static Consumer<Object> tellThis(final TestKit testKit) {
        return result -> testKit.getRef().tell(result, ActorRef.noSender());
    }
}
