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
package org.eclipse.ditto.signals.commands.things.acks;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.acks.AbstractCommandAckRequestSetter;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.signals.commands.things.ThingCommand;

/**
 * This UnaryOperator accepts a ThingCommand and checks whether its DittoHeaders should be extended by an
 * {@link AcknowledgementRequest} for {@link DittoAcknowledgementLabel#LIVE_RESPONSE}.
 * <p>
 * If so, the result is a new command with extended headers, else the same command is returned.
 * </p>
 *
 * @since 1.2.0
 */
@Immutable
public final class ThingLiveCommandAckRequestSetter extends AbstractCommandAckRequestSetter<ThingCommand<?>> {

    private static final ThingLiveCommandAckRequestSetter INSTANCE = new ThingLiveCommandAckRequestSetter();

    private ThingLiveCommandAckRequestSetter() {
        super(DittoAcknowledgementLabel.LIVE_RESPONSE);
    }

    /**
     * Returns an instance of {@code ThingLiveCommandAckRequestSetter}.
     *
     * @return the instance.
     */
    public static ThingLiveCommandAckRequestSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isApplicable(final ThingCommand<?> command) {
        checkNotNull(command, "command");
        return isLiveChannelCommand(command);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<ThingCommand<?>> getMatchedClass() {
        return (Class) ThingCommand.class;
    }

    @Override
    protected boolean isBindResponseRequiredToAddingRemovingImplicitLabel() {
        return true;
    }
}
