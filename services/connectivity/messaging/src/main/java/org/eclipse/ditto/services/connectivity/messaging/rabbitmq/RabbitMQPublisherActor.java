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
package org.eclipse.ditto.services.connectivity.messaging.rabbitmq;

import static org.eclipse.ditto.services.connectivity.messaging.validation.ConnectionValidator.resolveConnectionIdPlaceholder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.common.CharsetDeterminer;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.ConnectivityStatus;
import org.eclipse.ditto.model.connectivity.MessageSendingFailedException;
import org.eclipse.ditto.model.connectivity.ResourceStatus;
import org.eclipse.ditto.model.connectivity.Target;
import org.eclipse.ditto.model.placeholders.ExpressionResolver;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.connectivity.config.DittoConnectivityConfig;
import org.eclipse.ditto.services.connectivity.messaging.BasePublisherActor;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.eclipse.ditto.services.utils.config.DefaultScopedConfig;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.base.CommandResponse;

import com.newmotion.akka.rabbitmq.ChannelCreated;
import com.newmotion.akka.rabbitmq.ChannelMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.ReturnListener;
import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Responsible for publishing {@link ExternalMessage}s into RabbitMQ / AMQP 0.9.1.
 * <p>
 * To receive responses the {@code replyTo} header must be set. Responses are sent to the default exchange with the
 * {@code replyTo} header as routing key.
 * </p>
 * The {@code address} of the {@code targets} from the {@link Connection} are interpreted as follows:
 * <ul>
 * <li>no {@code targets} defined: signals are not published at all</li>
 * <li>{@code address="target/routingKey"}: signals are published to exchange {@code target} with routing key {@code
 * routingKey}</li>
 * </ul>
 */
public final class RabbitMQPublisherActor extends BasePublisherActor<RabbitMQTarget> {

    /**
     * The name of this Actor in the ActorSystem.
     */
    static final String ACTOR_NAME = "rmqPublisherActor";

    private static final AcknowledgementLabel NO_ACK_LABEL = AcknowledgementLabel.of("ditto-rabbitmq-diagnostic");

    /**
     * Lifetime of an entry in the cache 'outstandingAcks'.
     */
    private final Duration pendingAckTTL;

    private final ConcurrentSkipListMap<Long, OutstandingAck> outstandingAcks = new ConcurrentSkipListMap<>();
    private final ConcurrentHashMap<RabbitMQTarget, Queue<OutstandingAck>> outstandingAcksByTarget =
            new ConcurrentHashMap<>();
    private ConfirmMode confirmMode = ConfirmMode.UNKNOWN;
    @Nullable private ActorRef channelActor;

    @SuppressWarnings("unused")
    private RabbitMQPublisherActor(final Connection connection, final String clientId) {
        super(connection, clientId);
        final Config config = getContext().getSystem().settings().config();
        pendingAckTTL = DittoConnectivityConfig.of(DefaultScopedConfig.dittoScoped(config))
                .getConnectionConfig()
                .getAmqp091Config()
                .getPublisherPendingAckTTL();
    }

    /**
     * Creates Akka configuration object {@link Props} for this {@code RabbitMQPublisherActor}.
     *
     * @param connection the connection this publisher belongs to
     * @param clientId identifier of the client actor.
     * @return the Akka configuration Props object.
     */
    static Props props(final Connection connection, final String clientId) {

        return Props.create(RabbitMQPublisherActor.class, connection, clientId);
    }

    @Override
    protected boolean shouldPublishAcknowledgement(final Acknowledgement acknowledgement) {
        return !NO_ACK_LABEL.equals(acknowledgement.getLabel());
    }

    @Override
    protected void preEnhancement(final ReceiveBuilder receiveBuilder) {
        receiveBuilder
                .match(ChannelCreated.class, channelCreated -> {
                    channelActor = channelCreated.channel();
                    final ChannelMessage channelMessage = ChannelMessage.apply(this::onChannelCreated, false);
                    channelCreated.channel().tell(channelMessage, getSelf());
                })
                .match(ChannelStatus.class, this::handleChannelStatus)
                .build();
    }

    @Override
    protected void postEnhancement(final ReceiveBuilder receiveBuilder) {
        // noop
    }

    @Override
    protected RabbitMQTarget toPublishTarget(final String address) {
        return RabbitMQTarget.fromTargetAddress(address);
    }

    @Override
    protected CompletionStage<CommandResponse<?>> publishMessage(final Signal<?> signal,
            @Nullable final Target autoAckTarget,
            final RabbitMQTarget publishTarget,
            final ExternalMessage message,
            final int maxTotalMessageSize,
            final int ackSizeQuota) {

        if (channelActor == null) {
            return sendFailedFuture(signal, "No channel available, dropping response.");
        }

        if (publishTarget.getRoutingKey() == null) {
            return sendFailedFuture(signal, "No routing key, dropping message.");
        }

        final Map<String, String> messageHeaders = message.getHeaders();
        final String contentType = messageHeaders.get(ExternalMessage.CONTENT_TYPE_HEADER);
        final String correlationId = messageHeaders.get(DittoHeaderDefinition.CORRELATION_ID.getKey());

        final Map<String, Object> stringObjectMap = messageHeaders.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue()));

        final AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .contentType(contentType)
                .correlationId(correlationId)
                .headers(stringObjectMap)
                .build();

        final byte[] body;
        if (message.isTextMessage()) {
            body = message.getTextPayload()
                    .map(text -> text.getBytes(CharsetDeterminer.getInstance().apply(contentType)))
                    .orElseThrow(() -> new IllegalArgumentException("Failed to convert text to bytes."));
        } else {
            body = message.getBytePayload()
                    .map(ByteBuffer::array)
                    .orElse(new byte[]{});
        }

        final CompletableFuture<CommandResponse<?>> resultFuture = new CompletableFuture<>();
        // create consumer outside channel message: need to check actor state and decide whether to handle acks.
        final LongConsumer nextPublishSeqNoConsumer =
                computeNextPublishSeqNoConsumer(signal, autoAckTarget, publishTarget, resultFuture);
        final ChannelMessage channelMessage = ChannelMessage.apply(channel -> {
            try {
                logger.withCorrelationId(message.getInternalHeaders())
                        .debug("Publishing to exchange <{}> and routing key <{}>: {}", publishTarget.getExchange(),
                                publishTarget.getRoutingKey(), basicProperties);
                nextPublishSeqNoConsumer.accept(channel.getNextPublishSeqNo());
                channel.basicPublish(publishTarget.getExchange(), publishTarget.getRoutingKey(), true, basicProperties,
                        body);
            } catch (final Exception e) {
                final String errorMessage = String.format("Failed to publish message to RabbitMQ: %s", e.getMessage());
                resultFuture.completeExceptionally(sendFailed(signal, errorMessage, e));
            }
            return null;
        }, false);

        channelActor.tell(channelMessage, getSelf());
        return resultFuture;
    }

    // This method is NOT thread-safe, but its returned consumer MUST be thread-safe.
    private LongConsumer computeNextPublishSeqNoConsumer(final Signal<?> signal,
            @Nullable final Target autoAckTarget,
            final RabbitMQTarget publishTarget,
            final CompletableFuture<CommandResponse<?>> resultFuture) {

        if (confirmMode == ConfirmMode.ACTIVE) {
            return seqNo -> addOutstandingAck(seqNo, signal, resultFuture, autoAckTarget, publishTarget, pendingAckTTL);
        } else {
            final Acknowledgement unsupportedAck = getUnsupportedAck(signal, autoAckTarget, connectionIdResolver);
            return seqNo -> resultFuture.complete(unsupportedAck);
        }
    }

    // Thread-safe
    private void addOutstandingAck(final Long seqNo,
            final Signal<?> signal,
            final CompletableFuture<CommandResponse<?>> resultFuture,
            @Nullable final Target autoAckTarget,
            final RabbitMQTarget publishTarget,
            final Duration timeoutDuration) {

        final OutstandingAck outstandingAck = new OutstandingAck(signal, autoAckTarget, resultFuture,
                connectionIdResolver);
        final Acknowledgement timeoutAck = getTimeoutAck(signal, autoAckTarget, connectionIdResolver);

        // index the outstanding ack by delivery tag
        outstandingAcks.put(seqNo, outstandingAck);
        resultFuture.completeOnTimeout(timeoutAck,
                timeoutDuration.toMillis(), TimeUnit.MILLISECONDS)
                // Only remove future from cache. Actual logging/reporting done elsewhere.
                .whenComplete((ack, error) -> outstandingAcks.remove(seqNo));

        // index the outstanding ack by publish target in order to generate negative acks on basic.return messages
        outstandingAcksByTarget.compute(publishTarget, (key, queue) -> {
            final Queue<OutstandingAck> result = queue != null ? queue : new ConcurrentLinkedQueue<>();
            result.offer(outstandingAck);
            return result;
        });
        // maintain outstanding-acks-by-target. It need not be accurate because outstandingAcksByTarget is only used
        // on basic.return, which affects all messages published to 1 target but is not precise.
        resultFuture.whenComplete(
                (ignoredAck, ignoredError) -> outstandingAcksByTarget.computeIfPresent(publishTarget, (key, queue) -> {
                    queue.poll();
                    return queue.isEmpty() ? null : queue;
                }));
    }

    private void handleChannelStatus(final ChannelStatus channelStatus) {
        if (channelStatus.confirmationException != null) {
            logger.error(channelStatus.confirmationException, "Failed to enter confirm mode.");
            confirmMode = ConfirmMode.INACTIVE;
        } else {
            confirmMode = ConfirmMode.ACTIVE;
        }
        resourceStatusMap.putAll(channelStatus.targetStatus);
    }

    // called by ChannelActor; must be thread-safe.
    private Void onChannelCreated(final Channel channel) {
        final IOException confirmationStatus =
                tryToEnterConfirmationMode(channel, outstandingAcks, outstandingAcksByTarget).orElse(null);
        final Map<Target, ResourceStatus> targetStatus =
                declareExchangesPassive(channel, this::toPublishTarget);
        getSelf().tell(new ChannelStatus(confirmationStatus, targetStatus), ActorRef.noSender());
        return null;
    }

    private static Acknowledgement getTimeoutAck(final Signal<?> signal, @Nullable final Target autoAckTarget,
            final ExpressionResolver connectionIdResolver) {
        return buildAcknowledgement(signal, autoAckTarget, HttpStatus.REQUEST_TIMEOUT,
                "No publisher confirm arrived.", connectionIdResolver);
    }

    private static Acknowledgement getUnsupportedAck(final Signal<?> signal, @Nullable final Target autoAckTarget,
            final ExpressionResolver connectionIdResolver) {
        if (autoAckTarget != null && autoAckTarget.getIssuedAcknowledgementLabel().isPresent()) {
            // Not possible to recover without broker upgrade. Use status 400 to prevent redelivery at the source.
            return buildAcknowledgement(signal, autoAckTarget, HttpStatus.BAD_REQUEST,
                    "The external broker does not support RabbitMQ publisher confirms. " +
                            "Acknowledgement is not possible.", connectionIdResolver);
        } else {
            return getSuccessAck(signal, autoAckTarget, connectionIdResolver);
        }
    }

    private static Acknowledgement getSuccessAck(final Signal<?> signal, @Nullable final Target autoAckTarget,
            final ExpressionResolver connectionIdResolver) {
        return buildAcknowledgement(signal, autoAckTarget, HttpStatus.OK, null, connectionIdResolver);
    }

    private static Acknowledgement buildAcknowledgement(final Signal<?> signal,
            @Nullable final Target autoAckTarget,
            final HttpStatus httpStatus,
            @Nullable final String message,
            final ExpressionResolver connectionIdResolver) {

        final var label = Optional.ofNullable(autoAckTarget)
                .flatMap(Target::getIssuedAcknowledgementLabel)
                .flatMap(ackLabel -> resolveConnectionIdPlaceholder(connectionIdResolver, ackLabel))
                .orElse(NO_ACK_LABEL);
        return Acknowledgement.of(label,
                ThingId.of(signal.getEntityId()),
                httpStatus,
                signal.getDittoHeaders(),
                message == null ? null : JsonValue.of(message));
    }

    private Map<Target, ResourceStatus> declareExchangesPassive(final Channel channel,
            final Function<String, RabbitMQTarget> toPublishTarget) {

        final List<Target> targets = connection.getTargets();
        final Map<String, Target> exchanges = targets.stream()
                .collect(Collectors.toMap(
                        t -> toPublishTarget.apply(t.getAddress()).getExchange(),
                        Function.identity()));
        final Map<Target, ResourceStatus> declarationStatus = new HashMap<>();
        exchanges.forEach((exchange, target) -> {
            logger.debug("Checking for existence of exchange <{}>.", exchange);
            try {
                channel.exchangeDeclarePassive(exchange);
            } catch (final IOException e) {
                logger.warning("Failed to declare exchange <{}> passively.", exchange);
                if (target != null) {
                    declarationStatus.put(target,
                            ConnectivityModelFactory.newTargetStatus(
                                    getClientId(),
                                    ConnectivityStatus.FAILED,
                                    target.getAddress(),
                                    "Exchange '" + exchange + "' was missing at " + Instant.now()));
                }
            }
        });
        return Collections.unmodifiableMap(declarationStatus);
    }

    private static Optional<IOException> tryToEnterConfirmationMode(final Channel channel,
            final ConcurrentSkipListMap<Long, OutstandingAck> outstandingAcks,
            final ConcurrentHashMap<RabbitMQTarget, Queue<OutstandingAck>> outstandingAcksByTarget) {

        try {
            enterConfirmationMode(channel, outstandingAcks, outstandingAcksByTarget);
            return Optional.empty();
        } catch (final IOException e) {
            return Optional.of(e);
        }
    }

    private static void enterConfirmationMode(final Channel channel,
            final ConcurrentSkipListMap<Long, OutstandingAck> outstandingAcks,
            final ConcurrentHashMap<RabbitMQTarget, Queue<OutstandingAck>> outstandingAcksByTarget) throws IOException {

        channel.confirmSelect();
        channel.clearConfirmListeners();
        channel.clearReturnListeners();
        final ActorConfirmListener confirmListener =
                new ActorConfirmListener(outstandingAcks, outstandingAcksByTarget);
        channel.addConfirmListener(confirmListener);
        channel.addReturnListener(confirmListener);
    }

    private static <T> CompletionStage<T> sendFailedFuture(final Signal<?> signal, final String errorMessage) {
        return CompletableFuture.failedFuture(sendFailed(signal, errorMessage, null));
    }

    private static MessageSendingFailedException sendFailed(final Signal<?> signal, final String errorMessage,
            @Nullable final Throwable cause) {
        return MessageSendingFailedException.newBuilder()
                .message(errorMessage)
                .dittoHeaders(signal.getDittoHeaders())
                .cause(cause)
                .build();
    }

    private static final class ChannelStatus {

        @Nullable private final IOException confirmationException;
        private final Map<Target, ResourceStatus> targetStatus;

        private ChannelStatus(@Nullable final IOException confirmationException,
                final Map<Target, ResourceStatus> targetStatus) {
            this.confirmationException = confirmationException;
            this.targetStatus = targetStatus;
        }
    }

    private static final class ActorConfirmListener implements ConfirmListener, ReturnListener {

        private final ConcurrentSkipListMap<Long, OutstandingAck> outstandingAcks;
        private final ConcurrentHashMap<RabbitMQTarget, Queue<OutstandingAck>> outstandingAcksByTarget;

        private ActorConfirmListener(final ConcurrentSkipListMap<Long, OutstandingAck> outstandingAcks,
                final ConcurrentHashMap<RabbitMQTarget, Queue<OutstandingAck>> outstandingAcksByTarget) {
            this.outstandingAcks = outstandingAcks;
            this.outstandingAcksByTarget = outstandingAcksByTarget;
        }

        /**
         * Handle ACK from the broker. ACK messages are always sent after a RETURN message.
         * The channel actor runs this listener in its thread, which guarantees that this.handleAck is always
         * called after this.handleReturn returns.
         *
         * @param deliveryTag The delivery tag of the acknowledged message.
         * @param multiple Whether this acknowledgement applies to multiple messages.
         */
        @Override
        public void handleAck(final long deliveryTag, final boolean multiple) {
            forEach(deliveryTag, multiple, OutstandingAck::completeWithSuccess);
        }

        /**
         * Handle NACK from the broker. NACK messages are always handled after this.handleReturn returns.
         *
         * @param deliveryTag The delivery tag of the acknowledged message.
         * @param multiple Whether this acknowledgement applies to multiple messages.
         */
        @Override
        public void handleNack(final long deliveryTag, final boolean multiple) {
            forEach(deliveryTag, multiple, OutstandingAck::completeWithFailure);
        }

        /**
         * Handle RETURN from the broker. RETURN messages are sent if a routing key does not exist for an existing
         * exchange. Since users can set publish target for each message by the means of placeholders, and because
         * queues can be created and deleted at any time, getting RETURN messages does not imply failure of the whole
         * channel.
         * <p>
         * Here all outstanding acks of an exchange, routing-key pair are completed exceptionally with the reply text
         * and reply code.
         *
         * @param replyCode reply code of this RETURN message.
         * @param replyText textual description of this RETURN message.
         * @param exchange exchange of the outgoing message being returned.
         * @param routingKey routing key of the outgoing message being returned.
         * @param properties AMQP properties of the RETURN message; typically empty regardless of the properties of the
         * outgoing message being returned.
         * @param body body of the returned message.
         */
        @Override
        public void handleReturn(final int replyCode, final String replyText, final String exchange,
                final String routingKey,
                final AMQP.BasicProperties properties, final byte[] body) {

            final RabbitMQTarget rabbitMQTarget = RabbitMQTarget.of(exchange, routingKey);
            final Queue<OutstandingAck> queue = outstandingAcksByTarget.get(rabbitMQTarget);
            // cleanup handled in another thread on completion of each outstanding ack
            if (queue != null) {
                queue.forEach(outstandingAck -> outstandingAck.completeForReturn(replyCode, replyText));
            }
        }

        private void forEach(final long deliveryTag, final boolean multiple,
                final Consumer<OutstandingAck> outstandingAckConsumer) {
            if (multiple) {
                // iterator is in key order because each ConcurrentSkipListMap is a SortedMap
                final Iterator<Map.Entry<Long, OutstandingAck>> it = outstandingAcks.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<Long, OutstandingAck> entry = it.next();
                    if (entry.getKey() > deliveryTag) {
                        break;
                    }
                    outstandingAckConsumer.accept(entry.getValue());
                    it.remove();
                }
            } else {
                final OutstandingAck outstandingAck = outstandingAcks.get(deliveryTag);
                if (outstandingAck != null) {
                    outstandingAckConsumer.accept(outstandingAck);
                    outstandingAcks.remove(deliveryTag);
                }
            }
        }
    }

    private static final class OutstandingAck {

        private final Signal<?> signal;
        @Nullable private final Target autoAckTarget;
        private final CompletableFuture<CommandResponse<?>> future;
        private final ExpressionResolver connectionIdResolver;

        private OutstandingAck(final Signal<?> signal, @Nullable final Target autoAckTarget,
                final CompletableFuture<CommandResponse<?>> future, final ExpressionResolver connectionIdResolver) {

            this.signal = signal;
            this.autoAckTarget = autoAckTarget;
            this.future = future;
            this.connectionIdResolver = connectionIdResolver;
        }

        private void completeWithSuccess() {
            future.complete(getSuccessAck(signal, autoAckTarget, connectionIdResolver));
        }

        private void completeWithFailure() {
            future.complete(getFailureAck(signal, autoAckTarget));
        }

        private Acknowledgement getFailureAck(final Signal<?> signal, @Nullable final Target target) {
            return buildAcknowledgement(signal, target, HttpStatus.SERVICE_UNAVAILABLE,
                    "Received negative confirm from the external broker.", connectionIdResolver);
        }

        private void completeForReturn(final int replyCode, final String replyText) {
            future.complete(getReturnAck(signal, autoAckTarget, replyCode, replyText));
        }

        private Acknowledgement getReturnAck(final Signal<?> signal,
                @Nullable final Target autoAckTarget,
                final int replyCode,
                final String replyText) {

            return buildAcknowledgement(signal, autoAckTarget, HttpStatus.SERVICE_UNAVAILABLE,
                    String.format("Received basic.return from the external broker: %d %s", replyCode, replyText),
                    connectionIdResolver);
        }

    }

    private enum ConfirmMode {
        UNKNOWN,
        ACTIVE,
        INACTIVE
    }

}
