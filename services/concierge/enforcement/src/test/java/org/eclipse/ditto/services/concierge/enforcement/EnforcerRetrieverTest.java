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
package org.eclipse.ditto.services.concierge.enforcement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.entity.id.DefaultEntityId;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.enforcers.Enforcer;
import org.eclipse.ditto.services.models.caching.EntityId;
import org.eclipse.ditto.services.models.caching.Entry;
import org.eclipse.ditto.services.utils.cache.Cache;
import org.eclipse.ditto.services.utils.cache.EntityIdWithResourceType;
import org.eclipse.ditto.services.utils.cache.entry.Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnforcerRetrieverTest {

    @Mock
    private Cache<EntityIdWithResourceType, Entry<EntityIdWithResourceType>> idCache;
    @Mock
    private Cache<EntityIdWithResourceType, Entry<Enforcer>> enforcerCache;

    private EnforcerRetriever<Enforcer> retriever;

    @Before
    public void setUp() {
        retriever = new EnforcerRetriever<Enforcer>(idCache, enforcerCache);
    }

    @Test
    public void verifyLookupRevealsInnerException() throws ExecutionException, InterruptedException {
        final DittoRuntimeException expectedException =
                DittoRuntimeException.newBuilder("this should be happening", HttpStatus.HTTPVERSION_NOT_SUPPORTED)
                        .build();
        final EntityIdWithResourceType entityId = EntityIdWithResourceType.of("any", DefaultEntityId.of("id"));
        when(idCache.get(any(EntityIdWithResourceType.class))).thenReturn(
                CompletableFuture.completedFuture(Optional.of(Entry.nonexistent())));

        final CompletionStage<Contextual<WithDittoHeaders<?>>> result =
                retriever.retrieve(entityId, (entityIdEntry, enforcerEntry) -> {
                    throw expectedException;
                });

        verify(idCache).get(entityId);
        verifyNoInteractions(enforcerCache);
        verifyException(result, expectedException);
    }

    @Test
    public void verifyLookupRevealsInnermostException() throws ExecutionException, InterruptedException {
        final DittoRuntimeException expectedException =
                DittoRuntimeException.newBuilder("this should be happening", HttpStatus.HTTPVERSION_NOT_SUPPORTED)
                        .build();
        final EntityIdWithResourceType entityId = EntityIdWithResourceType.of("any", DefaultEntityId.of("id"));
        final EntityIdWithResourceType innerEntityId =
                EntityIdWithResourceType.of("other", DefaultEntityId.of("randomId"));
        when(idCache.get(any(EntityIdWithResourceType.class))).thenReturn(
                CompletableFuture.completedFuture(Optional.of(Entry.permanent(innerEntityId))));
        when(enforcerCache.get(any(EntityIdWithResourceType.class))).thenReturn(
                CompletableFuture.completedFuture(Optional.of(Entry.nonexistent())));
        final CompletionStage<Contextual<WithDittoHeaders<?>>> result =
                retriever.retrieve(entityId, (entityIdEntry, enforcerEntry) -> {
                    throw expectedException;
                });

        verify(idCache).get(entityId);
        verify(enforcerCache).get(innerEntityId);
        verifyException(result, expectedException);
    }

    @Test
    public void verifyLookupEnforcerRevealsException() throws ExecutionException, InterruptedException {
        final DittoRuntimeException expectedException =
                DittoRuntimeException.newBuilder("this should be happening", HttpStatus.HTTPVERSION_NOT_SUPPORTED)
                        .build();
        final EntityIdWithResourceType entityId = EntityIdWithResourceType.of("any", DefaultEntityId.of("id"));
        when(enforcerCache.get(any(EntityIdWithResourceType.class))).thenReturn(
                CompletableFuture.completedFuture(Optional.of(Entry.nonexistent())));

        final CompletionStage<Contextual<WithDittoHeaders<?>>> result =
                retriever.retrieveByEnforcerKey(entityId, enforcerEntry -> {
                    throw expectedException;
                });

        verify(enforcerCache).get(entityId);
        verifyException(result, expectedException);
    }

    private static void verifyException(final CompletionStage<Contextual<WithDittoHeaders<?>>> completionStage,
            final Throwable expectedException) throws ExecutionException, InterruptedException {
        assertThat(completionStage.thenApply(_void -> new RuntimeException("this should not be happening"))
                .exceptionally(executionException -> (RuntimeException) executionException.getCause())
                .toCompletableFuture()
                .get())
                .isEqualTo(expectedException);
    }

}
