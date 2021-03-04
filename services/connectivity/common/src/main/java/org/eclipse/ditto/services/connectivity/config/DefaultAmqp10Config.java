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
package org.eclipse.ditto.services.connectivity.config;

import java.time.Duration;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.services.base.config.ThrottlingConfig;
import org.eclipse.ditto.services.utils.config.ConfigWithFallback;
import org.eclipse.ditto.services.utils.config.ScopedConfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * This class is the default implementation of {@link Amqp10Config}.
 */
@Immutable
public final class DefaultAmqp10Config implements Amqp10Config {

    private static final String CONFIG_PATH = "amqp10";
    private static final String CONSUMER_PATH = "consumer";
    private static final String BACKOFF_PATH = "backoff";

    private final boolean consumerRateLimitEnabled;
    private final int consumerMaxInFlight;
    private final Duration consumerRedeliveryExpectationTimeout;
    private final int producerCacheSize;
    private final BackOffConfig backOffConfig;
    private final ThrottlingConfig consumerThrottlingConfig;
    private final int maxQueueSize;
    private final int messagePublishingParallelism;
    private final Duration globalConnectTimeout;
    private final Duration globalSendTimeout;
    private final Duration globalRequestTimeout;
    private final int globalPrefetchPolicyAllCount;

    private DefaultAmqp10Config(final ScopedConfig config) {
        consumerRateLimitEnabled = config.getBoolean(Amqp10ConfigValue.CONSUMER_RATE_LIMIT_ENABLED.getConfigPath());
        consumerMaxInFlight = config.getInt(Amqp10ConfigValue.CONSUMER_MAX_IN_FLIGHT.getConfigPath());
        consumerRedeliveryExpectationTimeout =
                config.getDuration(Amqp10ConfigValue.CONSUMER_REDELIVERY_EXPECTATION_TIMEOUT.getConfigPath());
        producerCacheSize = config.getInt(Amqp10ConfigValue.PRODUCER_CACHE_SIZE.getConfigPath());
        backOffConfig = DefaultBackOffConfig.of(config.hasPath(BACKOFF_PATH)
                ? config
                : ConfigFactory.parseString(BACKOFF_PATH + "={}"));
        consumerThrottlingConfig = ThrottlingConfig.of(config.hasPath(CONSUMER_PATH)
                ? config.getConfig(CONSUMER_PATH)
                : ConfigFactory.empty());
        maxQueueSize = config.getInt(Amqp10ConfigValue.MAX_QUEUE_SIZE.getConfigPath());
        messagePublishingParallelism = config.getInt(Amqp10ConfigValue.MESSAGE_PUBLISHING_PARALLELISM.getConfigPath());
        globalConnectTimeout = config.getDuration(Amqp10ConfigValue.GLOBAL_CONNECT_TIMEOUT.getConfigPath());
        globalSendTimeout = config.getDuration(Amqp10ConfigValue.GLOBAL_SEND_TIMEOUT.getConfigPath());
        globalRequestTimeout = config.getDuration(Amqp10ConfigValue.GLOBAL_REQUEST_TIMEOUT.getConfigPath());
        globalPrefetchPolicyAllCount =
                config.getInt(Amqp10ConfigValue.GLOBAL_PREFETCH_POLICY_ALL_COUNT.getConfigPath());
    }

    /**
     * Returns an instance of {@code DefaultAmqp10Config} based on the settings of the specified Config.
     *
     * @param config is supposed to provide the settings of the JavaScript mapping config at {@value #CONFIG_PATH}.
     * @return the instance.
     * @throws org.eclipse.ditto.services.utils.config.DittoConfigError if {@code config} is invalid.
     */
    public static DefaultAmqp10Config of(final Config config) {
        return new DefaultAmqp10Config(ConfigWithFallback.newInstance(config, CONFIG_PATH, Amqp10ConfigValue.values()));
    }

    @Override
    public boolean isConsumerRateLimitEnabled() {
        return consumerRateLimitEnabled;
    }

    @Override
    public int getConsumerMaxInFlight() {
        return consumerMaxInFlight;
    }

    @Override
    public Duration getConsumerRedeliveryExpectationTimeout() {
        return consumerRedeliveryExpectationTimeout;
    }

    @Override
    public ThrottlingConfig getConsumerThrottlingConfig() {
        return consumerThrottlingConfig;
    }

    @Override
    public int getProducerCacheSize() {
        return producerCacheSize;
    }

    @Override
    public BackOffConfig getBackOffConfig() {
        return backOffConfig;
    }

    @Override
    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    @Override
    public int getPublisherParallelism() {
        return messagePublishingParallelism;
    }

    @Override
    public Duration getGlobalConnectTimeout() {
        return globalConnectTimeout;
    }

    @Override
    public Duration getGlobalSendTimeout() {
        return globalSendTimeout;
    }

    @Override
    public Duration getGlobalRequestTimeout() {
        return globalRequestTimeout;
    }

    @Override
    public int getGlobalPrefetchPolicyAllCount() {
        return globalPrefetchPolicyAllCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultAmqp10Config that = (DefaultAmqp10Config) o;
        return consumerRateLimitEnabled == that.consumerRateLimitEnabled &&
                consumerMaxInFlight == that.consumerMaxInFlight &&
                producerCacheSize == that.producerCacheSize &&
                globalPrefetchPolicyAllCount == that.globalPrefetchPolicyAllCount &&
                Objects.equals(consumerRedeliveryExpectationTimeout, that.consumerRedeliveryExpectationTimeout) &&
                Objects.equals(backOffConfig, that.backOffConfig) &&
                maxQueueSize == that.maxQueueSize &&
                messagePublishingParallelism == that.messagePublishingParallelism &&
                Objects.equals(consumerThrottlingConfig, that.consumerThrottlingConfig) &&
                Objects.equals(globalConnectTimeout, that.globalConnectTimeout) &&
                Objects.equals(globalSendTimeout, that.globalSendTimeout) &&
                Objects.equals(globalRequestTimeout, that.globalRequestTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerRateLimitEnabled, consumerMaxInFlight, consumerRedeliveryExpectationTimeout,
                producerCacheSize, backOffConfig, consumerThrottlingConfig, maxQueueSize,
                messagePublishingParallelism, globalConnectTimeout, globalSendTimeout, globalRequestTimeout,
                globalPrefetchPolicyAllCount);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "consumerRateLimitEnabled=" + consumerRateLimitEnabled +
                ", consumerMaxInFlight=" + consumerMaxInFlight +
                ", consumerRedeliveryExpectationTimeout=" + consumerRedeliveryExpectationTimeout +
                ", producerCacheSize=" + producerCacheSize +
                ", backOffConfig=" + backOffConfig +
                ", consumerThrottlingConfig=" + consumerThrottlingConfig +
                ", maxQueueSize=" + maxQueueSize +
                ", messagePublishingParallelism=" + messagePublishingParallelism +
                ", globalConnectTimeout=" + globalConnectTimeout +
                ", globalSendTimeout=" + globalSendTimeout +
                ", globalRequestTimeout=" + globalRequestTimeout +
                ", globalPrefetchPolicyAllCount=" + globalPrefetchPolicyAllCount +
                "]";
    }
}
