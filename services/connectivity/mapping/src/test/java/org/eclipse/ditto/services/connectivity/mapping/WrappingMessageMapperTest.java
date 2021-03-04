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
package org.eclipse.ditto.services.connectivity.mapping;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.MessageMappingFailedException;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.services.connectivity.config.mapping.DefaultMappingConfig;
import org.eclipse.ditto.services.connectivity.config.mapping.MappingConfig;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.typesafe.config.ConfigFactory;

/**
 * Tests for {@link WrappingMessageMapper}.
 */
public class WrappingMessageMapperTest {

    private static final JsonObject DEFAULT_OPTIONS = JsonObject.newBuilder().set("default", "option").build();
    private MessageMapper mockMapper;
    private MessageMapper underTest;
    private MessageMapperConfiguration mockConfiguration;
    private ExternalMessage mockMessage;
    private Adaptable mockAdaptable;

    private MappingConfig mapperLimitsConfig;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        mockMapper = mock(MessageMapper.class);
        mockConfiguration = mock(MessageMapperConfiguration.class);
        mockMessage = mock(ExternalMessage.class);
        mockAdaptable = mock(Adaptable.class);

        when(mockMapper.map(any(ExternalMessage.class))).thenReturn(singletonList(mockAdaptable));
        when(mockMapper.map(mockAdaptable)).thenReturn(singletonList(mockMessage));
        when(mockMapper.getId()).thenReturn("mockMapper");
        when(mockMapper.getDefaultOptions()).thenReturn(DEFAULT_OPTIONS);
        when(mockAdaptable.getTopicPath()).thenReturn(ProtocolFactory.emptyTopicPath());
        when(mockAdaptable.getDittoHeaders()).thenReturn(DittoHeaders.empty());
        when(mockAdaptable.getPayload()).thenReturn(ProtocolFactory.newPayload("{\"path\":\"/\"}"));
        when(mockMessage.getInternalHeaders()).thenReturn(DittoHeaders.empty());

        mapperLimitsConfig = DefaultMappingConfig.of(ConfigFactory.load("mapping-test"));
        underTest = WrappingMessageMapper.wrap(mockMapper);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void configure() {
        when(mockConfiguration.getContentTypeBlocklist()).thenReturn(Collections.singletonList("blockedContentType"));

        underTest.configure(mapperLimitsConfig, mockConfiguration);
        verify(mockMapper).configure(mapperLimitsConfig, mockConfiguration);
    }

    @Test
    public void mapMessage() {
        underTest.configure(mapperLimitsConfig, mockConfiguration);
        underTest.map(mockMessage);
        verify(mockMapper).map(any(ExternalMessage.class));
        verify(mockMapper).map(any(ExternalMessage.class));
    }

    @Test
    public void mapAdaptable() {
        final DittoHeaders headers =
                DittoHeaders.of(Collections.singletonMap(ExternalMessage.CONTENT_TYPE_HEADER, "contentType"));
        when(mockAdaptable.getDittoHeaders()).thenReturn(headers);

        underTest.configure(mapperLimitsConfig, mockConfiguration);
        underTest.map(mockAdaptable);
        verify(mockMapper).map(mockAdaptable);
    }

    @Test
    public void mapMessageWithInvalidNumberOfMessages() {
        exception.expect(MessageMappingFailedException.class);
        final List<Adaptable> adaptables = listOfElements(mockAdaptable,
                mapperLimitsConfig.getMapperLimitsConfig().getMaxMappedInboundMessages());
        when(mockMapper.map(any(ExternalMessage.class))).thenReturn(adaptables);

        underTest.configure(mapperLimitsConfig, mockConfiguration);
        underTest.map(mockMessage);
    }

    @Test
    public void mapAdaptableWithInvalidNumberOfMessages() {
        exception.expect(MessageMappingFailedException.class);
        final List<ExternalMessage> externalMessages =
                listOfElements(mockMessage,
                        mapperLimitsConfig.getMapperLimitsConfig().getMaxMappedOutboundMessages());
        when(mockMapper.map(any(Adaptable.class))).thenReturn(externalMessages);

        underTest.configure(mapperLimitsConfig, mockConfiguration);
        underTest.map(mockAdaptable);
    }

    @Test
    public void getDefaultOptions() {
        Assertions.assertThat(underTest.getDefaultOptions()).isEqualTo(DEFAULT_OPTIONS);
    }

    private <T> List<T> listOfElements(final T elementInList, final int numberOfElements) {
        final List<T> list = new ArrayList<>();
        for (int i = 0; i < numberOfElements + 1; i++) {
            list.add(elementInList);
        }
        return list;
    }
}
