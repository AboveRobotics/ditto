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

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * An immutable representation of a JSON number, which is known to fit in Java {@code double}.
 */
@Immutable
final class ImmutableJsonDouble extends AbstractJsonNumber<Double> {

    private ImmutableJsonDouble(final double value) {
        super(value);
    }

    /**
     * Returns an instance of {@code ImmutableJsonDouble}.
     *
     * @return the instance.
     */
    public static ImmutableJsonDouble of(final double value) {
        return new ImmutableJsonDouble(value);
    }

    @Override
    public boolean isDouble() {
        return true;
    }

    @Override
    public double asDouble() {
        return getValue();
    }

    @Override
    public boolean isInt() {
        final Double value = getValue();
        return value.intValue() == value;
    }

    @Override
    public boolean isLong() {
        final Double value = getValue();
        return value.longValue() == value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AbstractJsonNumber) {
            final AbstractJsonNumber that = (AbstractJsonNumber) o;
            if (isInt() && that.isInt()) {
                return Objects.equals(asInt(), that.asInt());
            }
            if (isLong() && that.isLong()) {
                return Objects.equals(asLong(), that.asLong());
            }
            return Objects.equals(getValue(), that.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (isInt()) {
            return asInt();
        }
        if (isLong()) {
            return Long.hashCode(asLong());
        }
        return getValue().hashCode();
    }

}
