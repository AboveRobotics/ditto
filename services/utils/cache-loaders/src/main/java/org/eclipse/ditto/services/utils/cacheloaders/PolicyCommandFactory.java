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
package org.eclipse.ditto.services.utils.cacheloaders;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.services.models.policies.commands.sudo.SudoRetrievePolicy;
import org.eclipse.ditto.services.utils.cache.CacheLookupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates commands to access the Policies service.
 */
final class PolicyCommandFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyCommandFactory.class);

    private PolicyCommandFactory() {
        throw new AssertionError();
    }


    /**
     * Creates a sudo command for retrieving a policy.
     *
     * @param policyId the policyId.
     * @param cacheLookupContext the context to apply when doing the cache lookup.
     * @return the created command.
     */
    static SudoRetrievePolicy sudoRetrievePolicy(final EntityId policyId,
            @Nullable final CacheLookupContext cacheLookupContext) {
        return sudoRetrievePolicy(PolicyId.of(policyId), cacheLookupContext);
    }

    /**
     * Creates a sudo command for retrieving a policy.
     *
     * @param policyId the policyId.
     * @param cacheLookupContext the context to apply when doing the cache lookup.
     * @return the created command.
     */
    static SudoRetrievePolicy sudoRetrievePolicy(final PolicyId policyId,
            @Nullable final CacheLookupContext cacheLookupContext) {
        return SudoRetrievePolicy.of(policyId,
                Optional.ofNullable(cacheLookupContext).flatMap(CacheLookupContext::getDittoHeaders)
                        .map(headers -> DittoHeaders.newBuilder()
                                .authorizationContext(headers.getAuthorizationContext())
                                .schemaVersion(headers.getImplementedSchemaVersion())
                                .correlationId("sudoRetrievePolicy-" +
                                        headers.getCorrelationId().orElseGet(() -> UUID.randomUUID().toString()))
                                .build()
                        )
                        .orElseGet(() ->
                                DittoHeaders.newBuilder()
                                        .correlationId("sudoRetrievePolicy-" + UUID.randomUUID().toString())
                                        .build()));
    }

}
