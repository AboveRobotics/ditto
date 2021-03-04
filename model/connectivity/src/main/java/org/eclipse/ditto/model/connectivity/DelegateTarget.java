/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.model.connectivity;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.common.ConditionChecker;

/**
 * Abstract class that delegates to an existing {@link Target}. Used to extend the default fields of a {@link Target}
 * for a specific protocol.
 */
abstract class DelegateTarget implements Target {

    protected final Target delegate;

    DelegateTarget(final Target delegate) {
        this.delegate = ConditionChecker.checkNotNull(delegate, "delegate");
    }

    @Override
    public String getAddress() {
        return delegate.getAddress();
    }

    @Override
    public String getOriginalAddress() {
        return delegate.getOriginalAddress();
    }

    @Override
    public Set<FilteredTopic> getTopics() {
        return delegate.getTopics();
    }

    @Override
    public AuthorizationContext getAuthorizationContext() {
        return delegate.getAuthorizationContext();
    }

    @Override
    public Optional<HeaderMapping> getHeaderMapping() {
        return delegate.getHeaderMapping();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DelegateTarget that = (DelegateTarget) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "delegate=" + delegate +
                "]";
    }
}
