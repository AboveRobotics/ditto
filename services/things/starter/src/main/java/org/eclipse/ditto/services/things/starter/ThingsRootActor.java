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
package org.eclipse.ditto.services.things.starter;

import static akka.http.javadsl.server.Directives.logRequest;
import static akka.http.javadsl.server.Directives.logResult;
import static org.eclipse.ditto.services.models.things.ThingsMessagingConstants.CLUSTER_ROLE;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.services.base.actors.DittoRootActor;
import org.eclipse.ditto.services.base.config.http.HttpConfig;
import org.eclipse.ditto.services.models.things.ThingEventPubSubFactory;
import org.eclipse.ditto.services.models.things.ThingsMessagingConstants;
import org.eclipse.ditto.services.things.common.config.ThingsConfig;
import org.eclipse.ditto.services.things.persistence.actors.ThingPersistenceActorPropsFactory;
import org.eclipse.ditto.services.things.persistence.actors.ThingPersistenceOperationsActor;
import org.eclipse.ditto.services.things.persistence.actors.ThingSupervisorActor;
import org.eclipse.ditto.services.things.persistence.actors.ThingsPersistenceStreamingActorCreator;
import org.eclipse.ditto.services.utils.akka.LogUtil;
import org.eclipse.ditto.services.utils.cluster.ClusterStatusSupplier;
import org.eclipse.ditto.services.utils.cluster.DistPubSubAccess;
import org.eclipse.ditto.services.utils.cluster.RetrieveStatisticsDetailsResponseSupplier;
import org.eclipse.ditto.services.utils.cluster.ShardRegionExtractor;
import org.eclipse.ditto.services.utils.cluster.config.ClusterConfig;
import org.eclipse.ditto.services.utils.config.LocalHostAddressSupplier;
import org.eclipse.ditto.services.utils.health.DefaultHealthCheckingActorFactory;
import org.eclipse.ditto.services.utils.health.HealthCheckingActorOptions;
import org.eclipse.ditto.services.utils.health.config.HealthCheckConfig;
import org.eclipse.ditto.services.utils.health.config.MetricsReporterConfig;
import org.eclipse.ditto.services.utils.health.routes.StatusRoute;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoHealthChecker;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoMetricsReporter;
import org.eclipse.ditto.services.utils.persistence.mongo.config.TagsConfig;
import org.eclipse.ditto.services.utils.pubsub.DistributedPub;
import org.eclipse.ditto.signals.commands.devops.RetrieveStatisticsDetails;
import org.eclipse.ditto.signals.events.things.ThingEvent;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.event.DiagnosticLoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;

/**
 * Our "Parent" Actor which takes care of supervision of all other Actors in our system.
 */
public final class ThingsRootActor extends DittoRootActor {

    /**
     * The name of this Actor in the ActorSystem.
     */
    public static final String ACTOR_NAME = "thingsRoot";

    private final DiagnosticLoggingAdapter log = LogUtil.obtain(this);

    private final RetrieveStatisticsDetailsResponseSupplier retrieveStatisticsDetailsResponseSupplier;

    @SuppressWarnings("unused")
    private ThingsRootActor(final ThingsConfig thingsConfig,
            final ActorRef pubSubMediator,
            final ActorMaterializer materializer,
            final ThingPersistenceActorPropsFactory propsFactory) {

        final ActorSystem actorSystem = getContext().system();

        final ClusterConfig clusterConfig = thingsConfig.getClusterConfig();
        final ShardRegionExtractor shardRegionExtractor =
                ShardRegionExtractor.of(clusterConfig.getNumberOfShards(), actorSystem);
        final ThingEventPubSubFactory pubSubFactory = ThingEventPubSubFactory.of(getContext(), shardRegionExtractor);
        final DistributedPub<ThingEvent> distributedPub = pubSubFactory.startDistributedPub();

        final ActorRef thingsShardRegion = ClusterSharding.get(actorSystem)
                .start(ThingsMessagingConstants.SHARD_REGION,
                        getThingSupervisorActorProps(pubSubMediator, distributedPub, propsFactory),
                        ClusterShardingSettings.create(actorSystem).withRole(CLUSTER_ROLE),
                        shardRegionExtractor);

        startChildActor(ThingPersistenceOperationsActor.ACTOR_NAME,
                ThingPersistenceOperationsActor.props(pubSubMediator, thingsConfig.getMongoDbConfig(),
                        actorSystem.settings().config(), thingsConfig.getPersistenceOperationsConfig()));

        retrieveStatisticsDetailsResponseSupplier = RetrieveStatisticsDetailsResponseSupplier.of(thingsShardRegion,
                ThingsMessagingConstants.SHARD_REGION, log);

        final HealthCheckConfig healthCheckConfig = thingsConfig.getHealthCheckConfig();
        final HealthCheckingActorOptions.Builder hcBuilder =
                HealthCheckingActorOptions.getBuilder(healthCheckConfig.isEnabled(), healthCheckConfig.getInterval());
        if (healthCheckConfig.getPersistenceConfig().isEnabled()) {
            hcBuilder.enablePersistenceCheck();
        }

        final HealthCheckingActorOptions healthCheckingActorOptions = hcBuilder.build();
        final MetricsReporterConfig metricsReporterConfig =
                healthCheckConfig.getPersistenceConfig().getMetricsReporterConfig();
        final ActorRef healthCheckingActor = startChildActor(DefaultHealthCheckingActorFactory.ACTOR_NAME,
                DefaultHealthCheckingActorFactory.props(healthCheckingActorOptions,
                        MongoHealthChecker.props(),
                        MongoMetricsReporter.props(
                                metricsReporterConfig.getResolution(),
                                metricsReporterConfig.getHistory(),
                                pubSubMediator
                        )
                ));

        final TagsConfig tagsConfig = thingsConfig.getTagsConfig();
        final ActorRef eventStreamingActor =
                ThingsPersistenceStreamingActorCreator.startEventStreamingActor(tagsConfig.getStreamingCacheSize(),
                        this::startChildActor);
        final ActorRef snapshotStreamingActor =
                ThingsPersistenceStreamingActorCreator.startSnapshotStreamingActor(this::startChildActor);

        pubSubMediator.tell(DistPubSubAccess.put(getSelf()), getSelf());
        pubSubMediator.tell(DistPubSubAccess.put(eventStreamingActor), getSelf());
        pubSubMediator.tell(DistPubSubAccess.put(snapshotStreamingActor), getSelf());

        final HttpConfig httpConfig = thingsConfig.getHttpConfig();
        String hostname = httpConfig.getHostname();
        if (hostname.isEmpty()) {
            hostname = LocalHostAddressSupplier.getInstance().get();
            log.info("No explicit hostname configured, using HTTP hostname <{}>.", hostname);
        }
        final CompletionStage<ServerBinding> binding = Http.get(actorSystem)
                .bindAndHandle(
                        createRoute(actorSystem, healthCheckingActor).flow(actorSystem,
                                materializer),
                        ConnectHttp.toHost(hostname, httpConfig.getPort()), materializer);

        binding.thenAccept(theBinding -> CoordinatedShutdown.get(getContext().getSystem()).addTask(
                CoordinatedShutdown.PhaseServiceUnbind(), "shutdown_health_http_endpoint", () -> {
                    log.info("Gracefully shutting down status/health HTTP endpoint ...");
                    return theBinding.terminate(Duration.ofSeconds(1))
                            .handle((httpTerminated, e) -> Done.getInstance());
                })
        );
        binding.thenAccept(this::logServerBinding)
                .exceptionally(failure -> {
                    log.error(failure, "Something very bad happened: {}", failure.getMessage());
                    actorSystem.terminate();
                    return null;
                });
    }

    /**
     * Creates Akka configuration object Props for this ThingsRootActor.
     *
     * @param thingsConfig the configuration settings of the Things service.
     * @param pubSubMediator the PubSub mediator Actor.
     * @param materializer the materializer for the Akka actor system.
     * @param propsFactory factory of Props of thing-persistence-actor.
     * @return the Akka configuration Props object.
     */
    public static Props props(final ThingsConfig thingsConfig,
            final ActorRef pubSubMediator,
            final ActorMaterializer materializer,
            final ThingPersistenceActorPropsFactory propsFactory) {

        // Beware: ThingPersistenceActorPropsFactory is not serializable.
        return Props.create(ThingsRootActor.class, thingsConfig, pubSubMediator, materializer, propsFactory);
    }

    private static Route createRoute(final ActorSystem actorSystem, final ActorRef healthCheckingActor) {
        final StatusRoute statusRoute = new StatusRoute(new ClusterStatusSupplier(Cluster.get(actorSystem)),
                healthCheckingActor, actorSystem);

        return logRequest("http-request", () -> logResult("http-response", statusRoute::buildStatusRoute));
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(RetrieveStatisticsDetails.class, this::handleRetrieveStatisticsDetails)
                .build().orElse(super.createReceive());
    }

    private void handleRetrieveStatisticsDetails(final RetrieveStatisticsDetails command) {
        log.info("Sending the namespace stats of the things shard as requested ...");
        Patterns.pipe(retrieveStatisticsDetailsResponseSupplier
                .apply(command.getDittoHeaders()), getContext().dispatcher()).to(getSender());
    }

    private void logServerBinding(final ServerBinding serverBinding) {
        log.info("Bound to address {}:{}", serverBinding.localAddress().getHostString(),
                serverBinding.localAddress().getPort());
    }

    private static Props getThingSupervisorActorProps(
            final ActorRef pubSubMediator,
            final DistributedPub<ThingEvent> distributedPub,
            final ThingPersistenceActorPropsFactory propsFactory) {

        return ThingSupervisorActor.props(pubSubMediator, distributedPub, propsFactory);
    }

}
