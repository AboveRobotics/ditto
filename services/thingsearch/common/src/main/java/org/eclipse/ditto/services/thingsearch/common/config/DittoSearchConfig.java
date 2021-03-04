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
package org.eclipse.ditto.services.thingsearch.common.config;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.services.base.config.DittoServiceConfig;
import org.eclipse.ditto.services.base.config.http.HttpConfig;
import org.eclipse.ditto.services.base.config.limits.LimitsConfig;
import org.eclipse.ditto.services.utils.cluster.config.ClusterConfig;
import org.eclipse.ditto.services.utils.config.ConfigWithFallback;
import org.eclipse.ditto.services.utils.config.ScopedConfig;
import org.eclipse.ditto.services.utils.config.WithConfigPath;
import org.eclipse.ditto.services.utils.health.config.DefaultHealthCheckConfig;
import org.eclipse.ditto.services.utils.health.config.HealthCheckConfig;
import org.eclipse.ditto.services.utils.metrics.config.MetricsConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.DefaultIndexInitializationConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.DefaultMongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.IndexInitializationConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.MongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.operations.DefaultPersistenceOperationsConfig;
import org.eclipse.ditto.services.utils.persistence.operations.PersistenceOperationsConfig;

/**
 * This class is the default implementation of {@link SearchConfig}.
 */
@Immutable
public final class DittoSearchConfig implements SearchConfig, WithConfigPath {

    private static final String CONFIG_PATH = "things-search";

    private final DittoServiceConfig dittoServiceConfig;
    @Nullable private final String mongoHintsByNamespace;
    private final String queryCriteriaValidator;
    private final UpdaterConfig updaterConfig;
    private final HealthCheckConfig healthCheckConfig;
    private final IndexInitializationConfig indexInitializationConfig;
    private final PersistenceOperationsConfig persistenceOperationsConfig;
    private final MongoDbConfig mongoDbConfig;

    private DittoSearchConfig(final ScopedConfig dittoScopedConfig) {
        dittoServiceConfig = DittoServiceConfig.of(dittoScopedConfig, CONFIG_PATH);
        persistenceOperationsConfig = DefaultPersistenceOperationsConfig.of(dittoScopedConfig);
        mongoDbConfig = DefaultMongoDbConfig.of(dittoScopedConfig);
        healthCheckConfig = DefaultHealthCheckConfig.of(dittoScopedConfig);

        final ConfigWithFallback configWithFallback =
                ConfigWithFallback.newInstance(dittoScopedConfig, CONFIG_PATH, SearchConfigValue.values());
        mongoHintsByNamespace = configWithFallback.getStringOrNull(SearchConfigValue.MONGO_HINTS_BY_NAMESPACE);
        queryCriteriaValidator = configWithFallback.getStringOrNull(SearchConfigValue.QUERY_CRITERIA_VALIDATOR);
        updaterConfig = DefaultUpdaterConfig.of(configWithFallback);
        indexInitializationConfig = DefaultIndexInitializationConfig.of(configWithFallback);
    }

    /**
     * Returns an instance of DittoSearchConfig based on the settings of the specified Config.
     *
     * @param dittoScopedConfig is supposed to provide the settings of the service config at the {@code "ditto"} config
     * path.
     * @return the instance.
     * @throws org.eclipse.ditto.services.utils.config.DittoConfigError if {@code config} is invalid.
     */
    public static DittoSearchConfig of(final ScopedConfig dittoScopedConfig) {
        return new DittoSearchConfig(dittoScopedConfig);
    }

    @Override
    public Optional<String> getMongoHintsByNamespace() {
        return Optional.ofNullable(mongoHintsByNamespace);
    }

    @Override
    public String getQueryValidatorImplementation() {
        return queryCriteriaValidator;
    }

    @Override
    public UpdaterConfig getUpdaterConfig() {
        return updaterConfig;
    }

    @Override
    public ClusterConfig getClusterConfig() {
        return dittoServiceConfig.getClusterConfig();
    }

    @Override
    public LimitsConfig getLimitsConfig() {
        return dittoServiceConfig.getLimitsConfig();
    }

    @Override
    public HttpConfig getHttpConfig() {
        return dittoServiceConfig.getHttpConfig();
    }

    @Override
    public MetricsConfig getMetricsConfig() {
        return dittoServiceConfig.getMetricsConfig();
    }

    @Override
    public HealthCheckConfig getHealthCheckConfig() {
        return healthCheckConfig;
    }

    @Override
    public IndexInitializationConfig getIndexInitializationConfig() {
        return indexInitializationConfig;
    }

    @Override
    public PersistenceOperationsConfig getPersistenceOperationsConfig() {
        return persistenceOperationsConfig;
    }

    @Override
    public MongoDbConfig getMongoDbConfig() {
        return mongoDbConfig;
    }

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DittoSearchConfig that = (DittoSearchConfig) o;
        return Objects.equals(mongoHintsByNamespace, that.mongoHintsByNamespace) &&
                Objects.equals(queryCriteriaValidator, that.queryCriteriaValidator) &&
                Objects.equals(updaterConfig, that.updaterConfig) &&
                Objects.equals(dittoServiceConfig, that.dittoServiceConfig) &&
                Objects.equals(healthCheckConfig, that.healthCheckConfig) &&
                Objects.equals(indexInitializationConfig, that.indexInitializationConfig) &&
                Objects.equals(persistenceOperationsConfig, that.persistenceOperationsConfig) &&
                Objects.equals(mongoDbConfig, that.mongoDbConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mongoHintsByNamespace, queryCriteriaValidator, updaterConfig,
                dittoServiceConfig, healthCheckConfig, indexInitializationConfig, persistenceOperationsConfig,
                mongoDbConfig);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "mongoHintsByNamespace=" + mongoHintsByNamespace +
                ", queryCriteriaValidator=" + queryCriteriaValidator +
                ", updaterConfig=" + updaterConfig +
                ", dittoServiceConfig=" + dittoServiceConfig +
                ", healthCheckConfig=" + healthCheckConfig +
                ", indexInitializationConfig=" + indexInitializationConfig +
                ", persistenceOperationsConfig=" + persistenceOperationsConfig +
                ", mongoDbConfig=" + mongoDbConfig +
                "]";
    }

    @Override
    public String getConfigPath() {
        return CONFIG_PATH;
    }

}
