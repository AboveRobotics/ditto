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
package org.eclipse.ditto.services.things.persistence.actors.strategies.events;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.services.utils.persistentactors.events.AbstractEventStrategies;
import org.eclipse.ditto.signals.events.things.AclEntryCreated;
import org.eclipse.ditto.signals.events.things.AclEntryDeleted;
import org.eclipse.ditto.signals.events.things.AclEntryModified;
import org.eclipse.ditto.signals.events.things.AclModified;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeDeleted;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.eclipse.ditto.signals.events.things.AttributesCreated;
import org.eclipse.ditto.signals.events.things.AttributesDeleted;
import org.eclipse.ditto.signals.events.things.AttributesModified;
import org.eclipse.ditto.signals.events.things.FeatureCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionModified;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDesiredPropertiesCreated;
import org.eclipse.ditto.signals.events.things.FeatureDesiredPropertiesDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDesiredPropertiesModified;
import org.eclipse.ditto.signals.events.things.FeatureDesiredPropertyCreated;
import org.eclipse.ditto.signals.events.things.FeatureDesiredPropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDesiredPropertyModified;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertyCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.FeaturesCreated;
import org.eclipse.ditto.signals.events.things.FeaturesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturesModified;
import org.eclipse.ditto.signals.events.things.PolicyIdCreated;
import org.eclipse.ditto.signals.events.things.PolicyIdModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDefinitionCreated;
import org.eclipse.ditto.signals.events.things.ThingDefinitionDeleted;
import org.eclipse.ditto.signals.events.things.ThingDefinitionModified;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.eclipse.ditto.signals.events.things.ThingMerged;
import org.eclipse.ditto.signals.events.things.ThingModified;

/**
 * This Singleton strategy handles all {@link org.eclipse.ditto.signals.events.things.ThingEvent}s.
 */
@Immutable
public final class ThingEventStrategies extends AbstractEventStrategies<ThingEvent<?>, Thing> {

    private static final ThingEventStrategies INSTANCE = new ThingEventStrategies();

    /**
     * Returns the <em>singleton</em> {@code EventHandleStrategy} instance.
     *
     * @return the instance.
     */
    public static ThingEventStrategies getInstance() {
        return INSTANCE;
    }

    /**
     * Constructs a new {@code ThingEventHandleStrategy}.
     */
    private ThingEventStrategies() {
        addThingStrategies();
        addAclStrategies();
        addAttributesStrategies();
        addDefinitionStrategies();
        addFeaturesStrategies();
        addPolicyIdStrategies();
    }

    private void addThingStrategies() {
        addStrategy(ThingCreated.class, new ThingCreatedStrategy());
        addStrategy(ThingModified.class, new ThingModifiedStrategy());
        addStrategy(ThingDeleted.class, new ThingDeletedStrategy());
        addStrategy(ThingMerged.class, new ThingMergedStrategy());
    }

    private void addAclStrategies() {
        addStrategy(AclModified.class, new AclModifiedStrategy());
        addStrategy(AclEntryCreated.class, new AclEntryCreatedStrategy());
        addStrategy(AclEntryModified.class, new AclEntryModifiedStrategy());
        addStrategy(AclEntryDeleted.class, new AclEntryDeletedStrategy());
    }

    private void addAttributesStrategies() {
        addStrategy(AttributesCreated.class, new AttributesCreatedStrategy());
        addStrategy(AttributesModified.class, new AttributesModifiedStrategy());
        addStrategy(AttributesDeleted.class, new AttributesDeletedStrategy());

        addStrategy(AttributeCreated.class, new AttributeCreatedStrategy());
        addStrategy(AttributeModified.class, new AttributeModifiedStrategy());
        addStrategy(AttributeDeleted.class, new AttributeDeletedStrategy());
    }

    private void addDefinitionStrategies() {
        addStrategy(ThingDefinitionCreated.class, new ThingDefinitionCreatedStrategy());
        addStrategy(ThingDefinitionModified.class, new ThingDefinitionModifiedStrategy());
        addStrategy(ThingDefinitionDeleted.class, new ThingDefinitionDeletedStrategy());
    }

    private void addFeaturesStrategies() {
        addStrategy(FeaturesCreated.class, new FeaturesCreatedStrategy());
        addStrategy(FeaturesModified.class, new FeaturesModifiedStrategy());
        addStrategy(FeaturesDeleted.class, new FeaturesDeletedStrategy());

        addStrategy(FeatureCreated.class, new FeatureCreatedStrategy());
        addStrategy(FeatureModified.class, new FeatureModifiedStrategy());
        addStrategy(FeatureDeleted.class, new FeatureDeletedStrategy());

        addStrategy(FeatureDefinitionCreated.class, new FeatureDefinitionCreatedStrategy());
        addStrategy(FeatureDefinitionModified.class, new FeatureDefinitionModifiedStrategy());
        addStrategy(FeatureDefinitionDeleted.class, new FeatureDefinitionDeletedStrategy());

        addStrategy(FeaturePropertiesCreated.class, new FeaturePropertiesCreatedStrategy());
        addStrategy(FeaturePropertiesModified.class, new FeaturePropertiesModifiedStrategy());
        addStrategy(FeaturePropertiesDeleted.class, new FeaturePropertiesDeletedStrategy());

        addStrategy(FeaturePropertyCreated.class, new FeaturePropertyCreatedStrategy());
        addStrategy(FeaturePropertyModified.class, new FeaturePropertyModifiedStrategy());
        addStrategy(FeaturePropertyDeleted.class, new FeaturePropertyDeletedStrategy());

        addStrategy(FeatureDesiredPropertiesCreated.class, new FeatureDesiredPropertiesCreatedStrategy());
        addStrategy(FeatureDesiredPropertiesModified.class, new FeatureDesiredPropertiesModifiedStrategy());
        addStrategy(FeatureDesiredPropertiesDeleted.class, new FeatureDesiredPropertiesDeletedStrategy());

        addStrategy(FeatureDesiredPropertyCreated.class, new FeatureDesiredPropertyCreatedStrategy());
        addStrategy(FeatureDesiredPropertyModified.class, new FeatureDesiredPropertyModifiedStrategy());
        addStrategy(FeatureDesiredPropertyDeleted.class, new FeatureDesiredPropertyDeletedStrategy());
    }

    private void addPolicyIdStrategies() {
        addStrategy(PolicyIdCreated.class, new PolicyIdCreatedStrategy());
        addStrategy(PolicyIdModified.class, new PolicyIdModifiedStrategy());
    }
}
