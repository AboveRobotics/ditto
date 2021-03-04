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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ditto.model.things.ThingId;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ExpressionResolverTest {

    private static final HeadersPlaceholder HEADERS_PLACEHOLDER = PlaceholderFactory.newHeadersPlaceholder();
    private static final ThingPlaceholder THING_PLACEHOLDER = PlaceholderFactory.newThingPlaceholder();
    private static final TopicPathPlaceholder TOPIC_PLACEHOLDER = PlaceholderFactory.newTopicPathPlaceholder();

    private static final String THING_NS = "the.thing";
    private static final String THING_NAME = "the.id:the-rest";
    private static final ThingId THING_ID = ThingId.of(THING_NS, THING_NAME);

    private ExpressionResolver expressionResolver;

    @Before
    public void setupExpressionResolver() {
        final Map<String, String> headersMap = new HashMap<>();
        headersMap.put("header-name", "header-val");
        headersMap.put("header:with:colon", "value:with:colon");
        expressionResolver = PlaceholderFactory.newExpressionResolver(
                PlaceholderFactory.newPlaceholderResolver(HEADERS_PLACEHOLDER, headersMap),
                PlaceholderFactory.newPlaceholderResolver(THING_PLACEHOLDER, THING_ID),
                PlaceholderFactory.newPlaceholderResolver(TOPIC_PLACEHOLDER, null)
        );
    }

    @Test
    public void testPlaceholderFunctionDefaultWithConstant() {

        assertThat(expressionResolver.resolve("{{ header:nonexistent | fn:default('fallback-val') }}"))
                .contains("fallback-val");
    }

    @Test
    public void testHeaderWithColon() {
        assertThat(expressionResolver.resolve("{{ header:header:with:colon }}"))
                .contains("value:with:colon");
    }

    @Test
    public void testPlaceholderFunctionDefaultWithPlaceholder() {
        assertThat(expressionResolver.resolve("{{ header:nonexistent | fn:default(header:header-name) }}"))
                .contains("header-val");
    }

    @Test
    public void testPlaceholderFunctionDefaultWithPlaceholderNonExistingDefault() {
        assertThat(expressionResolver.resolve("{{ header:nonexistent | fn:default(header:alsoNotThere) }}"))
                .isEmpty();
    }

    @Test
    public void testPlaceholderFunctionSubstringBefore() {
        assertThat(expressionResolver.resolve("{{ thing:namespace }}:{{thing:name | fn:substring-before(':') }}"))
                .contains(THING_NS + ":" + "the.id");
    }

    @Test
    public void testPlaceholderFunctionSubstringBeforeWithDefaultFallback() {
        assertThat(expressionResolver.resolve(
                "{{ thing:namespace }}:{{thing:name | fn:substring-before('_') | fn:default(thing:name)}}"))
                .contains(THING_ID.toString());
    }

    @Test
    public void testPlaceholderFunctionSubstringAfterWithUpper() {
        assertThat(expressionResolver.resolve("{{ thing:name | fn:substring-after(':') | fn:upper() }}"))
                .contains("the-rest".toUpperCase());
    }

    @Test
    public void testLoneDelete() {
        assertThat(expressionResolver.resolve("{{ fn:delete() }}"))
                .isEqualTo(PipelineElement.deleted());
    }

    @Test
    public void testLoneDefault() {
        assertThat(expressionResolver.resolve("{{ fn:default(header:header-name) }}"))
                .contains("header-val");
    }

    @Test
    public void testPipelineStartingWithDefault() {
        assertThat(expressionResolver.resolve("{{ fn:default(header:header-name) | fn:upper() }}"))
                .contains("HEADER-VAL");
    }

    @Test
    public void testDeleteIfUnresolved() {
        assertThat(expressionResolver.resolve("{{ header:nonexistent }}"))
                .isEqualTo(PipelineElement.unresolved());

        assertThat(expressionResolver.resolve("{{ header:nonexistent | fn:default(fn:delete()) }}"))
                .isEqualTo(PipelineElement.deleted());

        assertThat(expressionResolver.resolve("{{ header:header-name | fn:delete() }}"))
                .isEqualTo(PipelineElement.deleted());
    }

    @Test
    public void testPartialResolution() {
        assertThat(expressionResolver.resolvePartially("{{header:header-name}}-{{unknown:placeholder|fn:unknown}}"))
                .isEqualTo("header-val-{{unknown:placeholder|fn:unknown}}");
    }

}
