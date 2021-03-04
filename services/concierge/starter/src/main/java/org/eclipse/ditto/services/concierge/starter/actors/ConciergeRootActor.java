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
package org.eclipse.ditto.services.concierge.starter.actors;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import org.eclipse.ditto.services.base.actors.DittoRootActor;
import org.eclipse.ditto.services.concierge.actors.ShardRegions;
import org.eclipse.ditto.services.concierge.actors.cleanup.EventSnapshotCleanupCoordinator;
import org.eclipse.ditto.services.concierge.common.ConciergeConfig;
import org.eclipse.ditto.services.concierge.starter.proxy.EnforcerActorFactory;
import org.eclipse.ditto.services.models.concierge.ConciergeMessagingConstants;
import org.eclipse.ditto.services.models.concierge.actors.ConciergeForwarderActor;
import org.eclipse.ditto.services.utils.cluster.ClusterUtil;
import org.eclipse.ditto.services.utils.cluster.DistPubSubAccess;
import org.eclipse.ditto.services.utils.health.DefaultHealthCheckingActorFactory;
import org.eclipse.ditto.services.utils.health.HealthCheckingActorOptions;
import org.eclipse.ditto.services.utils.health.SingletonStatusReporter;
import org.eclipse.ditto.services.utils.health.config.HealthCheckConfig;
import org.eclipse.ditto.services.utils.health.config.PersistenceConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoHealthChecker;

import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * The root actor of the concierge service.
 */
public final class ConciergeRootActor extends DittoRootActor {

    /**
     * Name of this actor.
     */
    public static final String ACTOR_NAME = "conciergeRoot";

    @SuppressWarnings("unused")
    private <C extends ConciergeConfig> ConciergeRootActor(final C conciergeConfig,
            final ActorRef pubSubMediator,
            final EnforcerActorFactory<C> enforcerActorFactory) {

        pubSubMediator.tell(DistPubSubAccess.put(getSelf()), getSelf());

        final ActorContext context = getContext();
        final ShardRegions shardRegions = ShardRegions.of(getContext().getSystem(), conciergeConfig.getClusterConfig());

        enforcerActorFactory.startEnforcerActor(context, conciergeConfig, pubSubMediator, shardRegions);

        final ActorRef conciergeForwarder = context.findChild(ConciergeForwarderActor.ACTOR_NAME).orElseThrow(() ->
                new IllegalStateException("ConciergeForwarder could not be found"));

        final ActorRef cleanupCoordinator = startClusterSingletonActor(
                EventSnapshotCleanupCoordinator.ACTOR_NAME,
                EventSnapshotCleanupCoordinator.props(conciergeConfig.getPersistenceCleanupConfig(), pubSubMediator,
                        shardRegions));

        final ActorRef healthCheckingActor = startHealthCheckingActor(conciergeConfig, cleanupCoordinator);
        bindHttpStatusRoute(conciergeConfig.getHttpConfig(), healthCheckingActor);
    }

    /**
     * Creates Akka configuration object Props for this actor.
     *
     * @param conciergeConfig the config of Concierge.
     * @param pubSubMediator the PubSub mediator Actor.
     * @param enforcerActorFactory factory for creating sharded enforcer actors.
     * @return the Akka configuration Props object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static <C extends ConciergeConfig> Props props(final C conciergeConfig,
            final ActorRef pubSubMediator,
            final EnforcerActorFactory<C> enforcerActorFactory) {

        checkNotNull(conciergeConfig, "config of Concierge");
        checkNotNull(pubSubMediator, "pub-sub mediator");
        checkNotNull(enforcerActorFactory, "EnforcerActor factory");

        return Props.create(ConciergeRootActor.class, conciergeConfig, pubSubMediator, enforcerActorFactory);
    }


    private ActorRef startClusterSingletonActor(final String actorName, final Props props) {

        return ClusterUtil.startSingleton(getContext(), ConciergeMessagingConstants.CLUSTER_ROLE, actorName, props);
    }

    private ActorRef startHealthCheckingActor(final ConciergeConfig conciergeConfig,
            final ActorRef cleanupCoordinator) {

        final HealthCheckConfig healthCheckConfig = conciergeConfig.getHealthCheckConfig();

        final HealthCheckingActorOptions.Builder hcBuilder =
                HealthCheckingActorOptions.getBuilder(healthCheckConfig.isEnabled(), healthCheckConfig.getInterval());

        final PersistenceConfig persistenceConfig = healthCheckConfig.getPersistenceConfig();
        if (persistenceConfig.isEnabled()) {
            hcBuilder.enablePersistenceCheck();
        }
        final HealthCheckingActorOptions healthCheckingActorOptions = hcBuilder.build();

        final ActorRef cleanupCoordinatorProxy = ClusterUtil.startSingletonProxy(getContext(),
                ConciergeMessagingConstants.CLUSTER_ROLE, cleanupCoordinator);

        return startChildActor(DefaultHealthCheckingActorFactory.ACTOR_NAME,
                DefaultHealthCheckingActorFactory.props(healthCheckingActorOptions,
                        MongoHealthChecker.props(),
                        SingletonStatusReporter.props(ConciergeMessagingConstants.CLUSTER_ROLE,
                                cleanupCoordinatorProxy))
        );
    }

}
