/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.placeholders;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.ditto.base.model.common.Placeholders;

/**
 * The ExpressionResolver is able to:
 * <ul>
 * <li>resolve {@link Placeholder}s in a passed {@code template} (based on {@link PlaceholderResolver}</li>
 * <li>execute optional pipeline stages in a passed {@code template}</li>
 * </ul>
 * As a result, a resolved String is returned.
 * For example, following expressions can be resolved:
 * <ul>
 * <li>{@code {{ thing:id }} }</li>
 * <li>{@code {{ header:device_id }} }</li>
 * <li>{@code {{ topic:full }} }</li>
 * <li>{@code {{ thing:name | fn:substring-before(':') | fn:default(thing:name) }} }</li>
 * <li>{@code {{ header:unknown | fn:default('fallback') }} }</li>
 * </ul>
 */
public interface ExpressionResolver {

    /**
     * Resolve a single pipeline expression.
     *
     * @param pipelineExpression the pipeline expression.
     * @return the pipeline element after evaluation.
     * @throws UnresolvedPlaceholderException if not all placeholders were resolved
     */
    PipelineElement resolveAsPipelineElement(String pipelineExpression);

    /**
     * Resolves a complete expression template starting with a {@link Placeholder} followed by optional pipeline stages
     * (e.g. functions).
     *
     * @param expressionTemplate the expressionTemplate to resolve {@link Placeholder}s and and execute optional
     * pipeline stages
     * @return the resolved String, a signifier for resolution failure, or one for deletion.
     * @throws PlaceholderFunctionTooComplexException thrown if the {@code expressionTemplate} contains a placeholder
     * function chain which is too complex (e.g. too much chained function calls)
     */
    default PipelineElement resolve(final String expressionTemplate) {
        return ExpressionResolver.substitute(expressionTemplate, this::resolveAsPipelineElement);
    }

    /**
     * Resolve a single pipeline expression as a stream of pipeline elements.
     *
     * @param pipelineExpression the pipeline expression.
     * @return the stream of pipeline elements after evaluation.
     * @throws UnresolvedPlaceholderException if not all placeholders were resolved
     * @since 2.4.0
     */
    Stream<PipelineElement> resolveAsArrayPipelineElement(String pipelineExpression);

    /**
     * Resolves a complete expression template as a stream of pipeline elements starting with a
     * {@link Placeholder} followed by optional pipeline stages (e.g. functions).
     *
     * @param expressionTemplate the expressionTemplate to resolve {@link Placeholder}s and execute optional
     * pipeline stages
     * @return the stream of pipeline elements after evaluation.
     * @throws PlaceholderFunctionTooComplexException thrown if the {@code expressionTemplate} contains a placeholder
     * function chain which is too complex (e.g. too much chained function calls)
     * @since 2.4.0
     */
    default Stream<PipelineElement> resolveAsArray(final String expressionTemplate) {
        return ExpressionResolver.substituteArray(expressionTemplate, this::resolveAsArrayPipelineElement);
    }

    /**
     * Resolves a complete expression template starting with a {@link Placeholder} followed by optional pipeline stages
     * (e.g. functions). Keep unresolvable expressions as is.
     *
     * @param expressionTemplate the expressionTemplate to resolve {@link Placeholder}s and execute optional
     * pipeline stages
     * @param forbiddenUnresolvedExpressionPrefixes a collection of expression prefixes which must be resolved
     * @return the resolved String, a signifier for resolution failure, or one for deletion.
     * @throws PlaceholderFunctionTooComplexException thrown if the {@code expressionTemplate} contains a placeholder
     * function chain which is too complex (e.g. too much chained function calls)
     * @throws UnresolvedPlaceholderException if placeholders could not be resolved which contained prefixed in the
     * provided {@code forbiddenUnresolvedExpressionPrefixes} list.
     * @since 2.0.0
     */
    default String resolvePartially(final String expressionTemplate,
            final Collection<String> forbiddenUnresolvedExpressionPrefixes) {

        return ExpressionResolver.substitute(expressionTemplate, expression -> {
            final PipelineElement pipelineElement;
            try {
                pipelineElement = resolveAsPipelineElement(expression);
            } catch (final UnresolvedPlaceholderException e) {
                if (forbiddenUnresolvedExpressionPrefixes.stream().anyMatch(expression::startsWith)) {
                    throw e;
                } else {
                    // placeholder is not supported; return the expression without resolution.
                    return PipelineElement.resolved("{{" + expression + "}}");
                }
            }

            return pipelineElement.onUnresolved(() -> {
                if (forbiddenUnresolvedExpressionPrefixes.stream().anyMatch(expression::startsWith)) {
                    throw UnresolvedPlaceholderException.newBuilder(expression).build();
                }
                return PipelineElement.resolved("{{" + expression + "}}");
            });
        }).toOptional().orElseThrow(() -> new IllegalStateException("Impossible"));
    }

    /**
     * Perform simple substitution on a string based on a template function.
     *
     * @param input the input string.
     * @param substitutionFunction the substitution function turning the content of each placeholder into a result.
     * @return the substitution result.
     */
    static PipelineElement substitute(
            final String input,
            final Function<String, PipelineElement> substitutionFunction) {

        final Matcher matcher = Placeholders.pattern().matcher(input);
        final StringBuffer resultBuilder = new StringBuffer();

        while (matcher.find()) {
            final String placeholderExpression = Placeholders.groupNames()
                    .stream()
                    .map(matcher::group)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse("");
            final PipelineElement element = substitutionFunction.apply(placeholderExpression);
            switch (element.getType()) {
                case DELETED:
                case UNRESOLVED:
                    // abort pipeline execution: resolution failed or the string has been deleted.
                    return element;
                default:
                    // proceed to append resolution result and evaluate the next pipeline expression
            }
            // append resolved placeholder
            element.map(resolvedValue -> {
                // increment counter inside matcher for "matcher.appendTail" later
                matcher.appendReplacement(resultBuilder, "");
                // actually append resolved value - do not attempt to interpret as regex
                resultBuilder.append(resolvedValue);
                return resolvedValue;
            });
        }

        matcher.appendTail(resultBuilder);
        return PipelineElement.resolved(resultBuilder.toString());

    }

    /**
     * Perform simple substitution on a string as a stream of pipeline elements based on a template function.
     *
     * @param input the input string.
     * @param substitutionFunction the substitution function turning the content of each placeholder into a result.
     * @return the stream of substitution results.
     * @since 2.4.0
     */
    static Stream<PipelineElement> substituteArray(
            final String input,
            final Function<String, Stream<PipelineElement>> substitutionFunction) {

        final Matcher matcher = Placeholders.pattern().matcher(input);
        Collection<StringBuffer> resultBuilder = new ArrayList<>();
        resultBuilder.add(new StringBuffer());

        while (matcher.find()) {
            final String placeholderExpression = Placeholders.groupNames()
                    .stream()
                    .map(matcher::group)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse("");

            final Collection<StringBuffer> finalResultBuilder = resultBuilder;

            final Stream<PipelineElement> elements = substitutionFunction.apply(placeholderExpression);

            final AtomicInteger counter = new AtomicInteger();
            final AtomicReference<StringBuffer> appendingBuffer = new AtomicReference<>(new StringBuffer());

            resultBuilder = elements
                    .filter(element -> !element.getType().equals(PipelineElement.Type.UNRESOLVED))
                    .flatMap(element ->
                            finalResultBuilder.stream()
                                    .flatMap(string -> {
                                        if (counter.get() == 0) {
                                            counter.getAndIncrement();
                                            matcher.appendReplacement(appendingBuffer.get(), "");
                                        }
                                        return element.toOptionalStream().map(streamElement ->
                                                new StringBuffer(string)
                                                        .append(appendingBuffer.get())
                                                        .append(streamElement)
                                                );
                                    })
                    ).collect(Collectors.toList());
        }
        return resultBuilder.stream()
                .map(matcher::appendTail)
                .map(StringBuffer::toString)
                .map(PipelineElement::resolved);

    }


}
