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
package org.eclipse.ditto.protocoladapter.signals;

import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;


/**
 * Base class of {@link SignalMapper} for query commands.
 *
 * @param <T> the type supported by this {@link SignalMapper}
 */
abstract class AbstractQuerySignalMapper<T extends Signal<?>> extends AbstractCommandSignalMapper<T> {

    private static final TopicPath.Action[] SUPPORTED_ACTIONS = {TopicPath.Action.RETRIEVE};

    @Override
    TopicPath.Action[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }
}
