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
package org.eclipse.ditto.services.models.connectivity;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.connectivity.Enforcement;
import org.eclipse.ditto.model.connectivity.EnforcementFilter;
import org.eclipse.ditto.model.connectivity.EnforcementFilterFactory;
import org.eclipse.ditto.model.placeholders.Placeholder;
import org.eclipse.ditto.model.placeholders.PlaceholderFactory;
import org.eclipse.ditto.model.placeholders.PlaceholderFilter;

/**
 * Factory that creates instances of EnforcementFilterFactory.
 *
 * @param <I> the type required to resolve the placeholders in the input
 * @param <M> the type required to resolve the placeholders in the filters
 */
@Immutable
final class ImmutableEnforcementFilterFactory<I, M> implements EnforcementFilterFactory<I, M> {

    private final Enforcement enforcement;
    private final Placeholder<I> inputPlaceholder;
    private final List<Placeholder<M>> filterPlaceholder;

    /**
     * Instantiates a new {@link ImmutableEnforcementFilterFactory}.
     *
     * @param enforcement the enforcement configuration, contains the input and filters templates
     * @param inputPlaceholder the input placeholder used to resolve the values in the input template
     * @param filterPlaceholder the filters placeholder used to resolve the values in the filters template
     */
    ImmutableEnforcementFilterFactory(final Enforcement enforcement,
            final Placeholder<I> inputPlaceholder,
            final List<Placeholder<M>> filterPlaceholder) {
        this.enforcement = enforcement;
        this.inputPlaceholder = inputPlaceholder;
        this.filterPlaceholder = filterPlaceholder;
    }

    @Override
    public EnforcementFilter<M> getFilter(final I input) {
        final String inputResolved = PlaceholderFilter.apply(enforcement.getInput(),
                PlaceholderFactory.newExpressionResolver(inputPlaceholder, input));
        return new ImmutableEnforcementFilter<>(enforcement, filterPlaceholder, inputResolved);
    }

}
