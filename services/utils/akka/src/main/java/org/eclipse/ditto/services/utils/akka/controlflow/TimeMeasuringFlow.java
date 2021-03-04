/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.utils.akka.controlflow;

import java.time.Duration;

import org.eclipse.ditto.services.utils.metrics.instruments.timer.PreparedTimer;
import org.eclipse.ditto.services.utils.metrics.instruments.timer.StartedTimer;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.Graph;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Zip;

public final class TimeMeasuringFlow {

    private TimeMeasuringFlow() {
        //No-Op because this is a factory.
    }

    /**
     * Builds a flow that measures the time it took the given flow to process the input to the output using the given timer.
     *
     * @param flow the flow that should be measured.
     * @param timer the timer that should be used to measure the time.
     * @param <I> the type of the Flow input.
     * @param <O> the type of the Flow output.
     * @return a Flow wrapping the given flow to measure the time.
     */
    public static <I, O, M> Flow<I, O, M> measureTimeOf(final Flow<I, O, M> flow, final PreparedTimer timer) {
        return measureTimeOf(flow, timer, Sink.ignore());
    }

    /**
     * Builds a flow that measures the time it took the given flow to process the input to the output using the given timer.
     *
     * <pre>
     *   +------------------------------------------------------------------------------------+
     *   |                                                                                    |
     *   |                         +--------------+                                           |
     *   |                     +-->+ startTimer   +-------------+   +-----+   +-----------+   |
     * IN|  +---------------+  |   +--------------+             +-->+ zip +-->+ stopTimer |   |
     * +--->+ beforeTimerBC +--+                                |   +-----+   +-----------+   |
     *   |  +---------------+  |   +------+   +--------------+  |                             |
     *   |                     +-->+ flow +-->+ afterTimerBC +--+                             |OUT
     *   |                         +------+   +--------------+  +-------------------------------->
     *   |                                                                                    |
     *   +------------------------------------------------------------------------------------+
     * </pre>
     *
     * @param flow the flow that should be measured.
     * @param timer the timer that should be used to measure the time.
     * @param durationSink the sink which should receive the measured time of each request.
     * @param <I> the type of the Flow input.
     * @param <O> the type of the Flow output.
     * @return a Flow wrapping the given flow to measure the time.
     */
    @SuppressWarnings("unchecked")
    public static <I, O, M> Flow<I, O, M> measureTimeOf(final Flow<I, O, M> flow, final PreparedTimer timer,
            final Sink<Duration, ?> durationSink) {

        final Graph<FlowShape<I, O>, M> flowShapeNotUsedGraph = GraphDSL.create(flow, (builder, flowShape) -> {

            final UniformFanOutShape<I, I> beforeTimerBroadcast = builder.add(Broadcast.create(2));

            final UniformFanOutShape<O, O> afterTimerBroadcast = builder.add(Broadcast.create(2));

            final FanInShape2<StartedTimer, O, Pair<StartedTimer, O>> zip = builder.add(Zip.create());

            final Flow<Pair<StartedTimer, O>, Duration, NotUsed> stopTimerFlow =
                    Flow.<Pair<StartedTimer, O>, Duration>fromFunction(pair -> pair.first().stop().getDuration());

            final Sink<Pair<StartedTimer, O>, NotUsed> stopTimerSink = stopTimerFlow.to(durationSink);

            final Flow<I, StartedTimer, NotUsed> startTimerFlow = Flow.fromFunction(request -> timer.start());

            // its important that outlet 0 is connected to the timers, to guarantee that the timer is started first
            builder.from(beforeTimerBroadcast.out(0))
                    .via(builder.add(startTimerFlow))
                    .toInlet(zip.in0());

            builder.from(afterTimerBroadcast.out(0))
                    .toInlet(zip.in1());

            builder.from(zip.out())
                    .to(builder.add(stopTimerSink));

            builder.from(beforeTimerBroadcast.out(1))
                    .via(flowShape)
                    .viaFanOut(afterTimerBroadcast);

            return FlowShape.of(beforeTimerBroadcast.in(), afterTimerBroadcast.out(1));
        });

        return Flow.fromGraph(flowShapeNotUsedGraph);
    }

}
