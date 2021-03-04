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
package org.eclipse.ditto.protocoladapter;

import java.util.Optional;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;

/**
 * An {@code Adaptable} can be mapped to and from a {@link org.eclipse.ditto.signals.commands.base.Command}, {@link
 * org.eclipse.ditto.signals.commands.base.CommandResponse} or {@link org.eclipse.ditto.signals.events.base.Event}.
 */
public interface Adaptable extends WithDittoHeaders<Adaptable> {

    /**
     * Returns a mutable builder to create immutable {@code Adaptable} instances for a given {@code topicPath}.
     *
     * @param topicPath the topic path.
     * @return the builder.
     * @throws NullPointerException if {@code topicPath} is {@code null}.
     */
    static AdaptableBuilder newBuilder(final TopicPath topicPath) {
        return ProtocolFactory.newAdaptableBuilder(topicPath);
    }

    /**
     * Returns the {@code TopicPath} of this {@code Adaptable}.
     *
     * @return the topic path.
     */
    TopicPath getTopicPath();

    /**
     * Returns the {@code Payload} of this {@code Adaptable}.
     *
     * @return the payload.
     */
    Payload getPayload();

    /**
     * Returns the {@code DittoHeaders} of this {@code Adaptable} if present.
     *
     * @return the optional headers.
     * @deprecated since 1.3.0, will be removed in a future release. Use {@link #getDittoHeaders()} instead.
     */
    @Deprecated
    Optional<DittoHeaders> getHeaders();

    /**
     * Indicates whether this Adaptable contains a header with the specified key.
     *
     * @param key the key to be looked up.
     * @return {@code true} if this Adaptable contains a header with key {@code key}.
     */
    boolean containsHeaderForKey(CharSequence key);

}
