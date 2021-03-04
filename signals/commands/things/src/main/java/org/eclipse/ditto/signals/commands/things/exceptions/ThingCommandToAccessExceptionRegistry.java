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
package org.eclipse.ditto.signals.commands.things.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.signals.base.WithFeatureId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandToExceptionRegistry;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAclEntry;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDesiredProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAcl;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAclEntry;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeature;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDesiredProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingDefinition;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAcl;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAclEntry;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttribute;
import org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeature;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureDesiredProperties;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperties;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperty;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatures;
import org.eclipse.ditto.signals.commands.things.query.RetrievePolicyId;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThing;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThingDefinition;

/**
 * Registry to map thing commands to access exceptions.
 */
public final class ThingCommandToAccessExceptionRegistry extends AbstractCommandToExceptionRegistry<ThingCommand<?>,
        DittoRuntimeException> {

    private static final ThingCommandToAccessExceptionRegistry INSTANCE = createInstance();

    private ThingCommandToAccessExceptionRegistry(
            final Map<String, Function<ThingCommand<?>, DittoRuntimeException>> mappingStrategies) {
        super(mappingStrategies);
    }

    /**
     * Returns an instance of {@code ThingCommandToAccessExceptionRegistry}.
     *
     * @return the instance.
     */
    public static ThingCommandToAccessExceptionRegistry getInstance() {
        return INSTANCE;
    }

    private static ThingCommandToAccessExceptionRegistry createInstance() {
        final Map<String, Function<ThingCommand<?>, DittoRuntimeException>> mappingStrategies = new HashMap<>();

        // modify
        mappingStrategies.put(CreateThing.TYPE, command -> ThingConflictException.newBuilder(command.getThingEntityId())
                .dittoHeaders(command.getDittoHeaders())
                .build());
        mappingStrategies.put(ModifyThing.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);
        mappingStrategies.put(DeleteThing.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);

        mappingStrategies.put(ModifyAcl.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);
        mappingStrategies.put(ModifyAclEntry.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);
        mappingStrategies.put(DeleteAclEntry.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);

        mappingStrategies.put(ModifyPolicyId.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);

        mappingStrategies.put(ModifyThingDefinition.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToDefinitionException);
        mappingStrategies.put(DeleteThingDefinition.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToDefinitionException);

        mappingStrategies.put(ModifyAttributes.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToAttributesException);
        mappingStrategies.put(DeleteAttributes.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToAttributesException);
        mappingStrategies.put(ModifyAttribute.TYPE, ThingCommandToAccessExceptionRegistry::commandToAttributeException);
        mappingStrategies.put(DeleteAttribute.TYPE, ThingCommandToAccessExceptionRegistry::commandToAttributeException);

        mappingStrategies.put(ModifyFeatures.TYPE, ThingCommandToAccessExceptionRegistry::commandToFeaturesException);
        mappingStrategies.put(DeleteFeatures.TYPE, ThingCommandToAccessExceptionRegistry::commandToFeaturesException);
        mappingStrategies.put(ModifyFeature.TYPE, ThingCommandToAccessExceptionRegistry::commandToFeatureException);
        mappingStrategies.put(DeleteFeature.TYPE, ThingCommandToAccessExceptionRegistry::commandToFeatureException);

        mappingStrategies.put(ModifyFeatureProperties.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeaturePropertiesException);
        mappingStrategies.put(DeleteFeatureProperties.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeaturePropertiesException);
        mappingStrategies.put(ModifyFeatureProperty.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeaturePropertyException);
        mappingStrategies.put(DeleteFeatureProperty.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeaturePropertyException);
        mappingStrategies.put(ModifyFeatureDesiredProperties.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDesiredPropertiesException);
        mappingStrategies.put(DeleteFeatureDesiredProperties.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDesiredPropertiesException);
        mappingStrategies.put(ModifyFeatureDesiredProperty.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDesiredPropertyException);
        mappingStrategies.put(DeleteFeatureDesiredProperty.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDesiredPropertyException);

        // query
        mappingStrategies.put(RetrieveThing.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);

        mappingStrategies.put(RetrievePolicyId.TYPE,
                command -> PolicyIdNotAccessibleException.newBuilder(command.getThingEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());

        mappingStrategies.put(RetrieveAcl.TYPE, ThingCommandToAccessExceptionRegistry::commandToThingException);
        mappingStrategies.put(RetrieveAclEntry.TYPE,
                command -> AclNotAccessibleException.newBuilder(command.getThingEntityId(),
                        ((RetrieveAclEntry) command).getAuthorizationSubject())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(RetrieveThingDefinition.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToDefinitionException);
        mappingStrategies.put(RetrieveAttribute.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToAttributeException);
        mappingStrategies.put(RetrieveAttributes.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToAttributesException);
        mappingStrategies.put(RetrieveFeature.TYPE, ThingCommandToAccessExceptionRegistry::commandToFeatureException);
        mappingStrategies.put(RetrieveFeatures.TYPE, ThingCommandToAccessExceptionRegistry::commandToFeaturesException);
        mappingStrategies.put(RetrieveFeatureDefinition.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDefinitionException);
        mappingStrategies.put(RetrieveFeatureProperties.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeaturePropertiesException);
        mappingStrategies.put(RetrieveFeatureProperty.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeaturePropertyException);
        mappingStrategies.put(RetrieveFeatureDesiredProperties.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDesiredPropertiesException);
        mappingStrategies.put(RetrieveFeatureDesiredProperty.TYPE,
                ThingCommandToAccessExceptionRegistry::commandToFeatureDesiredPropertyException);

        return new ThingCommandToAccessExceptionRegistry(mappingStrategies);
    }

    private static ThingNotAccessibleException commandToThingException(final ThingCommand<?> command) {
        return ThingNotAccessibleException.newBuilder(command.getThingEntityId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static ThingDefinitionNotAccessibleException commandToDefinitionException(final ThingCommand<?> command) {
        return ThingDefinitionNotAccessibleException.newBuilder(command.getThingEntityId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static AttributesNotAccessibleException commandToAttributesException(final ThingCommand<?> command) {
        return AttributesNotAccessibleException.newBuilder(command.getThingEntityId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static AttributeNotAccessibleException commandToAttributeException(final ThingCommand<?> command) {
        return AttributeNotAccessibleException.newBuilder(command.getThingEntityId(), command.getResourcePath())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeaturesNotAccessibleException commandToFeaturesException(final ThingCommand<?> command) {
        return FeaturesNotAccessibleException.newBuilder(command.getThingEntityId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeatureNotAccessibleException commandToFeatureException(final ThingCommand<?> command) {
        return FeatureNotAccessibleException.newBuilder(command.getThingEntityId(),
                ((WithFeatureId) command).getFeatureId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeatureDefinitionNotAccessibleException commandToFeatureDefinitionException(
            final ThingCommand<?> command) {
        return FeatureDefinitionNotAccessibleException.newBuilder(command.getThingEntityId(),
                ((WithFeatureId) command).getFeatureId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeaturePropertiesNotAccessibleException commandToFeaturePropertiesException(
            final ThingCommand<?> command) {
        return FeaturePropertiesNotAccessibleException.newBuilder(command.getThingEntityId(),
                ((WithFeatureId) command).getFeatureId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeaturePropertyNotAccessibleException commandToFeaturePropertyException(
            final ThingCommand<?> command) {
        return FeaturePropertyNotAccessibleException.newBuilder(command.getThingEntityId(),
                ((WithFeatureId) command).getFeatureId(), command.getResourcePath())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeatureDesiredPropertiesNotAccessibleException commandToFeatureDesiredPropertiesException(
            final ThingCommand<?> command) {
        return FeatureDesiredPropertiesNotAccessibleException.newBuilder(command.getThingEntityId(),
                ((WithFeatureId) command).getFeatureId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

    private static FeatureDesiredPropertyNotAccessibleException commandToFeatureDesiredPropertyException(
            final ThingCommand<?> command) {
        return FeatureDesiredPropertyNotAccessibleException.newBuilder(command.getThingEntityId(),
                ((WithFeatureId) command).getFeatureId(), command.getResourcePath())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

}
