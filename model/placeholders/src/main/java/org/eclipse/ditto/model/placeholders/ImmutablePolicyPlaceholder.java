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
package org.eclipse.ditto.model.placeholders;

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.policies.PolicyId;

/**
 * Placeholder implementation that replaces {@code policy:id}, {@code policy:namespace} and {@code policy:name}. The
 * input value is a String and must be a valid Policy ID.
 */
@Immutable
final class ImmutablePolicyPlaceholder extends AbstractEntityPlaceholder<PolicyId> implements PolicyPlaceholder {

    /**
     * Singleton instance of the ImmutablePolicyPlaceholder.
     */
    static final ImmutablePolicyPlaceholder INSTANCE = new ImmutablePolicyPlaceholder();

    @Override
    public String getPrefix() {
        return "policy";
    }

    @Override
    public Optional<String> resolve(final CharSequence policyId, final String placeholder) {
        argumentNotEmpty(placeholder, "placeholder");
        checkNotNull(policyId, "Policy ID");
        if (policyId instanceof PolicyId) {
            return doResolve(((PolicyId) policyId), placeholder);
        } else {
            return Optional.empty();
        }
    }
}
