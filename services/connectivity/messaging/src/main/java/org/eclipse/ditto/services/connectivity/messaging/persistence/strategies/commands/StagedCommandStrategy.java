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
package org.eclipse.ditto.services.connectivity.messaging.persistence.strategies.commands;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionState;
import org.eclipse.ditto.services.connectivity.messaging.persistence.stages.StagedCommand;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.events.connectivity.ConnectivityEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.services.connectivity.messaging.persistence.stages.StagedCommand}
 * command.
 */
final class StagedCommandStrategy extends AbstractConnectivityCommandStrategy<StagedCommand> {

    StagedCommandStrategy() {
        super(StagedCommand.class);
    }

    @Override
    protected Result<ConnectivityEvent<?>> doApply(final Context<ConnectionState> context,
            @Nullable final Connection entity,
            final long nextRevision,
            final StagedCommand command,
            @Nullable final Metadata metadata) {

        return ResultFactory.newMutationResult(command, command.getEvent(), command.getResponse());
    }
}
