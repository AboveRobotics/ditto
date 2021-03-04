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
package org.eclipse.ditto.signals.commands.things.exceptions;

import java.net.URI;
import java.text.MessageFormat;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeExceptionBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonParsableException;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.ThingException;
import org.eclipse.ditto.model.things.ThingId;

/**
 * Thrown if a Thing could not be created because a linked Policy ID was not existing for example.
 */
@Immutable
@JsonParsableException(errorCode = ThingNotCreatableException.ERROR_CODE)
public final class ThingNotCreatableException extends DittoRuntimeException implements ThingException {

    /**
     * Error code of this exception.
     */
    public static final String ERROR_CODE = ERROR_CODE_PREFIX + "thing.notcreatable";

    private static final String MESSAGE_TEMPLATE = "The Thing with ID ''{0}'' could not be created as the Policy with "
            + "ID ''{1}'' is not existing.";

    private static final String MESSAGE_TEMPLATE_POLICY_CREATION_FAILURE =
            "The Thing with ID ''{0}'' could not be created because creation of its " +
                    "implicit Policy ID ''{1}'' failed.";

    private static final String DEFAULT_DESCRIPTION_NOT_EXISTING =
            "Check if the ID of the Policy you created the Thing with is correct and that the Policy is existing.";

    private static final String DEFAULT_DESCRIPTION_POLICY_CREATION_FAILED =
            "If you want to use an existing Policy, specify it as 'policyId' in the Thing JSON you create.";

    private static final String DEFAULT_DESCRIPTION_GENERIC =
            "Either check if the ID of the Policy you created the Thing with is correct and that the " +
                    "Policy is existing or If you want to use the existing Policy, specify it as 'policyId' " +
                    "in the Thing JSON you create.";

    private static final long serialVersionUID = 2153912949789822362L;

    private ThingNotCreatableException(final DittoHeaders dittoHeaders,
            @Nullable final String message,
            @Nullable final String description,
            @Nullable final Throwable cause,
            @Nullable final URI href) {
        super(ERROR_CODE, HttpStatus.BAD_REQUEST, dittoHeaders, message, description, cause, href);
    }

    /**
     * A mutable builder for a {@code ThingNotCreatableException} thrown if a Thing could not be created because a
     * referenced Policy in the Thing to be created is missing.
     *
     * @param thingId the ID of the Thing.
     * @param policyId the ID of the Policy which was used when creating the Thing.
     * @return the builder.
     */
    public static Builder newBuilderForPolicyMissing(final ThingId thingId, final PolicyId policyId) {
        return new Builder(thingId, policyId, true);
    }

    /**
     * A mutable builder for a {@code ThingNotCreatableException} thrown if a Thing could not be created because
     * the creation of its implicit Policy failed.
     *
     * @param thingId the ID of the Thing.
     * @param policyId the ID of the Policy which was used when creating the Thing.
     * @return the builder.
     */
    public static Builder newBuilderForPolicyExisting(final ThingId thingId, final PolicyId policyId) {
        return new Builder(thingId, policyId, false);
    }

    /**
     * Constructs a new {@code ThingNotCreatableException} object with the given exception message.
     *
     * @param message detail message. This message can be later retrieved by the {@link #getMessage()} method.
     * @param description the description which may be {@code null}.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new ThingNotCreatableException.
     * @throws NullPointerException if {@code dittoHeaders} is {@code null}.
     */
    public static ThingNotCreatableException fromMessage(@Nullable final String message,
            @Nullable final String description,
            final DittoHeaders dittoHeaders) {
        return DittoRuntimeException.fromMessage(message, dittoHeaders,
                new Builder().description(() -> description != null ? description : DEFAULT_DESCRIPTION_GENERIC));
    }

    /**
     * Constructs a new {@code ThingNotCreatableException} object with the exception message extracted from the
     * given JSON object.
     *
     * @param jsonObject the JSON to read the {@link JsonFields#MESSAGE} field from.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new ThingNotCreatableException.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if this JsonObject did not contain an error message.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static ThingNotCreatableException fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {
        return DittoRuntimeException.fromJson(jsonObject, dittoHeaders, new Builder());
    }

    @Override
    public DittoRuntimeException setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new Builder()
                .message(getMessage())
                .description(getDescription().orElse(null))
                .cause(getCause())
                .href(getHref().orElse(null))
                .dittoHeaders(dittoHeaders)
                .build();
    }

    /**
     * A mutable builder with a fluent API for a {@link ThingNotCreatableException}.
     */
    @NotThreadSafe
    public static final class Builder extends DittoRuntimeExceptionBuilder<ThingNotCreatableException> {

        private Builder() {
            description(DEFAULT_DESCRIPTION_GENERIC);
        }

        private Builder(final boolean policyMissing) {
            if (policyMissing) {
                description(DEFAULT_DESCRIPTION_NOT_EXISTING);
            } else {
                description(DEFAULT_DESCRIPTION_POLICY_CREATION_FAILED);
            }
        }

        private Builder(final ThingId thingId, final PolicyId policyId, final boolean policyMissing) {
            this(policyMissing);
            if (policyMissing) {
                message(MessageFormat.format(MESSAGE_TEMPLATE, String.valueOf(thingId), policyId));
            } else {
                message(MessageFormat.format(MESSAGE_TEMPLATE_POLICY_CREATION_FAILURE, String.valueOf(thingId),
                        policyId));
            }
        }

        @Override
        protected ThingNotCreatableException doBuild(final DittoHeaders dittoHeaders,
                @Nullable final String message,
                @Nullable final String description,
                @Nullable final Throwable cause,
                @Nullable final URI href) {
            return new ThingNotCreatableException(dittoHeaders, message, description, cause, href);
        }
    }

}
