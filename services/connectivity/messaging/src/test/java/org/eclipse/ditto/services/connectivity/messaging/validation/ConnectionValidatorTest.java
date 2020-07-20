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
package org.eclipse.ditto.services.connectivity.messaging.validation;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.services.connectivity.messaging.TestConstants.Authorization;
import static org.eclipse.ditto.services.connectivity.messaging.TestConstants.Certificates;
import static org.mutabilitydetector.unittesting.AllowedReason.assumingFields;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.ClientCertificateCredentials;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionConfigurationInvalidException;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectionType;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.ConnectivityStatus;
import org.eclipse.ditto.model.connectivity.PayloadMappingDefinition;
import org.eclipse.ditto.model.connectivity.Source;
import org.eclipse.ditto.model.connectivity.SourceBuilder;
import org.eclipse.ditto.model.connectivity.Target;
import org.eclipse.ditto.model.connectivity.Topic;
import org.eclipse.ditto.model.query.filter.QueryFilterCriteriaFactory;
import org.eclipse.ditto.services.connectivity.mapping.MapperLimitsConfig;
import org.eclipse.ditto.services.connectivity.mapping.NormalizedMessageMapper;
import org.eclipse.ditto.services.connectivity.messaging.TestConstants;
import org.eclipse.ditto.services.connectivity.messaging.amqp.AmqpValidator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.typesafe.config.ConfigValueFactory;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Host;
import akka.http.javadsl.model.Uri;
import akka.testkit.javadsl.TestKit;

/**
 * Tests {@link org.eclipse.ditto.services.connectivity.messaging.validation.ConnectionValidator}.
 */
public class ConnectionValidatorTest {

    private static final ConnectionId CONNECTION_ID = TestConstants.createRandomConnectionId();
    protected static final MapperLimitsConfig MAPPER_LIMITS_CONFIG =
            TestConstants.MAPPING_CONFIG.getMapperLimitsConfig();

    private static ActorSystem actorSystem;

    @BeforeClass
    public static void setUp() {
        actorSystem = ActorSystem.create("AkkaTestSystem",
                TestConstants.CONFIG.withValue("ditto.connectivity.connection.blacklisted-hostnames",
                        ConfigValueFactory.fromAnyRef("8.8.8.8,2001:4860:4860:0000:0000:0000:0000:0001")));
    }

    @AfterClass
    public static void tearDown() {
        if (actorSystem != null) {
            TestKit.shutdownActorSystem(actorSystem, scala.concurrent.duration.Duration.apply(5, TimeUnit.SECONDS),
                    false);
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testImmutability() {
        assertInstancesOf(ConnectionValidator.class,
                areImmutable(),
                // mutability-detector cannot detect that maps built from stream collectors are safely copied.
                assumingFields("specMap").areSafelyCopiedUnmodifiableCollectionsWithImmutableElements(),
                provided(QueryFilterCriteriaFactory.class).isAlsoImmutable());
    }

    @Test
    public void acceptValidConnection() {
        final Connection connection = createConnection(CONNECTION_ID);
        final ConnectionValidator underTest = ConnectionValidator.of(
                MAPPER_LIMITS_CONFIG,
                AmqpValidator.newInstance());
        underTest.validate(connection, DittoHeaders.empty(), actorSystem);
    }

    @Test
    public void rejectConnectionWithSourceWithoutAddresses() {
        final Connection connection =
                ConnectivityModelFactory.newConnectionBuilder(CONNECTION_ID,
                        ConnectionType.AMQP_10, ConnectivityStatus.OPEN, "amqp://localhost:5671")
                        .sources(singletonList(
                                ConnectivityModelFactory.newSourceBuilder()
                                        .authorizationContext(Authorization.AUTHORIZATION_CONTEXT)
                                        .consumerCount(0)
                                        .index(1)
                                        .build()))
                        .build();

        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    @Test
    public void rejectConnectionWithEmptySourceAddress() {
        final Connection connection =
                ConnectivityModelFactory.newConnectionBuilder(CONNECTION_ID,
                        ConnectionType.AMQP_10, ConnectivityStatus.OPEN, "amqp://localhost:5671")
                        .sources(singletonList(
                                ConnectivityModelFactory.newSourceBuilder()
                                        .authorizationContext(Authorization.AUTHORIZATION_CONTEXT)
                                        .address("")
                                        .consumerCount(1)
                                        .index(0)
                                        .build()))
                        .build();

        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    @Test
    public void rejectConnectionWithEmptyTargetAddress() {
        final Connection connection = ConnectivityModelFactory.newConnectionBuilder(CONNECTION_ID,
                ConnectionType.AMQP_10, ConnectivityStatus.OPEN, "amqp://localhost:5671")
                .targets(Collections.singletonList(ConnectivityModelFactory.newTargetBuilder()
                        .address("")
                        .authorizationContext(Authorization.AUTHORIZATION_CONTEXT)
                        .topics(Topic.LIVE_MESSAGES)
                        .build()))
                .build();

        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    @Test
    public void rejectConnectionWithInvalidNormalizerMapperJsonFieldSelector() {
        final Map<String, String> normalizerMessageMapperOptions = new HashMap<>();
        normalizerMessageMapperOptions.put(NormalizedMessageMapper.FIELDS, "foo(bar");

        final Connection connection =
                ConnectivityModelFactory.newConnectionBuilder(CONNECTION_ID,
                        ConnectionType.AMQP_10, ConnectivityStatus.OPEN, "amqp://localhost:5671")
                        .targets(TestConstants.Targets.TARGETS)
                        .mappingContext(ConnectivityModelFactory.newMappingContext(
                                NormalizedMessageMapper.class.getName(),
                                normalizerMessageMapperOptions
                        ))
                        .build();

        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    @Test
    public void rejectConnectionWithIllFormedTrustedCertificates() {
        final Connection connection = createConnection(CONNECTION_ID).toBuilder()
                .trustedCertificates("Wurst")
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    @Test
    public void acceptConnectionWithTrustedCertificates() {
        final String trustedCertificates = String.join("\n",
                Certificates.CA_CRT,
                Certificates.SERVER_CRT,
                Certificates.CLIENT_CRT,
                Certificates.CLIENT_SELF_SIGNED_CRT);
        final Connection connection = createConnection(CONNECTION_ID).toBuilder()
                .trustedCertificates(trustedCertificates)
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        underTest.validate(connection, DittoHeaders.empty(), actorSystem);
    }

    @Test
    public void rejectIllFormedClientCertificate() {
        final Connection connection = createConnection(CONNECTION_ID).toBuilder()
                .credentials(ClientCertificateCredentials.newBuilder()
                        .clientKey(Certificates.CLIENT_KEY)
                        .clientCertificate("Wurst")
                        .build())
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    @Test
    public void rejectIllFormedClientKey() {
        final Connection connection = createConnection(CONNECTION_ID).toBuilder()
                .credentials(ClientCertificateCredentials.newBuilder()
                        .clientKey("-----BEGIN RSA PRIVATE KEY-----\nWurst\n-----END RSA PRIVATE KEY-----")
                        .clientCertificate(Certificates.CLIENT_CRT)
                        .build())
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));

    }

    @Test
    public void acceptClientCertificate() {
        final Connection connection = createConnection(CONNECTION_ID).toBuilder()
                .credentials(ClientCertificateCredentials.newBuilder()
                        .clientKey(Certificates.CLIENT_KEY)
                        .clientCertificate(Certificates.CLIENT_CRT)
                        .build())
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        underTest.validate(connection, DittoHeaders.empty(), actorSystem);
    }

    @Test
    public void testInvalidHosts() {
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        // wildcard
        expectConnectionConfigurationInvalid(underTest, getConnectionWithHost("0.0.0.0"));
        // blacklisted
        expectConnectionConfigurationInvalid(underTest, getConnectionWithHost("8.8.8.8"));
        // loopback
        expectConnectionConfigurationInvalid(underTest, getConnectionWithHost("[::1]"));
        // private
        expectConnectionConfigurationInvalid(underTest, getConnectionWithHost("192.168.0.1"));
        // multicast
        expectConnectionConfigurationInvalid(underTest, getConnectionWithHost("224.0.1.1"));
    }

    @Test
    public void expectExceptionForInvalidHost() {
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class).isThrownBy(
                () -> ConnectionValidator.isHostForbidden(Host.create("ditto"),
                        singletonList(InetAddress.getLoopbackAddress())));
    }

    private static void expectConnectionConfigurationInvalid(final ConnectionValidator underTest,
            final Connection connection) {
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem));
    }

    private static Connection getConnectionWithHost(final String host) {
        final Connection template = createConnection(CONNECTION_ID);
        final Uri newUri = Uri.create(template.getUri()).host(host);
        return template.toBuilder().uri(newUri.toString()).build();
    }

    private static Connection createConnection(final ConnectionId connectionId) {
        return TestConstants.createConnection(connectionId).toBuilder()
                .uri("amqps://8.8.4.4:443")
                .build();
    }

    @Test
    public void acceptValidConnectionWithValidNumberPayloadMapping() {
        final Connection connection = createConnection(CONNECTION_ID)
                .toBuilder()
                .setSources(TestConstants.Sources.SOURCES_WITH_VALID_MAPPING_NUMBER)
                .setTargets(TestConstants.Targets.TARGET_WITH_VALID_MAPPING_NUMBER)
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        underTest.validate(connection, DittoHeaders.empty(), actorSystem);
    }

    @Test
    public void rejectValidConnectionWithInvalidNumberSourcePayloadMapping() {
        exception.expect(ConnectionConfigurationInvalidException.class);
        final Connection connection = createConnection(CONNECTION_ID)
                .toBuilder()
                .setSources(TestConstants.Sources.SOURCES_WITH_INVALID_MAPPING_NUMBER)
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        underTest.validate(connection, DittoHeaders.empty(), actorSystem);
    }

    @Test
    public void rejectValidConnectionWithInvalidNumberTargetPayloadMapping() {
        exception.expect(ConnectionConfigurationInvalidException.class);
        final Connection connection = createConnection(CONNECTION_ID)
                .toBuilder()
                .setTargets(TestConstants.Targets.TARGET_WITH_INVALID_MAPPING_NUMBER)
                .build();
        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        underTest.validate(connection, DittoHeaders.empty(), actorSystem);
    }

    @Test
    public void rejectInvalidPayloadMappingReferenceInTarget() {
        final List<Target> targetWithInvalidMapping = singletonList(
                ConnectivityModelFactory.newTargetBuilder(TestConstants.Targets.TWIN_TARGET).payloadMapping(
                        ConnectivityModelFactory.newPayloadMapping("invalid")).build());

        rejectInvalidPayloadMappingReferenceInTarget(emptyList(), targetWithInvalidMapping);
    }

    @Test
    public void rejectInvalidPayloadMappingReferenceInSource() {
        final List<Source> sourceWithInvalidMapping =
                TestConstants.Sources.SOURCES_WITH_AUTH_CONTEXT.stream()
                        .map(ConnectivityModelFactory::newSourceBuilder)
                        .map(b -> b.payloadMapping(ConnectivityModelFactory.newPayloadMapping("invalid")))
                        .map(SourceBuilder::build)
                        .collect(Collectors.toList());
        rejectInvalidPayloadMappingReferenceInTarget(sourceWithInvalidMapping, emptyList());
    }

    private void rejectInvalidPayloadMappingReferenceInTarget(List<Source> sources, List<Target> targets) {
        final PayloadMappingDefinition payloadMappingDefinition =
                ConnectivityModelFactory.newPayloadMappingDefinition("status",
                        ConnectivityModelFactory.newMappingContext("ConnectionStatus",
                                singletonMap("thingId", "{{ header:device_id }}")));
        final Connection connection = createConnection(CONNECTION_ID)
                .toBuilder()
                .payloadMappingDefinition(payloadMappingDefinition)
                .setTargets(targets)
                .setSources(sources)
                .build();

        final ConnectionValidator underTest = ConnectionValidator.of(MAPPER_LIMITS_CONFIG, AmqpValidator.newInstance());
        assertThatExceptionOfType(ConnectionConfigurationInvalidException.class)
                .isThrownBy(() -> underTest.validate(connection, DittoHeaders.empty(), actorSystem))
                .withMessageContaining("invalid");
    }
}
