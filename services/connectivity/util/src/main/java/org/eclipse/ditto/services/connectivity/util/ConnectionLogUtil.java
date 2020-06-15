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

package org.eclipse.ditto.services.connectivity.util;

import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.services.utils.akka.LogUtil;

import akka.event.DiagnosticLoggingAdapter;

/**
 * Util for adding the connection id to a logger.
 */
public final class ConnectionLogUtil {

    private static final String MDC_CONNECTION_ID = "connection-id";

    private ConnectionLogUtil() {
        throw new AssertionError();
    }

    /**
     * Enhances the default slf4j MDC map with an entry for the passed {@code connectionId}.
     *
     * @param connectionId the connection ID to set.
     */
    public static void enhanceLogWithConnectionId(final CharSequence connectionId) {
        LogUtil.enhanceLogWithCustomField(LogUtil.newMdcField(MDC_CONNECTION_ID, connectionId.toString()));
    }

    /**
     * Enhances the default slf4j MDC map with an entry for the passed {@code connectionId} and {@code correlationId}.
     *
     * @param correlationId the correlation ID to set.
     * @param connectionId the connection ID to set.
     */
    public static void enhanceLogWithCorrelationIdAndConnectionId(final String correlationId,
            final CharSequence connectionId) {
        enhanceLogWithConnectionId(connectionId);
        LogUtil.enhanceLogWithCorrelationId(correlationId);
    }

    /**
     * Enhances the passed {@link DiagnosticLoggingAdapter} with an "MDC" map entry for the passed {@code
     * connectionId}.
     *
     * @param loggingAdapter the DiagnosticLoggingAdapter to set the "MDC" on.
     * @param connectionId the connection ID to set.
     */
    public static void enhanceLogWithConnectionId(final DiagnosticLoggingAdapter loggingAdapter,
            final CharSequence connectionId) {
        LogUtil.enhanceLogWithCustomField(loggingAdapter, MDC_CONNECTION_ID, connectionId.toString());
    }

    /**
     * Enhances the passed {@link DiagnosticLoggingAdapter} with an "MDC" map entry for the passed {@code connectionId}
     * and the correlation ID (if present).
     *
     * @param loggingAdapter the DiagnosticLoggingAdapter to set the "MDC" on.
     * @param signal signal possibly containing a correlation ID.
     * @param connectionId the connection ID to set.
     */
    public static void enhanceLogWithCorrelationIdAndConnectionId(final DiagnosticLoggingAdapter loggingAdapter,
            final WithDittoHeaders<?> signal,
            final CharSequence connectionId) {
        LogUtil.enhanceLogWithCorrelationId(loggingAdapter, signal,
                LogUtil.newMdcField(MDC_CONNECTION_ID, connectionId.toString()));
    }

    /**
     * Enhances the passed {@link DiagnosticLoggingAdapter} with an "MDC" map entry for the passed {@code connectionId}
     * and the correlation ID.
     *
     * @param loggingAdapter the DiagnosticLoggingAdapter to set the "MDC" on.
     * @param correlationId the correlating Protocol message ID
     * @param connectionId the connection ID to set.
     */
    public static void enhanceLogWithCorrelationIdAndConnectionId(final DiagnosticLoggingAdapter loggingAdapter,
            final String correlationId,
            final CharSequence connectionId) {
        LogUtil.enhanceLogWithCorrelationId(loggingAdapter, correlationId,
                LogUtil.newMdcField(MDC_CONNECTION_ID, connectionId.toString()));
    }

}
