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
package org.eclipse.ditto.services.connectivity.messaging.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.eclipse.ditto.model.base.entity.id.EntityId;

/**
 * Creates Kafka sinks.
 */
interface KafkaConnectionFactory {

    /**
     * Identifier of the connection.
     *
     * @return the ID.
     */
    EntityId connectionId();

    /**
     * Create a producer of Kafka messages.
     *
     * @return the producer.
     */
    Producer<String, String> newProducer();
}
