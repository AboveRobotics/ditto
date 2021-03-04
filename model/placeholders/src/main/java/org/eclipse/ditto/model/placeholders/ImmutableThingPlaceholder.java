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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.things.ThingId;

/**
 * Placeholder implementation that replaces {@code thing:id}, {@code thing:namespace} and {@code thing:name}. The
 * input value is a String and must be a valid Thing ID.
 */
@Immutable
final class ImmutableThingPlaceholder extends AbstractEntityPlaceholder<ThingId> implements ThingPlaceholder {

    /**
     * Singleton instance of the ImmutableThingPlaceholder.
     */
    static final ImmutableThingPlaceholder INSTANCE = new ImmutableThingPlaceholder();

    @Override
    public String getPrefix() {
        return "thing";
    }

    @Override
    public Optional<String> resolve(final CharSequence thingId, final String placeholder) {
        argumentNotEmpty(placeholder, "placeholder");
        checkNotNull(thingId, "Thing ID");
        if (thingId instanceof ThingId) {
            return doResolve(((ThingId) thingId), placeholder);
        } else {
            return Optional.empty();
        }
    }
}
