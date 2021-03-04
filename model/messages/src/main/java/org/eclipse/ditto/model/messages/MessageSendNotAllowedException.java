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
package org.eclipse.ditto.model.messages;

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
import org.eclipse.ditto.model.things.ThingId;

/**
 * Thrown if a message cannot be send because the affected thing does not exist or because of a missing permission.
 */
@Immutable
@JsonParsableException(errorCode = MessageSendNotAllowedException.ERROR_CODE)
public final class MessageSendNotAllowedException extends DittoRuntimeException implements MessageException {

    /**
     * Error code of this exception.
     */
    public static final String ERROR_CODE = ERROR_CODE_PREFIX + "send.notallowed";

    private static final String MESSAGE_TEMPLATE =
            "You are not allowed to send messages for the Thing with id ''{0}''!";

    private static final String DEFAULT_DESCRIPTION =
            "Please make sure that the Thing exists and that you have a WRITE permission on the Thing.";

    private static final long serialVersionUID = -7767643705375184154L;

    /**
     * Constructs a new {@code MessageSendNotAllowedException} object.
     *
     * @param thingId the ID of the Thing for which a message should be sent.
     */
    public MessageSendNotAllowedException(@Nullable final String thingId) {
        this(DittoHeaders.empty(), MessageFormat.format(MESSAGE_TEMPLATE, thingId), DEFAULT_DESCRIPTION, null, null);
    }

    private MessageSendNotAllowedException(final DittoHeaders dittoHeaders,
            @Nullable final String message,
            @Nullable final String description,
            @Nullable final Throwable cause,
            @Nullable final URI href) {
        super(ERROR_CODE, HttpStatus.FORBIDDEN, dittoHeaders, message, description, cause, href);
    }

    /**
     * A mutable builder for a {@code MessageNotSendableException}.
     *
     * @param thingId the ID of the Thing for which a message should be sent.
     * @return the builder.
     * @deprecated Thing ID is now typed. Use
     * {@link #newBuilder(org.eclipse.ditto.model.things.ThingId)}
     * instead.
     */
    @Deprecated
    public static Builder newBuilder(@Nullable final String thingId) {
        return newBuilder(ThingId.of(thingId));
    }

    /**
     * A mutable builder for a {@code MessageNotSendableException}.
     *
     * @param thingId the ID of the Thing for which a message should be sent.
     * @return the builder.
     */
    public static Builder newBuilder(@Nullable final ThingId thingId) {
        return new Builder(thingId);
    }

    /**
     * Constructs a new {@code MessageNotSendableException} object with the given exception message.
     *
     * @param message detail message. This message can be later retrieved by the {@link #getMessage()} method.
     * @return the new SubjectInvalidException.
     * @deprecated since DittoHeaders are required for the builder. Use {@code #fromMessage(String, DittoHeaders)} instead.
     */
    @Deprecated
    public static MessageSendNotAllowedException fromMessage(@Nullable final String message) {
        return new Builder()
                .message(message)
                .build();
    }

    /**
     * Constructs a new {@code MessageSendNotAllowedException} object with given message.
     *
     * @param message detail message. This message can be later retrieved by the {@link #getMessage()} method.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new MessageSendNotAllowedException.
     * @throws NullPointerException if {@code dittoHeaders} is {@code null}.
     */
    public static MessageSendNotAllowedException fromMessage(@Nullable final String message,
            final DittoHeaders dittoHeaders) {
        return DittoRuntimeException.fromMessage(message, dittoHeaders, new Builder());
    }

    /**
     * Deserialize from a JSON object.
     *
     * @param jsonObject the JSON object to deserialize.
     * @param dittoHeaders the headers.
     * @return an instance of this class.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if this JsonObject did not contain an error message.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static MessageSendNotAllowedException fromJson(final JsonObject jsonObject,
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
     * A mutable builder with a fluent API for a {@link MessageSendNotAllowedException}.
     */
    @NotThreadSafe
    public static final class Builder extends DittoRuntimeExceptionBuilder<MessageSendNotAllowedException> {

        private Builder() {
            description(DEFAULT_DESCRIPTION);
        }

        private Builder(@Nullable final ThingId thingId) {
            this();
            message(MessageFormat.format(MESSAGE_TEMPLATE, thingId));
        }

        @Override
        protected MessageSendNotAllowedException doBuild(final DittoHeaders dittoHeaders,
                @Nullable final String message,
                @Nullable final String description,
                @Nullable final Throwable cause,
                @Nullable final URI href) {
            return new MessageSendNotAllowedException(dittoHeaders, message, description, cause, href);
        }

    }

}
