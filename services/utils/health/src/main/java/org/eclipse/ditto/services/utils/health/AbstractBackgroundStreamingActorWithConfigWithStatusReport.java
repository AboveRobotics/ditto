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
package org.eclipse.ditto.services.utils.health;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.services.utils.akka.actors.ModifyConfigBehavior;
import org.eclipse.ditto.services.utils.akka.actors.RetrieveConfigBehavior;
import org.eclipse.ditto.services.utils.akka.logging.DittoDiagnosticLoggingAdapter;
import org.eclipse.ditto.services.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.services.utils.config.DittoConfigError;
import org.eclipse.ditto.services.utils.health.config.BackgroundStreamingConfig;
import org.eclipse.ditto.signals.commands.common.Shutdown;
import org.eclipse.ditto.signals.commands.common.ShutdownResponse;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import akka.Done;
import akka.actor.AbstractActorWithTimers;
import akka.japi.Pair;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.KillSwitch;
import akka.stream.KillSwitches;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

/**
 * Common behavior for actors that stay alive for a long time running a stream over and over.
 *
 * @param <C> type of configuration.
 */
public abstract class AbstractBackgroundStreamingActorWithConfigWithStatusReport<C extends BackgroundStreamingConfig>
        extends AbstractActorWithTimers implements RetrieveConfigBehavior, ModifyConfigBehavior {

    /**
     * Modifiable config to control this actor.
     */
    protected C config;

    /**
     * The logger.
     */
    protected final DittoDiagnosticLoggingAdapter log;

    /**
     * The actor materializer to materialize streams.
     */
    protected final ActorMaterializer materializer;

    @Nullable
    private final Deque<Pair<Instant, Event>> events;
    private KillSwitch killSwitch;

    /**
     * Initialize the actor with a background streaming config.
     *
     * @param config the background streaming config.
     */
    protected AbstractBackgroundStreamingActorWithConfigWithStatusReport(final C config) {
        this.config = config;
        log = DittoLoggerFactory.getDiagnosticLoggingAdapter(this);
        materializer = ActorMaterializer.create(getContext());
        events = new ArrayDeque<>(config.getKeptEvents() + 1);

        if (config.isEnabled()) {
            scheduleWakeUp();
        }
    }

    /**
     * Enqueue an element into a deque of bounded size.
     *
     * @param queue the deque.
     * @param element the element to enqueue.
     * @param maxQueueSize the maximum size of the queue.
     * @param <T> the type of elements.
     */
    protected static <T> void enqueue(final Deque<Pair<Instant, T>> queue, final T element, final int maxQueueSize) {
        queue.addFirst(Pair.create(Instant.now(), element));
        if (queue.size() > maxQueueSize) {
            queue.removeLast();
        }
    }

    /**
     * Construct a config object by parsing HOCON.
     *
     * @param config the HOCON.
     * @return the config object.
     */
    protected abstract C parseConfig(final Config config);

    /**
     * Get the stream that should be restarted again and again in the background as a source of whatever
     * to be followed by a kill switch and a sink that ignores all elements.
     *
     * @return the source.
     */
    protected abstract Source<?, ?> getSource();

    /**
     * Add message handling logic to the sleeping behavior of this actor.
     *
     * @param sleepingReceiveBuilder the builder for the sleeping behavior.
     */
    protected void preEnhanceSleepingBehavior(final ReceiveBuilder sleepingReceiveBuilder) {
        // do nothing by default
    }

    /**
     * Add message handling logic to the streaming behavior of this actor.
     *
     * @param streamingReceiveBuilder the builder for the streaming behavior.
     */
    protected void preEnhanceStreamingBehavior(final ReceiveBuilder streamingReceiveBuilder) {
        // do nothing by default
    }

    /**
     * Append fields to the status report.
     *
     * @param statusReportBuilder the builder for the status report.
     */
    protected void postEnhanceStatusReport(final JsonObjectBuilder statusReportBuilder) {
        // do nothing by default
    }

    @Override
    public Receive createReceive() {
        return sleeping();
    }

    @Override
    public Config getConfig() {
        return config.getConfig();
    }

    @Override
    public Config setConfig(final Config config) {
        final C previousConfig = this.config;
        // TODO Ditto issue #439: replace ConfigWithFallback - it breaks AbstractConfigValue.withFallback!
        // Workaround: re-parse my config
        final Config fallback = ConfigFactory.parseString(getConfig().root().render(ConfigRenderOptions.concise()));
        try {
            this.config = parseConfig(config.withFallback(fallback));
        } catch (final DittoConfigError | ConfigException e) {
            log.error(e, "Failed to set config");
        }
        if (!previousConfig.isEnabled() && this.config.isEnabled()) {
            scheduleWakeUp();
        }
        return this.config.getConfig();
    }

    private Receive sleeping() {
        final ReceiveBuilder sleepingReceiveBuilder = ReceiveBuilder.create();
        preEnhanceSleepingBehavior(sleepingReceiveBuilder);
        return sleepingReceiveBuilder.match(WokeUp.class, this::wokeUp)
                .match(Event.class, this::addCustomEventToLog)
                .match(RetrieveHealth.class, this::retrieveHealth)
                .match(Shutdown.class, this::shutdownStream)
                .build()
                .orElse(retrieveConfigBehavior())
                .orElse(modifyConfigBehavior());
    }

    private Receive streaming() {
        final ReceiveBuilder streamingReceiveBuilder = ReceiveBuilder.create();
        preEnhanceStreamingBehavior(streamingReceiveBuilder);
        return streamingReceiveBuilder
                .match(StreamTerminated.class, this::streamTerminated)
                .match(Event.class, this::addCustomEventToLog)
                .match(RetrieveHealth.class, this::retrieveHealth)
                .match(Shutdown.class, this::shutdownStream)
                .build()
                .orElse(retrieveConfigBehavior())
                .orElse(modifyConfigBehavior());
    }

    private void wokeUp(final WokeUp wokeUp) {
        log.info("Woke up.");
        enqueue(events, wokeUp.enable(config.isEnabled()), config.getKeptEvents());
        if (config.isEnabled()) {
            restartStream();
            getContext().become(streaming());
        } else {
            log.warning("Not waking up because disabled.");
        }
    }

    /**
     * Handle stream termination.
     *
     * @param streamTerminated the event of stream termination.
     */
    protected void streamTerminated(final Event streamTerminated) {
        enqueue(events, streamTerminated, config.getKeptEvents());
        if (config.isEnabled()) {
            log.info("Stream terminated. Will restart after quiet period.");
            scheduleWakeUp();
        } else {
            log.warning("Stream terminated while disabled.");
        }
        getContext().become(sleeping());
    }


    private void scheduleWakeUp() {
        scheduleWakeUp(config.getQuietPeriod());
    }

    private void scheduleWakeUp(final Duration when) {
        getTimers().startSingleTimer(WokeUp.class, WokeUp.ENABLED, when);
    }

    private void shutdownStream(final Shutdown shutdown) {
        log.info("Terminating stream on demand: <{}>", shutdown);
        shutdownKillSwitch();

        final Event streamTerminated = new StreamTerminated("Got " + shutdown);
        enqueue(events, streamTerminated, config.getKeptEvents());
        getContext().become(sleeping());

        if (config.isEnabled()) {
            final Duration wakeUpDelay = config.getQuietPeriod();
            final String message = String.format("Restarting in <%s>.", wakeUpDelay);
            scheduleWakeUp(wakeUpDelay);
            getSender().tell(ShutdownResponse.of(message, shutdown.getDittoHeaders()), getSelf());
        } else {
            final String message = "Not restarting stream because I am disabled.";
            getSender().tell(ShutdownResponse.of(message, shutdown.getDittoHeaders()), getSelf());
        }
    }

    private void addCustomEventToLog(final Event event) {
        enqueue(events, event, config.getKeptEvents());
    }

    private void restartStream() {
        shutdownKillSwitch();

        final Pair<UniqueKillSwitch, CompletionStage<Done>> materializedValues =
                getSource().viaMat(KillSwitches.single(), Keep.right())
                        .toMat(Sink.ignore(), Keep.both())
                        .run(materializer);

        killSwitch = materializedValues.first();

        materializedValues.second()
                .<Void>handle((result, error) -> {
                    final String description = String.format("Stream terminated. Result=<%s> Error=<%s>",
                            result, error);
                    log.info(description);
                    getSelf().tell(new StreamTerminated(description), getSelf());
                    return null;
                });
    }

    private void shutdownKillSwitch() {
        if (killSwitch != null) {
            killSwitch.shutdown();
            killSwitch = null;
        }
    }

    private void retrieveHealth(final RetrieveHealth trigger) {
        getSender().tell(RetrieveHealthResponse.of(renderStatusInfo(), trigger.getDittoHeaders()), getSelf());
    }

    private StatusInfo renderStatusInfo() {
        return StatusInfo.fromStatus(StatusInfo.Status.UP,
                Collections.singletonList(StatusDetailMessage.of(StatusDetailMessage.Level.INFO, render())));
    }

    private JsonObject render() {
        final JsonObjectBuilder statusReportBuilder = JsonObject.newBuilder()
                .set(JsonFields.ENABLED, config.isEnabled())
                .set(JsonFields.EVENTS, events.stream()
                        .map(AbstractBackgroundStreamingActorWithConfigWithStatusReport::renderEvent)
                        .collect(JsonCollectors.valuesToArray()));
        postEnhanceStatusReport(statusReportBuilder);
        return statusReportBuilder.build();
    }

    private static JsonObject renderEvent(final Pair<Instant, Event> element) {
        return JsonObject.newBuilder()
                .set(element.first().toString(), element.second().name())
                .build();
    }

    /**
     * Event to report.
     */
    protected interface Event {

        String name();
    }

    private static final class WokeUp implements Event {

        private static final WokeUp ENABLED = new WokeUp(true);

        private final boolean enabled;

        private WokeUp(final boolean enabled) {
            this.enabled = enabled;
        }

        private WokeUp enable(final boolean isEnabled) {
            return new WokeUp(isEnabled);
        }

        @Override
        public String name() {
            return enabled ? "WOKE_UP" : "Not waking up: I am disabled.";
        }
    }

    private static final class StreamTerminated implements Event {

        private final String whatHappened;

        private StreamTerminated(final String whatHappened) {
            this.whatHappened = whatHappened;
        }

        @Override
        public String name() {
            return whatHappened;
        }
    }

    private static final class JsonFields {

        private static final JsonFieldDefinition<Boolean> ENABLED =
                JsonFactory.newBooleanFieldDefinition("enabled");

        private static final JsonFieldDefinition<JsonArray> EVENTS =
                JsonFactory.newJsonArrayFieldDefinition("events");
    }
}
