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
package org.eclipse.ditto.services.connectivity.messaging;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.services.connectivity.messaging.monitoring.ConnectionMonitor;

/**
 * {@link org.eclipse.ditto.services.connectivity.messaging.MappingResultHandler} for messages.
 * This handler forwards to the given handlers. Additionally it
 * calls the {@link org.eclipse.ditto.services.connectivity.messaging.MappingResultHandler#onException(Exception)}
 * method for exceptions thrown in handlers and increases the according counters for mapped, dropped failed messages.
 *
 * @param <M> type of mapped messages.
 * @param <R> type of results.
 */
abstract class AbstractMappingResultHandler<M, R> implements MappingResultHandler<M, R> {

    private final Function<M, R> onMessageMapped;
    private final Runnable onMessageDropped;
    private final BiConsumer<Exception, TopicPath> onException;
    private final R emptyResult;
    private final BinaryOperator<R> combineResults;
    private final Collection<ConnectionMonitor> mappedMonitors;
    private final Collection<ConnectionMonitor> droppedMonitors;
    private final ConnectionMonitor.InfoProvider infoProvider;
    private TopicPath topicPath = null;

    protected AbstractMappingResultHandler(final AbstractBuilder<M, R, ?> builder) {
        onMessageMapped = checkNotNull(builder.onMessageMapped, "onMessageMapped");
        onMessageDropped = checkNotNull(builder.onMessageDropped, "onMessageDropped");
        onException = checkNotNull(builder.onException, "onException");
        mappedMonitors = checkNotNull(builder.mappedMonitors, "mappedMonitors");
        droppedMonitors = checkNotNull(builder.droppedMonitors, "droppedMonitors");
        infoProvider = checkNotNull(builder.infoProvider, "infoProvider");
        emptyResult = checkNotNull(builder.emptyResult, "emptyResult");
        combineResults = checkNotNull(builder.combineResults, "combineResults");
    }

    @Override
    public R onMessageMapped(final M mappedMessage) {
        try {
            mappedMonitors.forEach(monitor -> monitor.success(infoProvider));
            return onMessageMapped.apply(mappedMessage);
        } catch (final Exception e) {
            return onException(e);
        }
    }

    @Override
    public R onMessageDropped() {
        try {
            droppedMonitors.forEach(monitor -> monitor.success(infoProvider));
            onMessageDropped.run();
            return emptyResult;
        } catch (final Exception e) {
            return onException(e);
        }
    }

    @Override
    public R onException(final Exception exception) {
        if (exception instanceof DittoRuntimeException) {
            mappedMonitors.forEach(monitor -> monitor.failure((DittoRuntimeException) exception));
        } else {
            mappedMonitors.forEach(monitor -> monitor.exception(exception));
        }
        onException.accept(exception, topicPath);
        return emptyResult;
    }

    @Override
    public R combineResults(final R left, final R right) {
        return combineResults.apply(left, right);
    }

    @Override
    public R emptyResult() {
        return emptyResult;
    }

    @Override
    public void onTopicPathResolved(final TopicPath topicPath) {
        this.topicPath = topicPath;
    }

    @NotThreadSafe
    abstract static class AbstractBuilder<M, R, T extends AbstractBuilder<M, R, T>> {

        protected Collection<ConnectionMonitor> mappedMonitors;
        protected Collection<ConnectionMonitor> droppedMonitors;
        private final T myself;
        private ConnectionMonitor.InfoProvider infoProvider;
        private Function<M, R> onMessageMapped;
        private Runnable onMessageDropped;
        private BiConsumer<Exception, TopicPath> onException;
        private R emptyResult;
        private BinaryOperator<R> combineResults;

        protected AbstractBuilder(final Class<? extends T> selfType) {
            myself = selfType.cast(this);
        }

        T infoProvider(final ConnectionMonitor.InfoProvider infoProvider) {
            this.infoProvider = infoProvider;
            return myself;
        }

        T onMessageMapped(final Function<M, R> onMessageMapped) {
            this.onMessageMapped = onMessageMapped;
            return myself;
        }

        T onMessageDropped(final Runnable onMessageDropped) {
            this.onMessageDropped = onMessageDropped;
            return myself;
        }

        T onException(final BiConsumer<Exception, TopicPath> onException) {
            this.onException = onException;
            return myself;
        }

        T emptyResult(final R emptyResult) {
            this.emptyResult = emptyResult;
            return myself;
        }

        T combineResults(final BinaryOperator<R> combineResults) {
            this.combineResults = combineResults;
            return myself;
        }

    }

}
