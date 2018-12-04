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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.eclipsesource.json.JsonParser;
import com.eclipsesource.json.ParseException;

/**
 * This class provides access to functionality for parsing a {@link JsonValue} from various inputs.
 */
@Immutable
final class JsonValueParser {

    @Nullable private static Function<String, JsonValue> fromStringInstance = null;

    private JsonValueParser() {
        super();
    }

    /**
     * Returns a parser which accepts a String and which uses the given handler for object creation.
     * The parsed JsonValue can be obtained from the given handler finally.
     *
     * @param jsonHandler receives parser events in order to create a {@link org.eclipse.ditto.json.JsonValue}.
     * @param <A> the type to be used for parsing JSON arrays.
     * @param <O> the type to be used for parsing JSON objects.
     * @param <V> the type of the value this handler returns.
     * @return the parse Function.
     * @throws NullPointerException if {@code jsonHandler} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the string cannot be parsed.
     * @see DittoJsonHandler#getValue()
     */
    public static <A, O, V> Consumer<String> fromString(final DittoJsonHandler<A, O, V> jsonHandler) {
        return jsonString -> tryToParseJsonValue(jsonString, jsonHandler);
    }

    /**
     * Returns a Function for parsing a String to an instance of {@link JsonValue}.
     *
     * @return the function.
     */
    public static Function<String, JsonValue> fromString() {
        Function<String, JsonValue> result = fromStringInstance;
        if (null == result) {
            result = jsonString -> tryToParseJsonValue(jsonString, DefaultDittoJsonHandler.newInstance());
            fromStringInstance = result;
        }
        return result;
    }

    /**
     * Returns a Function for obtaining an instance of {@link JsonValue} from a {@code Reader}.
     *
     * @return the function.
     */
    public static Function<Reader, JsonValue> fromReader() {
        return JsonValueParser::tryToReadJsonValueFrom;
    }

    private static <T> T tryToParseJsonValue(final String jsonString,
            final DittoJsonHandler<?, ?, T> dittoJsonHandler) {

        try {
            return parseJsonValue(jsonString, dittoJsonHandler);
        } catch (final ParseException | UnsupportedOperationException | StackOverflowError e) {
            throw JsonParseException.newBuilder()
                    .message(MessageFormat.format("Failed to parse JSON string ''{0}''!", jsonString))
                    .cause(e)
                    .build();
        }
    }

    private static <T> T parseJsonValue(final String jsonString, final DittoJsonHandler<?, ?, T> dittoJsonHandler) {
        requireNonNull(jsonString, "The JSON string to be parsed must not be null!");
        new JsonParser(dittoJsonHandler).parse(jsonString);
        return dittoJsonHandler.getValue();
    }

    private static JsonValue tryToReadJsonValueFrom(final Reader reader) {
        try {
            return readJsonValueFrom(reader);
        } catch (final ParseException | IOException | StackOverflowError e) {
            throw JsonParseException.newBuilder()
                    .message("Failed to parse JSON value from reader!")
                    .cause(e)
                    .build();
        }
    }

    private static JsonValue readJsonValueFrom(final Reader reader) throws IOException {
        requireNonNull(reader, "The reader must not be null!");
        final DefaultDittoJsonHandler dittoJsonHandler = DefaultDittoJsonHandler.newInstance();
        new JsonParser(dittoJsonHandler).parse(reader);
        return dittoJsonHandler.getValue();
    }

}
