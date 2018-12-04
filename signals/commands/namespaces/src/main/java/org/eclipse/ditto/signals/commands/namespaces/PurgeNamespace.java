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
package org.eclipse.ditto.signals.commands.namespaces;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.base.CommandJsonDeserializer;

/**
 * Command for purging namespace data.
 */
@Immutable
public final class PurgeNamespace extends AbstractNamespaceCommand<PurgeNamespace> {

    /**
     * The name of the {@code PurgeNamespace} command.
     */
    static final String NAME = "purgeNamespace";

    /**
     * The type of the {@code PurgeNamespace} command.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    private PurgeNamespace(final CharSequence namespace, final DittoHeaders dittoHeaders) {
        super(namespace, TYPE, dittoHeaders);
    }

    /**
     * Returns an instance of {@code PurgeNamespace}.
     *
     * @param namespace the namespace to be purged.
     * @param dittoHeaders the headers of the command.
     * @return the instance.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code namespace} is empty.
     */
    public static PurgeNamespace of(final CharSequence namespace, final DittoHeaders dittoHeaders) {
        return new PurgeNamespace(namespace, dittoHeaders);
    }

    /**
     * Creates a new {@code PurgeNamespace} from a JSON object.
     *
     * @param jsonObject the JSON object of which the PurgeNamespace is to be created.
     * @param dittoHeaders the headers.
     * @return the command.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if {@code jsonObject} did not contain
     * {@link org.eclipse.ditto.signals.commands.namespaces.NamespaceCommand.JsonFields#NAMESPACE}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static PurgeNamespace fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandJsonDeserializer<PurgeNamespace>(TYPE, jsonObject).deserialize(() -> {
            final String namespace = jsonObject.getValueOrThrow(NamespaceCommand.JsonFields.NAMESPACE);
            return new PurgeNamespace(namespace, dittoHeaders);
        });
    }

    @Override
    public PurgeNamespace setDittoHeaders(final DittoHeaders dittoHeaders) {
        if (Objects.equals(getDittoHeaders(), dittoHeaders)) {
            return this;
        }
        return new PurgeNamespace(getNamespace(), dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof PurgeNamespace;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.toString() + "]";
    }

}
