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
package org.eclipse.ditto.services.thingsearch.persistence.write.streaming;

import static org.eclipse.ditto.services.thingsearch.persistence.PersistenceConstants.THINGS_COLLECTION_NAME;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.eclipse.ditto.services.thingsearch.common.config.PersistenceStreamConfig;
import org.eclipse.ditto.services.thingsearch.persistence.write.model.AbstractWriteModel;
import org.eclipse.ditto.services.thingsearch.persistence.write.model.WriteResultAndErrors;
import org.eclipse.ditto.services.utils.metrics.DittoMetrics;
import org.eclipse.ditto.services.utils.metrics.instruments.timer.StartedTimer;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import akka.NotUsed;
import akka.japi.Pair;
import akka.japi.pf.PFBuilder;
import akka.stream.Attributes;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.Graph;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Zip;

/**
 * Flow mapping write models to write results via the search persistence.
 */
final class MongoSearchUpdaterFlow {

    private static final String TRACE_THING_BULK_UPDATE = "things_search_thing_bulkUpdate";
    private static final String COUNT_THING_BULK_UPDATES_PER_BULK = "things_search_thing_bulkUpdate_updates_per_bulk";
    private static final String UPDATE_TYPE_TAG = "update_type";

    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> collectionWithAcknowledgements;

    private MongoSearchUpdaterFlow(final MongoCollection<Document> collection,
            final PersistenceStreamConfig persistenceConfig) {
        this.collection = collection;
        this.collectionWithAcknowledgements = collection.withWriteConcern(
                persistenceConfig.getWithAcknowledgementsWriteConcern());
    }

    /**
     * Create a MongoSearchUpdaterFlow object.
     *
     * @param database the MongoDB database.
     * @param persistenceConfig the persistence configuration for the search updater stream.
     * @return the MongoSearchUpdaterFlow object.
     */
    public static MongoSearchUpdaterFlow of(final MongoDatabase database,
            final PersistenceStreamConfig persistenceConfig) {
        return new MongoSearchUpdaterFlow(database.getCollection(THINGS_COLLECTION_NAME), persistenceConfig);
    }


    /**
     * Create a new flow through the search persistence.
     * No logging or recovery is attempted.
     *
     * @param shouldAcknowledge defines whether for this source the requested ack
     * {@link org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel#SEARCH_PERSISTED} was required or not.
     * @param parallelism How many write operations may run in parallel for this sink.
     * @param maxBulkSize How many writes to perform in one bulk.
     * @return the sink.
     */
    public Flow<Source<AbstractWriteModel, NotUsed>, WriteResultAndErrors, NotUsed> start(
            final boolean shouldAcknowledge,
            final int parallelism,
            final int maxBulkSize) {

        final Flow<Source<AbstractWriteModel, NotUsed>, List<AbstractWriteModel>, NotUsed> batchFlow =
                Flow.<Source<AbstractWriteModel, NotUsed>>create()
                        .flatMapConcat(source -> source.grouped(maxBulkSize));

        final Flow<List<AbstractWriteModel>, WriteResultAndErrors, NotUsed> writeFlow =
                Flow.<List<AbstractWriteModel>>create()
                        .flatMapMerge(parallelism, writeModels -> executeBulkWrite(shouldAcknowledge, writeModels))
                        // never initiate more than "parallelism" writes against the persistence
                        .withAttributes(Attributes.inputBuffer(parallelism, parallelism));

        return Flow.fromGraph(assembleFlows(batchFlow, writeFlow, createStartTimerFlow(), createStopTimerFlow()));
    }

    private Source<WriteResultAndErrors, NotUsed> executeBulkWrite(final boolean shouldAcknowledge,
            final List<AbstractWriteModel> abstractWriteModels) {
        final List<WriteModel<Document>> writeModels = abstractWriteModels.stream()
                .map(writeModel -> {
                    ConsistencyLag.startS5MongoBulkWrite(writeModel.getMetadata());
                    return writeModel.toMongo();
                })
                .collect(Collectors.toList());

        final MongoCollection<Document> theCollection;
        if (shouldAcknowledge) {
            theCollection = this.collection;
        } else {
            theCollection = this.collectionWithAcknowledgements;
        }

        return Source.fromPublisher(theCollection.bulkWrite(writeModels, new BulkWriteOptions().ordered(false)))
                .map(bulkWriteResult -> WriteResultAndErrors.success(abstractWriteModels, bulkWriteResult))
                .recoverWithRetries(1, new PFBuilder<Throwable, Source<WriteResultAndErrors, NotUsed>>()
                        .match(MongoBulkWriteException.class, bulkWriteException ->
                                Source.single(WriteResultAndErrors.failure(abstractWriteModels, bulkWriteException))
                        )
                        .matchAny(error ->
                                Source.single(WriteResultAndErrors.unexpectedError(abstractWriteModels, error))
                        )
                        .build()
                )
                .map(resultAndErrors -> {
                    abstractWriteModels.forEach(writeModel ->
                            ConsistencyLag.startS6Acknowledge(writeModel.getMetadata()));
                    return resultAndErrors;
                });
    }

    private static <T> Flow<List<T>, StartedTimer, NotUsed> createStartTimerFlow() {
        return Flow.fromFunction(writeModels -> {
            DittoMetrics.histogram(COUNT_THING_BULK_UPDATES_PER_BULK).record((long) writeModels.size());
            return DittoMetrics.timer(TRACE_THING_BULK_UPDATE).tag(UPDATE_TYPE_TAG, "bulkUpdate").start();
        });
    }

    private static <T> Flow<Pair<T, StartedTimer>, T, NotUsed> createStopTimerFlow() {
        return Flow.fromFunction(pair -> {
            try {
                pair.second().stop();
            } catch (final IllegalStateException e) {
                // it is okay if the timer stopped already; simply return the result.
            }
            return pair.first();
        });
    }

    @SuppressWarnings("unchecked") // java 8 can't handle graph DSL types
    private static <A, B, C, D> Graph<FlowShape<A, C>, NotUsed> assembleFlows(
            final Flow<A, B, NotUsed> stage1Flow,
            final Flow<B, C, NotUsed> stage2Flow,
            final Flow<B, D, NotUsed> startTimerFlow,
            final Flow<Pair<C, D>, C, NotUsed> resultProcessorFlow) {

        return GraphDSL.create(builder -> {
            final FlowShape<A, B> stage1 = builder.add(stage1Flow);
            final FlowShape<B, C> stage2 = builder.add(stage2Flow);
            final FlowShape<B, D> sideChannel = builder.add(startTimerFlow);
            final FlowShape<Pair<C, D>, C> resultProcessor = builder.add(resultProcessorFlow);

            final UniformFanOutShape<B, B> broadcast = builder.add(Broadcast.create(2));
            final FanInShape2<C, D, Pair<C, D>> zip = builder.add(Zip.create());

            builder.from(stage1.out()).toInlet(broadcast.in());
            // its important that outlet 0 is connected to the timers, to guarantee that the timer is started first
            builder.from(broadcast.out(0)).toInlet(sideChannel.in());
            builder.from(broadcast.out(1)).toInlet(stage2.in());
            builder.from(stage2.out()).toInlet(zip.in0());
            builder.from(sideChannel.out()).toInlet(zip.in1());
            builder.from(zip.out()).toInlet(resultProcessor.in());

            return FlowShape.of(stage1.in(), resultProcessor.out());
        });
    }
}
