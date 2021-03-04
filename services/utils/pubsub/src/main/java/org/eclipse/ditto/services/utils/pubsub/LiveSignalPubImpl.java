/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.utils.pubsub;

import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.services.utils.pubsub.extractors.ConstantTopics;
import org.eclipse.ditto.services.utils.pubsub.extractors.PubSubTopicExtractor;
import org.eclipse.ditto.services.utils.pubsub.extractors.ReadSubjectExtractor;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.events.base.Event;

import akka.actor.ActorContext;

/**
 * Default implementation of {@link LiveSignalPub}.
 */
final class LiveSignalPubImpl implements LiveSignalPub {

    private final DistributedPub<Command> liveCommandPub;
    private final DistributedPub<Event> liveEventPub;
    private final DistributedPub<Signal> messagePub;

    private LiveSignalPubImpl(
            final DistributedPub<Command> liveCommandPub,
            final DistributedPub<Event> liveEventPub,
            final DistributedPub<Signal> messagePub) {
        this.liveCommandPub = liveCommandPub;
        this.liveEventPub = liveEventPub;
        this.messagePub = messagePub;
    }

    /**
     * Start a live signal pub in an actor system.
     *
     * @param context context of the actor under which the pub and sub supervisors are started.
     * @param distributedAcks the distributed acks interface.
     * @return the live signal pub.
     */
    static LiveSignalPubImpl of(final ActorContext context, final DistributedAcks distributedAcks) {
        final DistributedPub<?> distributedPub =
                LiveSignalPubSubFactory.of(context, distributedAcks).startDistributedPub();
        final DistributedPub<Command> liveCommandPub =
                distributedPub.withTopicExtractor(getTopicExtractor(StreamingType.LIVE_COMMANDS));
        final DistributedPub<Event> liveEventPub =
                distributedPub.withTopicExtractor(getTopicExtractor(StreamingType.LIVE_EVENTS));
        final DistributedPub<Signal> messagePub =
                distributedPub.withTopicExtractor(getTopicExtractor(StreamingType.MESSAGES));
        return new LiveSignalPubImpl(liveCommandPub, liveEventPub, messagePub);
    }

    @Override
    public DistributedPub<Command> command() {
        return liveCommandPub;
    }

    @Override
    public DistributedPub<Event> event() {
        return liveEventPub;
    }

    @Override
    public DistributedPub<Signal> message() {
        return messagePub;
    }

    private static <T extends WithDittoHeaders> PubSubTopicExtractor<T> getTopicExtractor(
            final StreamingType streamingType) {

        return ReadSubjectExtractor.<T>of().with(ConstantTopics.of(streamingType.getDistributedPubSubTopic()));
    }

}
