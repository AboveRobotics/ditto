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
package org.eclipse.ditto.signals.commands.base;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFieldSelectorInvalidException;
import org.eclipse.ditto.json.JsonMissingFieldException;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonParseException;
import org.eclipse.ditto.json.JsonPointerInvalidException;
import org.eclipse.ditto.model.base.exceptions.DittoHeaderInvalidException;
import org.eclipse.ditto.model.base.exceptions.DittoJsonException;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.exceptions.InvalidRqlExpressionException;
import org.eclipse.ditto.signals.base.AbstractErrorRegistry;
import org.eclipse.ditto.signals.base.ErrorRegistry;
import org.eclipse.ditto.signals.base.JsonParsable;
import org.eclipse.ditto.signals.base.JsonTypeNotParsableException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayAuthenticationFailedException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayAuthenticationProviderUnavailableException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayInternalErrorException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayJwtInvalidException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayJwtIssuerNotSupportedException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayMethodNotAllowedException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayPlaceholderNotResolvableException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayPlaceholderReferenceNotSupportedException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayPlaceholderReferenceUnknownFieldException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayQueryTimeExceededException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayServiceTimeoutException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayServiceTooManyRequestsException;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayServiceUnavailableException;

/**
 * A {@link ErrorRegistry} aware of common {@link DittoRuntimeException}s.
 */
@Immutable
public final class CommonErrorRegistry extends AbstractErrorRegistry<DittoRuntimeException> {

    private CommonErrorRegistry(final Map<String, JsonParsable<DittoRuntimeException>> parseStrategies) {
        super(parseStrategies);
    }

    /**
     * Returns a new {@code CommonErrorRegistry}.
     *
     * @return the error registry.
     */
    public static CommonErrorRegistry newInstance() {
        final Map<String, JsonParsable<DittoRuntimeException>> parseStrategies = new HashMap<>();

        // exceptions in package
        parseStrategies.put(JsonParseException.ERROR_CODE, (jsonObject, dittoHeaders) -> new DittoJsonException(
                JsonParseException.newBuilder().message(getMessage(jsonObject)).build(), dittoHeaders));

        parseStrategies.put(JsonMissingFieldException.ERROR_CODE, (jsonObject, dittoHeaders) -> new DittoJsonException(
                JsonMissingFieldException.newBuilder().message(getMessage(jsonObject)).build(), dittoHeaders));

        parseStrategies.put(JsonFieldSelectorInvalidException.ERROR_CODE,
                (jsonObject, dittoHeaders) -> new DittoJsonException(
                        JsonFieldSelectorInvalidException.newBuilder().message(getMessage(jsonObject)).build(),
                        dittoHeaders));

        parseStrategies.put(JsonPointerInvalidException.ERROR_CODE,
                (jsonObject, dittoHeaders) -> new DittoJsonException(
                        JsonPointerInvalidException.newBuilder().message(getMessage(jsonObject)).build(),
                        dittoHeaders));

        // exceptions in package org.eclipse.ditto.model.base.exceptions
        parseStrategies.put(DittoHeaderInvalidException.ERROR_CODE,
                DittoHeaderInvalidException::fromJson);

        parseStrategies.put(InvalidRqlExpressionException.ERROR_CODE,
                InvalidRqlExpressionException::fromJson);

        // exception in package org.eclipse.ditto.signals.commands.base
        parseStrategies.put(CommandNotSupportedException.
                ERROR_CODE, CommandNotSupportedException::fromJson);

        // exception in package org.eclipse.ditto.signals.base
        parseStrategies.put(JsonTypeNotParsableException.ERROR_CODE,
                JsonTypeNotParsableException::fromJson);

        // exceptions in package org.eclipse.ditto.signals.commands.base.exceptions
        parseStrategies.put(GatewayAuthenticationFailedException.ERROR_CODE,
                GatewayAuthenticationFailedException::fromJson);

        parseStrategies.put(GatewayAuthenticationProviderUnavailableException.ERROR_CODE,
                GatewayAuthenticationProviderUnavailableException::fromJson);

        parseStrategies.put(GatewayInternalErrorException.ERROR_CODE,
                GatewayInternalErrorException::fromJson);

        parseStrategies.put(GatewayJwtInvalidException.ERROR_CODE,
                GatewayJwtInvalidException::fromJson);

        parseStrategies.put(GatewayJwtIssuerNotSupportedException.ERROR_CODE,
                GatewayJwtIssuerNotSupportedException::fromJson);

        parseStrategies.put(GatewayMethodNotAllowedException.ERROR_CODE,
                GatewayMethodNotAllowedException::fromJson);

        parseStrategies.put(GatewayPlaceholderNotResolvableException.ERROR_CODE,
                GatewayPlaceholderNotResolvableException::fromJson);

        parseStrategies.put(GatewayQueryTimeExceededException.ERROR_CODE,
                GatewayQueryTimeExceededException::fromJson);

        parseStrategies.put(GatewayServiceTimeoutException.ERROR_CODE,
                GatewayServiceTimeoutException::fromJson);

        parseStrategies.put(GatewayServiceTooManyRequestsException.ERROR_CODE,
                GatewayServiceTooManyRequestsException::fromJson);

        parseStrategies.put(GatewayServiceUnavailableException.ERROR_CODE,
                GatewayServiceUnavailableException::fromJson);

        parseStrategies.put(GatewayPlaceholderReferenceNotSupportedException.ERROR_CODE,
                GatewayPlaceholderReferenceNotSupportedException::fromJson);

        parseStrategies.put(GatewayPlaceholderReferenceUnknownFieldException.ERROR_CODE,
                GatewayPlaceholderReferenceUnknownFieldException::fromJson);

        return new CommonErrorRegistry(parseStrategies);
    }


    private static String getMessage(final JsonObject jsonObject) {
        return jsonObject.getValue(DittoJsonException.JsonFields.MESSAGE)
                .orElseThrow(() -> JsonMissingFieldException.newBuilder()
                        .fieldName(DittoRuntimeException.JsonFields.MESSAGE.getPointer().toString()).build());
    }

}
