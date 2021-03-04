/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.signals.commands.cleanup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.json.assertions.DittoJsonAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.entity.id.DefaultEntityId;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link CleanupPersistenceResponse}.
 */
public class CleanupPersistenceResponseTest {

    private static final EntityId ID = DefaultEntityId.of("thing:eclipse:ditto");
    private static final JsonObject KNOWN_JSON = JsonObject.newBuilder()
            .set(CommandResponse.JsonFields.TYPE, CleanupPersistenceResponse.TYPE)
            .set(CleanupCommandResponse.JsonFields.ENTITY_ID, ID.toString())
            .set(CommandResponse.JsonFields.STATUS, HttpStatus.OK.getCode())
            .build();
    private static final DittoHeaders HEADERS = DittoHeaders.newBuilder().correlationId("123").build();

    @Test
    public void assertImmutability() {
        assertInstancesOf(CleanupPersistenceResponse.class, areImmutable(), provided(EntityId.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(CleanupPersistenceResponse.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .verify();
    }

    @Test
    public void toJsonReturnsExpected() {
        final JsonObject jsonObject = CleanupPersistenceResponse.success(ID, DittoHeaders.empty()).toJson();
        assertThat(jsonObject).isEqualTo(KNOWN_JSON);
    }

    @Test
    public void fromJsonReturnsExpected() {
        final CleanupPersistenceResponse commandFromJson = CleanupPersistenceResponse.fromJson(KNOWN_JSON, HEADERS);
        final CleanupPersistenceResponse expectedCommand = CleanupPersistenceResponse.success(ID, HEADERS);
        assertThat(commandFromJson).isEqualTo(expectedCommand);
    }

}
