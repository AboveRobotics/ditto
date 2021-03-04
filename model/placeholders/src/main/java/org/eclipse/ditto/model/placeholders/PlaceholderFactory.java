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
package org.eclipse.ditto.model.placeholders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.auth.AuthorizationContext;

/**
 * Factory that creates instances of {@link Placeholder}, {@link PlaceholderResolver}s and {@link ExpressionResolver}s.
 */
public final class PlaceholderFactory {

    /**
     * @return new instance of the {@link HeadersPlaceholder}
     */
    public static HeadersPlaceholder newHeadersPlaceholder() {
        return ImmutableHeadersPlaceholder.INSTANCE;
    }

    /**
     * @return new instance of the {@link ThingPlaceholder}
     */
    public static ThingPlaceholder newThingPlaceholder() {
        return ImmutableThingPlaceholder.INSTANCE;
    }

    /**
     * @return new instance of the {@link PolicyPlaceholder}
     */
    public static PolicyPlaceholder newPolicyPlaceholder() {
        return ImmutablePolicyPlaceholder.INSTANCE;
    }

    /**
     * @return new instance of the {@link FeaturePlaceholder}
     * @since 1.5.0
     */
    public static FeaturePlaceholder newFeaturePlaceholder() {
        return ImmutableFeaturePlaceholder.INSTANCE;
    }

    /**
     * @return new instance of the {@link EntityPlaceholder}
     */
    public static EntityPlaceholder newEntityPlaceholder() {
        return ImmutableEntityPlaceholder.INSTANCE;
    }

    /**
     * @return new instance of the {@link TopicPathPlaceholder}
     */
    public static TopicPathPlaceholder newTopicPathPlaceholder() {
        return ImmutableTopicPathPlaceholder.INSTANCE;
    }

    /**
     * @return the unique instance of the placeholder with prefix {@code request}.
     */
    public static Placeholder<AuthorizationContext> newRequestPlaceholder() {
        return ImmutableRequestPlaceholder.INSTANCE;
    }

    /**
     * Creates a new PlaceholderResolver instance based on the given {@link Placeholder} and a placeholder source for
     * looking up placeholder names in.
     *
     * @param placeholder the placeholder.
     * @param placeholderSource the placeholder source for looking up placeholder names in.
     * @param <T> the type of the placeholder source
     * @return the created PlaceholderResolver instance
     */
    public static <T> PlaceholderResolver<T> newPlaceholderResolver(final Placeholder<T> placeholder,
            @Nullable final T placeholderSource) {
        return new ImmutablePlaceholderResolver<>(placeholder, placeholderSource);
    }

    /**
     * Creates a new PlaceholderResolver instance for validation based on the given {@link Placeholder}. As for
     * validation no lookup in a placeholder source has to be made, the source must not be provided.
     *
     * @param placeholder the placeholder.
     * @param <T> the type of the placeholder source
     * @return the created PlaceholderResolver instance
     */
    public static <T> PlaceholderResolver<T> newPlaceholderResolverForValidation(final Placeholder<T> placeholder) {
        return new ImmutablePlaceholderResolver<>(placeholder, null);
    }

    /**
     * Creates a new ExpressionResolver instance initialized with the passed in {@code placeholderResolvers} for looking
     * up {@link Placeholder}s.
     *
     * @param placeholderResolvers the PlaceholderResolvers to use in order to lookup placeholders in expressions.
     * @return the created ExpressionResolver instance
     */
    public static ExpressionResolver newExpressionResolver(final PlaceholderResolver<?>... placeholderResolvers) {
        return newExpressionResolver(Arrays.asList(placeholderResolvers));
    }

    /**
     * Creates a new ExpressionResolver instance initialized with the passed in {@code placeholderResolvers} for looking
     * up {@link Placeholder}s.
     *
     * @param placeholderResolvers the PlaceholderResolvers to use in order to lookup placeholders in expressions.
     * @return the created ExpressionResolver instance
     */
    public static ExpressionResolver newExpressionResolver(final List<PlaceholderResolver<?>> placeholderResolvers) {
        return new ImmutableExpressionResolver(placeholderResolvers);
    }

    /**
     * Creates a new ExpressionResolver instance initialized with a single {@code placeholder} and
     * {@code placeholderSource} for looking up {@link Placeholder}s.
     *
     * @param placeholder the placeholder.
     * @param placeholderSource the placeholder source for looking up placeholder names in.
     * @return the created ExpressionResolver instance
     */
    public static <T> ExpressionResolver newExpressionResolver(final Placeholder<T> placeholder,
            @Nullable final T placeholderSource) {
        return newExpressionResolver(Collections.singletonList(newPlaceholderResolver(placeholder, placeholderSource)));
    }

    /**
     * Creates a new ExpressionResolver instance for validation initialized with 0 or more placeholders.
     *
     * @param placeholders the placeholders.
     * @return the created ExpressionResolver instance
     */
    public static ExpressionResolver newExpressionResolverForValidation(final Placeholder<?>... placeholders) {
        return newExpressionResolverForValidation("", placeholders);
    }

    /**
     * Creates a new ExpressionResolver instance for validation initialized with 0 or more placeholders.
     *
     * @param stringUsedInPlaceholderReplacement the dummy value used as a replacement for the found placeholders.
     * @param placeholders the placeholders.
     * @return the created ExpressionResolver instance
     */
    public static ExpressionResolver newExpressionResolverForValidation(final String stringUsedInPlaceholderReplacement,
            final Placeholder<?>... placeholders) {
        return newExpressionResolver(
                Arrays.stream(placeholders)
                        .map(PlaceholderFactory::newPlaceholderResolverForValidation)
                        .collect(Collectors.toList()),
                stringUsedInPlaceholderReplacement);
    }

    private static ExpressionResolver newExpressionResolver(final List<PlaceholderResolver<?>> placeholderResolvers,
            final String stringUsedInPlaceholderValidation) {
        return new ImmutableExpressionResolver(placeholderResolvers, stringUsedInPlaceholderValidation);
    }

    /**
     * @return new instance of the {@link SourceAddressPlaceholder}
     * @since 1.4.0
     */
    public static SourceAddressPlaceholder newSourceAddressPlaceholder() {
        return ImmutableSourceAddressPlaceholder.INSTANCE;
    }

    /**
     * @return the singleton instance of {@link ConnectionIdPlaceholder}.
     * @since 1.4.0
     */
    public static ConnectionIdPlaceholder newConnectionIdPlaceholder() {
        return ImmutableConnectionIdPlaceholder.INSTANCE;
    }

    /**
     * @return new instance of the {@link PolicyEntryPlaceholder}
     * @since 2.0.0
     */
    public static PolicyEntryPlaceholder newPolicyEntryPlaceholder() {
        return ImmutablePolicyEntryPlaceholder.INSTANCE;
    }

    private PlaceholderFactory() {
        throw new AssertionError();
    }

}
