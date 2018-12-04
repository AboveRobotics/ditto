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
package org.eclipse.ditto.model.namespaces;

import java.util.Optional;

/**
 * A reader which provides functionality to parse namespaces.
 */
public final class NamespaceReader {

    private static final char NAMESPACE_SEPARATOR = ':';

    /**
     * Reads the namespace from the identifier of an entity.
     *
     * @param id the identifier.
     * @return the optional namespace or an empty optional if a namespace can't be read.
     */
    public static Optional<String> fromEntityId(final String id) {
        final int i = id.indexOf(NAMESPACE_SEPARATOR);
        return i >= 0
                ? Optional.of(id.substring(0, i))
                : Optional.empty();
    }
}
