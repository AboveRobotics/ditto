/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.utils.persistence.mongo.namespace;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoClientWrapper;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import akka.NotUsed;
import akka.japi.pf.Match;
import akka.stream.javadsl.Source;

/**
 * MongoDB operations on the level of namespaces.
 */
public final class MongoNamespaceOps implements NamespaceOps<MongoNamespaceSelection>, Closeable {

    private final MongoClientWrapper mongoDbClientWrapper;
    private final MongoDatabase db;

    private MongoNamespaceOps(final MongoDatabase db) {
        mongoDbClientWrapper = null;
        this.db = db;
    }

    private MongoNamespaceOps(final MongoClientWrapper theMongoDbClientWrapper) {
        mongoDbClientWrapper = theMongoDbClientWrapper;
        db = mongoDbClientWrapper.getDatabase();
    }

    /**
     * Create a new NamespaceOps object.
     *
     * @param db the database to operate on.
     * @return a new NamespaceOps object.
     */
    public static MongoNamespaceOps of(final MongoDatabase db) {
        return new MongoNamespaceOps(db);
    }

    /**
     * Returns an instance of {@code MongoNamespaceOps}.
     *
     * @param mongoDbClientWrapper provides the database to be used for operations. Will be closed with a call to #close.
     * @return the instance.
     * @throws NullPointerException if {@code mongoDbClientWrapper} is {@code null}.
     */
    public static MongoNamespaceOps of(final MongoClientWrapper mongoDbClientWrapper) {
        return new MongoNamespaceOps(mongoDbClientWrapper);
    }

    @Override
    public Source<Optional<Throwable>, NotUsed> purge(final MongoNamespaceSelection selection) {
        final MongoCollection<Document> collection = db.getCollection(selection.getCollectionName());
        if (selection.isEntireCollection()) {
            return Source.fromPublisher(collection.drop())
                    .map(success -> Optional.empty());
        } else {
            // https://stackoverflow.com/a/33164008
            // claims unordered bulk ops halve MongoDB load
            final List<WriteModel<Document>> writeModel =
                    Collections.singletonList(new DeleteManyModel<>(selection.getFilter()));
            final BulkWriteOptions options = new BulkWriteOptions().ordered(false);
            return Source.fromPublisher(collection.bulkWrite(writeModel, options))
                    .map(result -> Optional.<Throwable>empty())
                    .recover(Match.<Throwable, Optional<Throwable>>matchAny(Optional::of).build());
        }
    }

    @Override
    public void close() {
        mongoDbClientWrapper.close();
    }

}
