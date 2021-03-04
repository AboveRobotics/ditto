/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.model.base.headers;

import java.text.MessageFormat;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.exceptions.DittoHeaderInvalidException;

/**
 * This validator parses a CharSequence value and ensures that it was non-empty.
 * If it was empty, a {@link org.eclipse.ditto.model.base.exceptions.DittoHeaderInvalidException} is thrown.
 *
 * @since 1.3.0
 */
@Immutable
final class NonEmptyValueValidator extends AbstractHeaderValueValidator {

    private static final String MESSAGE_TEMPLATE = "The value of the header ''{0}'' must not be empty.";

    private static final NonEmptyValueValidator INSTANCE = new NonEmptyValueValidator();

    private NonEmptyValueValidator() {
        super(CharSequence.class::isAssignableFrom);
    }

    static NonEmptyValueValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected void validateValue(final HeaderDefinition definition, final CharSequence value) {
        if (value.length() < 1) {
            final String message = MessageFormat.format(MESSAGE_TEMPLATE, definition.getKey());
            throw DittoHeaderInvalidException.newCustomMessageBuilder(message).build();
        }
    }

}
