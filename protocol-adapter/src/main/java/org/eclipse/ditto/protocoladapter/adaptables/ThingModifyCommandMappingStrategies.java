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
package org.eclipse.ditto.protocoladapter.adaptables;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.JsonifiableMapper;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingIdNotExplicitlySettableException;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAclEntry;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition;
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
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDesiredProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommand;

/**
 * Defines mapping strategies (map from signal type to JsonifiableMapper) for thing modify commands.
 */
final class ThingModifyCommandMappingStrategies extends AbstractThingMappingStrategies<ThingModifyCommand<?>> {

    private static final ThingModifyCommandMappingStrategies INSTANCE = new ThingModifyCommandMappingStrategies();

    private ThingModifyCommandMappingStrategies() {
        super(initMappingStrategies());
    }

    private static Map<String, JsonifiableMapper<ThingModifyCommand<?>>> initMappingStrategies() {
        final Map<String, JsonifiableMapper<ThingModifyCommand<?>>> mappingStrategies = new HashMap<>();
        addTopLevelMappingStrategies(mappingStrategies);
        addAclMappingStrategies(mappingStrategies);
        addAttributeMappingStrategies(mappingStrategies);
        addDefinitionMappingStrategies(mappingStrategies);
        addFeatureMappingStrategies(mappingStrategies);
        return mappingStrategies;
    }

    private static void addTopLevelMappingStrategies(
            final Map<String, JsonifiableMapper<ThingModifyCommand<?>>> mappingStrategies) {
        mappingStrategies.put(CreateThing.TYPE, ThingModifyCommandMappingStrategies::createThingFrom);
        mappingStrategies.put(ModifyThing.TYPE, ThingModifyCommandMappingStrategies::modifyThingFrom);
        mappingStrategies.put(DeleteThing.TYPE,
                adaptable -> DeleteThing.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(ModifyPolicyId.TYPE, ThingModifyCommandMappingStrategies::modifyPolicyIdFrom);
    }

    private static void addDefinitionMappingStrategies(
            final Map<String, JsonifiableMapper<ThingModifyCommand<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyThingDefinition.TYPE, adaptable -> ModifyThingDefinition.of(thingIdFrom(adaptable),
                thingDefinitionFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteThingDefinition.TYPE,
                adaptable -> DeleteThingDefinition.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
    }

    private static void addAclMappingStrategies(
            final Map<String, JsonifiableMapper<ThingModifyCommand<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyAcl.TYPE,
                adaptable -> ModifyAcl.of(thingIdFrom(adaptable), aclFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyAclEntry.TYPE, adaptable -> ModifyAclEntry.of(thingIdFrom(adaptable),
                aclEntryFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteAclEntry.TYPE, adaptable -> DeleteAclEntry.of(thingIdFrom(adaptable),
                authorizationSubjectFrom(adaptable), dittoHeadersFrom(adaptable)));
    }

    private static void addAttributeMappingStrategies(
            final Map<String, JsonifiableMapper<ThingModifyCommand<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyAttributes.TYPE, adaptable -> ModifyAttributes.of(thingIdFrom(adaptable),
                attributesFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteAttributes.TYPE,
                adaptable -> DeleteAttributes.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyAttribute.TYPE, adaptable -> ModifyAttribute.of(thingIdFrom(adaptable),
                attributePointerFrom(adaptable), attributeValueFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteAttribute.TYPE, adaptable -> DeleteAttribute.of(thingIdFrom(adaptable),
                attributePointerFrom(adaptable), dittoHeadersFrom(adaptable)));
    }

    private static void addFeatureMappingStrategies(
            final Map<String, JsonifiableMapper<ThingModifyCommand<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyFeatures.TYPE, adaptable -> ModifyFeatures.of(thingIdFrom(adaptable),
                featuresFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatures.TYPE,
                adaptable -> DeleteFeatures.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeature.TYPE,
                adaptable -> ModifyFeature.of(thingIdFrom(adaptable), featureFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeature.TYPE, adaptable -> DeleteFeature.of(thingIdFrom(adaptable),
                featureIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureDefinition.TYPE,
                adaptable -> ModifyFeatureDefinition.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featureDefinitionFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureDefinition.TYPE, adaptable -> DeleteFeatureDefinition
                .of(thingIdFrom(adaptable), featureIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureProperties.TYPE,
                adaptable -> ModifyFeatureProperties.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featurePropertiesFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureProperties.TYPE, adaptable -> DeleteFeatureProperties
                .of(thingIdFrom(adaptable), featureIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureDesiredProperties.TYPE,
                adaptable -> ModifyFeatureDesiredProperties.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featurePropertiesFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureDesiredProperties.TYPE,
                adaptable -> DeleteFeatureDesiredProperties.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureProperty.TYPE,
                        adaptable -> ModifyFeatureProperty.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                                featurePropertyPointerFrom(adaptable), featurePropertyValueFrom(adaptable),
                                dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureProperty.TYPE, adaptable -> DeleteFeatureProperty.of(thingIdFrom(adaptable),
                featureIdFrom(adaptable), featurePropertyPointerFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureDesiredProperty.TYPE,
                        adaptable -> ModifyFeatureDesiredProperty.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                                featurePropertyPointerFrom(adaptable), featurePropertyValueFrom(adaptable),
                                dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureDesiredProperty.TYPE,
                adaptable -> DeleteFeatureDesiredProperty.of(thingIdFrom(adaptable),
                        featureIdFrom(adaptable), featurePropertyPointerFrom(adaptable), dittoHeadersFrom(adaptable)));
    }

    static ThingModifyCommandMappingStrategies getInstance() {
        return INSTANCE;
    }

    private static CreateThing createThingFrom(final Adaptable adaptable) {
        return CreateThing.of(thingToCreateOrModifyFrom(adaptable), initialPolicyForCreateThingFrom(adaptable),
                policyIdOrPlaceholderForCreateThingFrom(adaptable), dittoHeadersFrom(adaptable));
    }

    private static ModifyThing modifyThingFrom(final Adaptable adaptable) {
        final Thing thing = thingToCreateOrModifyFrom(adaptable);
        final ThingId thingId = thing.getEntityId().orElseThrow(
                () -> new IllegalStateException("ID should have been enforced in thingToCreateOrModifyFrom"));
        return ModifyThing.of(thingId, thing, initialPolicyForModifyThingFrom(adaptable),
                policyIdOrPlaceholderForModifyThingFrom(adaptable), dittoHeadersFrom(adaptable));
    }

    private static Thing thingToCreateOrModifyFrom(final Adaptable adaptable) {
        final Thing thing = thingFrom(adaptable);

        final Optional<ThingId> thingIdOptional = thing.getEntityId();
        final ThingId thingIdFromTopic = thingIdFrom(adaptable);

        if (thingIdOptional.isPresent()) {
            if (!thingIdOptional.get().equals(thingIdFromTopic)) {
                throw ThingIdNotExplicitlySettableException.forDittoProtocol().build();
            }
        } else {
            return thing.toBuilder()
                    .setId(thingIdFromTopic)
                    .build();
        }
        return thing;
    }

    private static ModifyPolicyId modifyPolicyIdFrom(final Adaptable adaptable) {
        final ThingId thingId = thingIdFrom(adaptable);
        final PolicyId policyId = policyIdFrom(adaptable);

        return ModifyPolicyId.of(thingId, policyId, dittoHeadersFrom(adaptable));
    }

    @Nullable
    private static JsonObject initialPolicyForCreateThingFrom(final Adaptable adaptable) {
        return adaptable.getPayload()
                .getValue()
                .map(JsonValue::asObject)
                .flatMap(o -> o.getValue(CreateThing.JSON_INLINE_POLICY).map(JsonValue::asObject))
                .orElse(null);
    }

    @Nullable
    private static String policyIdOrPlaceholderForCreateThingFrom(final Adaptable adaptable) {
        return adaptable.getPayload().getValue()
                .map(JsonValue::asObject)
                .flatMap(o -> o.getValue(CreateThing.JSON_COPY_POLICY_FROM))
                .orElse(null);
    }

    @Nullable
    private static JsonObject initialPolicyForModifyThingFrom(final Adaptable adaptable) {
        return adaptable.getPayload()
                .getValue()
                .map(JsonValue::asObject)
                .flatMap(o -> o.getValue(ModifyThing.JSON_INLINE_POLICY).map(JsonValue::asObject))
                .orElse(null);
    }

    @Nullable
    private static String policyIdOrPlaceholderForModifyThingFrom(final Adaptable adaptable) {
        return adaptable.getPayload().getValue()
                .map(JsonValue::asObject)
                .flatMap(o -> o.getValue(ModifyThing.JSON_COPY_POLICY_FROM))
                .orElse(null);
    }
}
