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
package org.eclipse.ditto.signals.commands.messages;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;

/**
 * Command to send a Claim response {@link Message} <em>FROM</em> a Thing.
 *
 * @param <T> the type of the message's payload.
 */
@JsonParsableCommandResponse(type = SendClaimMessageResponse.TYPE)
public final class SendClaimMessageResponse<T> extends AbstractMessageCommandResponse<T, SendClaimMessageResponse<T>> {

    /**
     * The name of the {@code Message} wrapped by this {@code MessageCommand}.
     */
    public static final String NAME = "claimResponseMessage";

    /**
     * Type of this command.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    private SendClaimMessageResponse(final ThingId thingId,
            final Message<T> message,
            final HttpStatus responseHttpStatus,
            final DittoHeaders dittoHeaders) {

        super(TYPE, thingId, message, responseHttpStatus, dittoHeaders);
    }

    @Override
    public SendClaimMessageResponse<T> setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(getThingEntityId(), getMessage(), getHttpStatus(), dittoHeaders);
    }

    /**
     * Creates a new instance of {@code SendClaimMessageResponse}.
     *
     * @param thingId the ID of the Thing to send the message from.
     * @param message the claim response message to send from the Thing.
     * @param responseStatusCode the optional status code of this response.
     * @param dittoHeaders the DittoHeaders of this message.
     * @param <T> the type of the message's payload.
     * @return new instance of {@code SendClaimMessageResponse}.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated Thing ID is now typed. Use {@link #of(ThingId, Message, HttpStatus, DittoHeaders)} instead.
     */
    @Deprecated
    public static <T> SendClaimMessageResponse<T> of(final String thingId,
            final Message<T> message,
            final HttpStatusCode responseStatusCode,
            final DittoHeaders dittoHeaders) {

        return of(ThingId.of(thingId), message, responseStatusCode.getAsHttpStatus(), dittoHeaders);
    }

    /**
     * Creates a new instance of {@code SendClaimMessageResponse}.
     *
     * @param thingId the ID of the Thing to send the message from.
     * @param message the claim response message to send from the Thing.
     * @param responseStatusCode the optional status code of this response.
     * @param dittoHeaders the DittoHeaders of this message.
     * @param <T> the type of the message's payload.
     * @return new instance of {@code SendClaimMessageResponse}.
     * @throws NullPointerException if any argument is {@code null}.
     * @deprecated as of 2.0.0 please use {@link #of(ThingId, Message, HttpStatus, DittoHeaders)} instead.
     */
    @Deprecated
    public static <T> SendClaimMessageResponse<T> of(final ThingId thingId,
            final Message<T> message,
            final HttpStatusCode responseStatusCode,
            final DittoHeaders dittoHeaders) {

        return of(thingId, message, responseStatusCode.getAsHttpStatus(), dittoHeaders);
    }

    /**
     * Creates a new instance of {@code SendClaimMessageResponse}.
     *
     * @param thingId the ID of the Thing to send the message from.
     * @param message the claim response message to send from the Thing.
     * @param responseHttpStatus the optional HTTP status of this response.
     * @param dittoHeaders the DittoHeaders of this message.
     * @param <T> the type of the message's payload.
     * @return new instance of {@code SendClaimMessageResponse}.
     * @throws NullPointerException if any argument is {@code null}.
     * @since 2.0.0
     */
    public static <T> SendClaimMessageResponse<T> of(final ThingId thingId,
            final Message<T> message,
            final HttpStatus responseHttpStatus,
            final DittoHeaders dittoHeaders) {

        return new SendClaimMessageResponse<>(thingId, message, responseHttpStatus, dittoHeaders);
    }

    /**
     * Creates a new {@code SendClaimMessageResponse} from a JSON string.
     *
     * @param jsonString the JSON string of which the SendClaimMessageResponse is to be created.
     * @param dittoHeaders the headers.
     * @return the command.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected format.
     */
    public static SendClaimMessageResponse<?> fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code SendClaimMessageResponse} from a JSON object.
     *
     * @param jsonObject the JSON object of which the SendClaimMessageResponse is to be created.
     * @param dittoHeaders the headers.
     * @return the command.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected format.
     */
    public static SendClaimMessageResponse<?> fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return new CommandResponseJsonDeserializer<SendClaimMessageResponse<?>>(TYPE, jsonObject).deserialize(
                httpStatus -> of(
                        ThingId.of(jsonObject.getValueOrThrow(MessageCommandResponse.JsonFields.JSON_THING_ID)),
                        deserializeMessageFromJson(jsonObject),
                        httpStatus,
                        dittoHeaders));
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof SendClaimMessageResponse;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + "]";
    }

}
