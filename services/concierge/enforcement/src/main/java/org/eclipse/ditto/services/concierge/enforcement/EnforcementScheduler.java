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
package org.eclipse.ditto.services.concierge.enforcement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.services.utils.akka.logging.DittoDiagnosticLoggingAdapter;
import org.eclipse.ditto.services.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.services.utils.metrics.DittoMetrics;
import org.eclipse.ditto.services.utils.metrics.instruments.counter.Counter;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;

/**
 * Actor that schedules enforcement tasks. Relying on the inherent timeout of enforcement tasks to not leak memory.
 */
final class EnforcementScheduler extends AbstractActor {

    /**
     * The name of this actor under the parent actor, which should be EnforcerActor.
     */
    static final String ACTOR_NAME = "scheduler";

    /**
     * Cache of started enforcement tasks for each entity ID.
     */
    private final Map<EntityId, Futures> futuresMap;
    private final DittoDiagnosticLoggingAdapter log;
    private final Counter scheduledEnforcementTasks;
    private final Counter completedEnforcementTasks;

    private EnforcementScheduler() {
        futuresMap = new HashMap<>();
        log = DittoLoggerFactory.getDiagnosticLoggingAdapter(this);
        scheduledEnforcementTasks = DittoMetrics.counter("scheduled_enforcement_tasks");
        completedEnforcementTasks = DittoMetrics.counter("completed_enforcement_tasks");
    }

    static Props props() {
        return Props.create(EnforcementScheduler.class);
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(EnforcementTask.class, this::scheduleEnforcement)
                .match(FutureComplete.class, this::futureComplete)
                .matchAny(message -> log.warning("UnknownMessage <{}>", message))
                .build();
    }

    private void scheduleEnforcement(final EnforcementTask task) {
        futuresMap.compute(task.getEntityId(), (entityId, cachedFutures) -> {
            if (entityId.isDummy()) {
                // This should not happen: Refuse to perform enforcement task for messages without ID.
                log.error("EnforcementTaskWithoutEntityId <{}>", task);
                return null;
            } else {
                log.debug("Scheduling <{}> at <{}>", task, cachedFutures);
                final Futures previousFutures = cachedFutures != null ? cachedFutures : Futures.initial();
                return scheduleTaskAfter(previousFutures, task);
            }
        });
        scheduledEnforcementTasks.increment();
    }

    private void futureComplete(final FutureComplete futureComplete) {
        log.debug("Got <{}>", futureComplete);
        futureComplete.getError().ifPresent(error -> log.error(error, "FutureFailed <{}>", futureComplete));
        futuresMap.computeIfPresent(futureComplete.entityId, (entityId, futures) -> {
            log.debug("Reducing reference count <{}>", futures);
            return futures.onComplete();
        });
        completedEnforcementTasks.increment();
    }

    private Void dispatchEnforcedMessage(final Contextual<?> enforcementResult) {
        final DittoDiagnosticLoggingAdapter logger = enforcementResult.getLog();
        final Optional<? extends WithDittoHeaders> messageOpt = enforcementResult.getMessageOptional();
        if (messageOpt.isPresent()) {
            final WithDittoHeaders<?> message = messageOpt.get();
            logger.setCorrelationId(message);
            final Optional<ActorRef> receiverOpt = enforcementResult.getReceiver();
            final Optional<Supplier<CompletionStage<Object>>> askFutureOpt = enforcementResult.getAskFuture();
            if (askFutureOpt.isPresent() && receiverOpt.isPresent()) {
                final ActorRef receiver = receiverOpt.get();
                logger.debug("About to pipe contextual message <{}> after ask-step to receiver: <{}>",
                        message, receiver);
                // It does not disrupt command order guarantee to run the ask-future here if the ask-future
                // is initiated by a call to Patterns.ask(), because Patterns.ask() calls ActorRef.tell()
                // in the calling thread.
                Patterns.pipe(askFutureOpt.get().get(), getContext().dispatcher()).to(receiver);
            } else if (receiverOpt.isPresent()) {
                final ActorRef receiver = receiverOpt.get();
                final Object wrappedMsg =
                        enforcementResult.getReceiverWrapperFunction().apply(message);
                logger.debug("About to send contextual message <{}> to receiver: <{}>", wrappedMsg, receiver);
                receiver.tell(wrappedMsg, enforcementResult.getSender());
            } else {
                logger.debug("No receiver found in Contextual - as a result just ignoring it: <{}>", enforcementResult);
            }
            logger.discardCorrelationId();
        } else {
            // message does not exist; nothing to dispatch
            logger.debug("Not dispatching due to lack of message: {}", enforcementResult);
        }
        return null;
    }

    /**
     * Schedule an enforcement task based on previous futures of an entity such that enforcement task does not start
     * until all previous authorization changes are complete and does not complete until all previous tasks are
     * complete.
     *
     * @param previousFutures in-flight enforcement tasks for the same entity.
     * @param task the task to schedule.
     * @return the next in-flight enforcement tasks, including the scheduled task.
     */
    private Futures scheduleTaskAfter(final Futures previousFutures, final EnforcementTask task) {
        final CompletionStage<?> taskFuture =
                previousFutures.authFuture.thenCompose(authChangeComplete ->
                        previousFutures.enforceFuture.thenCombine(task.start(),
                                (previousTaskComplete, enforcementResult) -> dispatchEnforcedMessage(enforcementResult)
                        )
                ).handle((result, error) -> sendFutureComplete(task, error));
        return task.changesAuthorization()
                ? previousFutures.appendAuthFuture(taskFuture)
                : previousFutures.appendEnforceFuture(taskFuture);
    }

    private Void sendFutureComplete(final EnforcementTask task, @Nullable final Throwable error) {
        getSelf().tell(FutureComplete.of(task.getEntityId(), error), ActorRef.noSender());
        return null;
    }

    /**
     * Self-sent event to signal completion of an enforcement task for cache maintenance.
     */
    private static final class FutureComplete {

        private final EntityId entityId;
        @Nullable final Throwable error;

        private FutureComplete(final EntityId entityId, @Nullable final Throwable error) {
            this.entityId = entityId;
            this.error = error;
        }

        private static FutureComplete of(final EntityId entityId, @Nullable final Throwable error) {
            return new FutureComplete(entityId, error);
        }

        private Optional<Throwable> getError() {
            return Optional.ofNullable(error);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[entityId=" + entityId + "]";
        }
    }

    /**
     * Cache entry for 1 entity including: its last scheduled authorization-changing task, its last scheduled
     * non-authorization-changing task, and the amount of in-flight enforcement tasks.
     */
    private static final class Futures {

        private static final Futures INITIAL_FUTURES =
                new Futures(CompletableFuture.completedStage(null), CompletableFuture.completedStage(null), 0);

        private final CompletionStage<?> authFuture;
        private final CompletionStage<?> enforceFuture;
        private final int referenceCount;

        private Futures(final CompletionStage<?> authFuture,
                final CompletionStage<?> enforceFuture, final int referenceCount) {
            this.authFuture = authFuture;
            this.enforceFuture = enforceFuture;
            this.referenceCount = referenceCount;
        }

        /**
         * @return the initial future of all entities: all tasks complete; 0 task in-flight.
         */
        private static Futures initial() {
            return INITIAL_FUTURES;
        }

        /**
         * Add another authorization-changing task to the in-flight futures.
         *
         * @param authFuture the scheduled enforcement task that would change authorization upon completion.
         * @return the futures.
         */
        private Futures appendAuthFuture(final CompletionStage<?> authFuture) {
            return new Futures(authFuture, authFuture, referenceCount + 1);
        }

        /**
         * Add another non-authorization-changing task to the in-flight futures.
         *
         * @param enforceFuture the scheduled enforcement task that would not change authorization upon completion.
         * @return the futures.
         */
        private Futures appendEnforceFuture(final CompletionStage<?> enforceFuture) {
            return new Futures(authFuture, enforceFuture, referenceCount + 1);
        }

        @Nullable
        private Futures onComplete() {
            final int nextReferenceCount = referenceCount - 1;
            if (nextReferenceCount <= 0) {
                return null;
            } else {
                return new Futures(authFuture, enforceFuture, nextReferenceCount);
            }
        }

        @Override
        public String toString() {
            return String.format("%d Futures", referenceCount);
        }
    }
}
