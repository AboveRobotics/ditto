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
package org.eclipse.ditto.services.policies.common.config;

import java.time.Duration;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.services.base.config.supervision.WithSupervisorConfig;
import org.eclipse.ditto.services.utils.config.KnownConfigValue;
import org.eclipse.ditto.services.utils.persistence.mongo.config.WithActivityCheckConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.WithSnapshotConfig;

/**
 * Provides configuration settings for policy entities.
 */
@Immutable
public interface PolicyConfig extends WithSupervisorConfig, WithActivityCheckConfig, WithSnapshotConfig {

    /**
     * Returns the configuration to which duration the {@code expiry} of a {@code Policy Subject} should be rounded up
     * to.
     * For example:
     * <ul>
     * <li>configured to "1 second": a received "expiry" is rounded up to the next full second</li>
     * <li>configured to "30 seconds": a received "expiry" is rounded up to the next half minute</li>
     * <li>configured to "1 hour": a received "expiry" is rounded up to the next full hour</li>
     * <li>configured to "12 hours": a received "expiry" is rounded up to the next half day</li>
     * <li>configured to "1 day": a received "expiry" is rounded up to the next full day</li>
     * <li>configured to "15 days": a received "expiry" is rounded up to the next half month</li>
     * </ul>
     *
     * @return the granularity to round up policy subject {@code expiry} timestamps to.
     */
    Duration getSubjectExpiryGranularity();

    /**
     * Return the class responsible for placeholder resolution in the subject ID of policy action commands.
     *
     * @return the class for subject resolution.
     */
    String getSubjectIdResolver();

    /**
     * An enumeration of the known config path expressions and their associated default values for {@code PolicyConfig}.
     */
    enum PolicyConfigValue implements KnownConfigValue {

        /**
         * The granularity to round up policy subject {@code expiry} timestamps to.
         */
        SUBJECT_EXPIRY_GRANULARITY("subject-expiry-granularity", Duration.ofHours(1L)),

        SUBJECT_ID_RESOLVER("subject-id-resolver",
                "org.eclipse.ditto.services.policies.persistence.actors.resolvers.DefaultSubjectIdFromActionResolver");

        private final String path;
        private final Object defaultValue;

        PolicyConfigValue(final String thePath, final Object theDefaultValue) {
            path = thePath;
            defaultValue = theDefaultValue;
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String getConfigPath() {
            return path;
        }

    }
}
