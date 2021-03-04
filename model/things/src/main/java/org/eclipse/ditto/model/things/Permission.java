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
package org.eclipse.ditto.model.things;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonKey;

/**
 * An enumeration of the available Permissions of Ditto.
 *
 * @deprecated Permissions belong to deprecated API version 1. Use API version 2 with policies instead.
 */
@Deprecated
@Immutable
public enum Permission {

    /**
     * Permission to read an entity, for example a Thing.
     */
    READ,

    /**
     * Permission to write/change an entity.
     */
    WRITE,

    /**
     * Permission to grant and revoke Permissions to other Authorization Subjects.
     */
    ADMINISTRATE;

    /**
     * Returns all permissions concatenated as one String. The result is composed using the delimiter {@code ", "}, the
     * prefix {@code "<"} and the suffix {@code ">"}.
     *
     * @return all permissions of this enum as one String.
     */
    public static String allToString() {
        final String delimiter = ", ";
        final String prefix = "<";
        final String suffix = ">";
        return Arrays.stream(Permission.values()) //
                .map(Permission::toString) //
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * Returns this Permission constant as {@link JsonKey}.
     *
     * @return this Permission constant as known JSON key.
     */
    public JsonKey toJsonKey() {
        return JsonFactory.newKey(name());
    }

}
