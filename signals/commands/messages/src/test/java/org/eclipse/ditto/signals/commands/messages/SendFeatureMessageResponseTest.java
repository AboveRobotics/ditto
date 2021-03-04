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
package org.eclipse.ditto.signals.commands.messages;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.FeatureIdInvalidException;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.Test;

public final class SendFeatureMessageResponseTest {

    @Test
    public void validatesThatFeatureIDIsPresentInMessageHeaders() {
        final String featureId = "foo";
        final ThingId thingId = ThingId.generateRandom();
        final String subject = "bar";
        final MessageHeaders messageHeadersWithoutFeatureId =
                MessageHeaders.newBuilder(MessageDirection.TO, thingId, subject).build();
        final Message<Object> message = Message.newBuilder(messageHeadersWithoutFeatureId).build();
        final DittoHeaders dittoHeaders = DittoHeaders.empty();
        assertThatExceptionOfType(FeatureIdInvalidException.class)
                .isThrownBy(() ->
                        SendFeatureMessageResponse.of(thingId, featureId, message, HttpStatus.MULTI_STATUS,
                                dittoHeaders))
                .withMessage("The Message did not contain a feature ID at all! Expected was feature ID <foo>.");
    }

    @Test
    public void validatesThatFeatureIDIsEqualInHeadersAndWrappingCommand() {
        final String featureId = "foo";
        final ThingId thingId = ThingId.generateRandom();
        final String subject = "bar";
        final MessageHeaders messageHeadersWithDifferentFeatureId =
                MessageHeaders.newBuilder(MessageDirection.TO, thingId, subject)
                        .featureId("bumlux")
                        .build();
        final Message<Object> message = Message.newBuilder(messageHeadersWithDifferentFeatureId).build();
        final DittoHeaders dittoHeaders = DittoHeaders.empty();
        assertThatExceptionOfType(FeatureIdInvalidException.class)
                .isThrownBy(() -> SendFeatureMessageResponse.of(thingId, featureId, message, HttpStatus.MULTI_STATUS,
                        dittoHeaders))
                .withMessage("The Message contained feature ID <bumlux>. Expected was feature ID <foo>.");
    }

    @Test
    public void instantiatingAFeatureMessageResponseWorksWithEqualFeatureIds() {
        final String featureId = "foo";
        final ThingId thingId = ThingId.generateRandom();
        final String subject = "bar";
        final MessageHeaders messageHeadersWithDifferentFeatureId =
                MessageHeaders.newBuilder(MessageDirection.TO, thingId, subject)
                        .featureId("foo")
                        .build();
        final Message<Object> message = Message.newBuilder(messageHeadersWithDifferentFeatureId).build();
        final DittoHeaders dittoHeaders = DittoHeaders.empty();
        assertThatCode(() -> SendFeatureMessageResponse.of(thingId, featureId, message, HttpStatus.MULTI_STATUS,
                                dittoHeaders))
                .doesNotThrowAnyException();
    }

}
