/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link org.eclipse.ditto.json.ImmutableJsonArray.ValueListJsonHandler}.
 * This class is indirectly tested which reflects the real use case.
 */
public final class ValueListJsonHandlerTest {

    private static JsonObject knownJsonObject;
    private static JsonArray anotherJsonArray;
    private static JsonArray knownJsonArray;

    private ImmutableJsonArray.ValueListJsonHandler underTest;
    private Consumer<String> parser;

    @BeforeClass
    public static void initTestConstants() {
        knownJsonObject = JsonObject.newBuilder()
                .set("foo", "bar")
                .set("baz", JsonObject.newBuilder()
                        .set("int", Integer.MAX_VALUE)
                        .set("boolean", true)
                        .set("double", 23.42D)
                        .set("long", Long.MAX_VALUE)
                        .set("object", JsonObject.newBuilder()
                                .set("unu", "asdf")
                                .set("du", "jkl;")
                                .build()
                        )
                        .build()
                )
                .build();

        anotherJsonArray = JsonArray.of("a", "b", "c", 1, 2.3, false, JsonArray.of("Hello", "World"));

        knownJsonArray = JsonArray.newBuilder()
                .add("hubbl")
                .add(anotherJsonArray)
                .add("fubbl")
                .add(false)
                .add(knownJsonObject)
                .add(3)
                .build();
    }

    @Before
    public void setUp() {
        underTest = new ImmutableJsonArray.ValueListJsonHandler();
        parser = JsonValueParser.fromString(underTest);
    }

    @Test
    public void tryToParseNullString() {
        assertThatNullPointerException()
                .isThrownBy(() -> parser.accept(null))
                .withMessage("The %s must not be null!", "JSON string to be parsed")
                .withNoCause();
    }

    @Test
    public void parseEmptyArrayStringToEmptyList() {
        parser.accept(JsonArray.empty().toString());

        assertThat(underTest.getValue()).isEmpty();
    }

    @Test
    public void parseArrayString() {
        final List<JsonValue> expected = new ArrayList<>();
        expected.add(JsonValue.of("hubbl"));
        expected.add(anotherJsonArray);
        expected.add(JsonValue.of("fubbl"));
        expected.add(JsonValue.of(false));
        expected.add(knownJsonObject);
        expected.add(JsonValue.of(3));

        parser.accept(knownJsonArray.toString());
        final List<JsonValue> actual = underTest.getValue();

        assertThat(actual).isEqualTo(expected);
    }

    private static Map.Entry<String, JsonField> getEntry(final String key, final JsonValue value) {
        return MapEntry.entry(key, JsonField.newInstance(key, value));
    }

}