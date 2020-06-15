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

import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.JsonifiableMapper;
import org.eclipse.ditto.signals.commands.things.modify.CreateThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAclEntryResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributeResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributesResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturePropertiesResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturesResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAclEntryResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAclResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributeResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertiesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyIdResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommandResponse;

/**
 * Defines mapping strategies (map from signal type to JsonifiableMapper) for thing modify command responses.
 */
final class ThingModifyCommandResponseMappingStrategies
        extends AbstractThingMappingStrategies<ThingModifyCommandResponse<?>> {

    private static final ThingModifyCommandResponseMappingStrategies INSTANCE =
            new ThingModifyCommandResponseMappingStrategies();

    private ThingModifyCommandResponseMappingStrategies() {
        super(initMappingStrategies());
    }

    static ThingModifyCommandResponseMappingStrategies getInstance() {
        return INSTANCE;
    }

    private static Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> initMappingStrategies() {
        final Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> mappingStrategies = new HashMap<>();

        addTopLevelResponses(mappingStrategies);
        addAclResponses(mappingStrategies);
        addAttributeResponses(mappingStrategies);
        addDefinitionResponses(mappingStrategies);
        addFeatureResponses(mappingStrategies);

        return mappingStrategies;
    }

    private static void addTopLevelResponses(
            final Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> mappingStrategies) {
        mappingStrategies.put(CreateThingResponse.TYPE,
                adaptable -> CreateThingResponse.of(thingFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(ModifyThingResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyThingResponse.created(thingFrom(adaptable), dittoHeadersFrom(adaptable))
                        : ModifyThingResponse.modified(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteThingResponse.TYPE,
                adaptable -> DeleteThingResponse.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(ModifyPolicyIdResponse.TYPE,
                ThingModifyCommandResponseMappingStrategies::modifyPolicyIdResponseFrom);
    }

    private static ModifyPolicyIdResponse modifyPolicyIdResponseFrom(final Adaptable adaptable) {
        final ThingId thingId = thingIdFrom(adaptable);
        return isCreated(adaptable) ?
                ModifyPolicyIdResponse.created(thingId, policyIdFrom(adaptable), dittoHeadersFrom(adaptable)) :
                ModifyPolicyIdResponse.modified(thingId, dittoHeadersFrom(adaptable));
    }

    private static void addAclResponses(
            final Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyAclResponse.TYPE,
                adaptable -> ModifyAclResponse.modified(thingIdFrom(adaptable), aclFrom(adaptable),
                        dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyAclEntryResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyAclEntryResponse.created(thingIdFrom(adaptable), aclEntryFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyAclEntryResponse.modified(thingIdFrom(adaptable), aclEntryFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteAclEntryResponse.TYPE,
                adaptable -> DeleteAclEntryResponse.of(thingIdFrom(adaptable), authorizationSubjectFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
    }

    private static void addAttributeResponses(
            final Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyAttributesResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyAttributesResponse.created(thingIdFrom(adaptable), attributesFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyAttributesResponse.modified(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteAttributesResponse.TYPE,
                adaptable -> DeleteAttributesResponse.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyAttributeResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyAttributeResponse.created(thingIdFrom(adaptable), attributePointerFrom(adaptable),
                        attributeValueFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyAttributeResponse.modified(thingIdFrom(adaptable), attributePointerFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteAttributeResponse.TYPE,
                adaptable -> DeleteAttributeResponse.of(thingIdFrom(adaptable), attributePointerFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
    }

    private static void addDefinitionResponses(
            final Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyThingDefinitionResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyThingDefinitionResponse.created(thingIdFrom(adaptable), thingDefinitionFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyThingDefinitionResponse.modified(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteThingDefinitionResponse.TYPE,
                adaptable -> DeleteThingDefinitionResponse.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
    }

    private static void addFeatureResponses(
            final Map<String, JsonifiableMapper<ThingModifyCommandResponse<?>>> mappingStrategies) {
        mappingStrategies.put(ModifyFeaturesResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyFeaturesResponse.created(thingIdFrom(adaptable), featuresFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyFeaturesResponse.modified(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeaturesResponse.TYPE,
                adaptable -> DeleteFeaturesResponse.of(thingIdFrom(adaptable), dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyFeatureResponse.created(thingIdFrom(adaptable), featureFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyFeatureResponse.modified(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureResponse.TYPE,
                adaptable -> DeleteFeatureResponse.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeatureDefinitionResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyFeatureDefinitionResponse.created(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featureDefinitionFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyFeatureDefinitionResponse.modified(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeatureDefinitionResponse.TYPE,
                adaptable -> DeleteFeatureDefinitionResponse.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeaturePropertiesResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyFeaturePropertiesResponse.created(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featurePropertiesFrom(adaptable),
                        dittoHeadersFrom(adaptable))
                        : ModifyFeaturePropertiesResponse.modified(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeaturePropertiesResponse.TYPE,
                adaptable -> DeleteFeaturePropertiesResponse.of(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        dittoHeadersFrom(adaptable)));

        mappingStrategies.put(ModifyFeaturePropertyResponse.TYPE,
                adaptable -> isCreated(adaptable)
                        ? ModifyFeaturePropertyResponse.created(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featurePropertyPointerFrom(adaptable),
                        featurePropertyValueFrom(adaptable), dittoHeadersFrom(adaptable))
                        : ModifyFeaturePropertyResponse.modified(thingIdFrom(adaptable), featureIdFrom(adaptable),
                        featurePropertyPointerFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
        mappingStrategies.put(DeleteFeaturePropertyResponse.TYPE, adaptable -> DeleteFeaturePropertyResponse
                .of(thingIdFrom(adaptable), featureIdFrom(adaptable), featurePropertyPointerFrom(adaptable),
                        dittoHeadersFrom(adaptable)));
    }

}
