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
package org.eclipse.ditto.model.things;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.common.ConditionChecker;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.PolicyId;

/**
 * Representation of one Thing within Ditto.
 */
@Immutable
final class ImmutableThing implements Thing {


    @Nullable private final ThingId thingId;
    @Nullable
    @Deprecated
    private final AccessControlList acl;
    @Nullable private final PolicyId policyId;
    @Nullable private final ThingDefinition definition;
    @Nullable private final Attributes attributes;
    @Nullable private final Features features;
    @Nullable private final ThingLifecycle lifecycle;
    @Nullable private final ThingRevision revision;
    @Nullable private final Instant modified;
    @Nullable private final Instant created;
    @Nullable private final Metadata metadata;

    private ImmutableThing(@Nullable final ThingId thingId,
            @Nullable final AccessControlList acl,
            @Nullable final PolicyId policyId,
            @Nullable final ThingDefinition definition,
            @Nullable final Attributes attributes,
            @Nullable final Features features,
            @Nullable final ThingLifecycle lifecycle,
            @Nullable final ThingRevision revision,
            @Nullable final Instant modified,
            @Nullable final Instant created,
            @Nullable final Metadata metadata) {

        this.thingId = thingId;
        this.acl = acl;

        this.policyId = policyId;
        this.definition = definition;
        this.attributes = attributes;
        this.features = features;
        this.lifecycle = lifecycle;
        this.revision = revision;
        this.modified = modified;
        this.created = created;
        this.metadata = metadata;
    }

    /**
     * Creates a new Thing object which is based on the given values.
     *
     * @param thingId the ID of the Thing to be created.
     * @param accessControlList the Access Control List of the Thing to be created.
     * @param attributes the attributes of the Thing to be created.
     * @param features the features of the Thing to be created.
     * @param lifecycle the lifecycle of the Thing to be created.
     * @param revision the revision of the Thing to be created.
     * @param modified the modified timestamp of the Thing to be created.
     * @param created the created timestamp of the thing to be created.
     * @param metadata the metadata of the Thing to be created.
     * @return the {@code Thing} which was created from the given JSON object.
     * @deprecated ThingId is now typed. Use
     * {@link #of(ThingId, AccessControlList, Attributes, Features, ThingLifecycle, ThingRevision, java.time.Instant,
     * java.time.Instant, Metadata)}
     * instead.
     */
    @Deprecated
    static Thing of(@Nullable final String thingId,
            @Nullable final AccessControlList accessControlList,
            @Nullable final Attributes attributes,
            @Nullable final Features features,
            @Nullable final ThingLifecycle lifecycle,
            @Nullable final ThingRevision revision,
            @Nullable final Instant modified,
            @Nullable final Instant created,
            @Nullable final Metadata metadata) {

        return of(ThingId.of(thingId), accessControlList, attributes, features, lifecycle, revision,
                modified, created, metadata);
    }

    /**
     * Creates a new Thing object which is based on the given values.
     *
     * @param thingId the ID of the Thing to be created.
     * @param accessControlList the Access Control List of the Thing to be created.
     * @param attributes the attributes of the Thing to be created.
     * @param features the features of the Thing to be created.
     * @param lifecycle the lifecycle of the Thing to be created.
     * @param revision the revision of the Thing to be created.
     * @param modified the modified timestamp of the Thing to be created.
     * @param created the created timestamp of the thing to be created.
     * @param metadata the metadata of the Thing to be created.
     * @return the {@code Thing} which was created from the given JSON object.
     * @deprecated deprecated API version 1. Use API version 2 instead.
     */
    @Deprecated
    static Thing of(@Nullable final ThingId thingId,
            @Nullable final AccessControlList accessControlList,
            @Nullable final Attributes attributes,
            @Nullable final Features features,
            @Nullable final ThingLifecycle lifecycle,
            @Nullable final ThingRevision revision,
            @Nullable final Instant modified,
            @Nullable final Instant created,
            @Nullable final Metadata metadata) {

        return new ImmutableThing(thingId, accessControlList, null, null, attributes, features, lifecycle,
                revision, modified, created, metadata);
    }

    /**
     * Creates a new Thing object which is based on the given values.
     *
     * @param thingId the ID of the Thing to be created.
     * @param policyId the Policy ID for the Thing to be created.
     * @param accessControlList the Access Control List of the Thing to be created.
     * @param attributes the attributes of the Thing to be created.
     * @param definition the definition of the Thing to be created.
     * @param features the features of the Thing to be created.
     * @param lifecycle the lifecycle of the Thing to be created.
     * @param revision the revision of the Thing to be created.
     * @param modified the modified timestamp of the thing to be created.
     * @param created the created timestamp of the thing to be created.
     * @param metadata the metadata of the Thing to be created.
     * @return the {@code Thing} which was created from the given JSON object.
     */

    static Thing of(@Nullable final ThingId thingId,

            @Nullable final AccessControlList accessControlList,
            @Nullable final PolicyId policyId,
            @Nullable final ThingDefinition definition,
            @Nullable final Attributes attributes,
            @Nullable final Features features,
            @Nullable final ThingLifecycle lifecycle,
            @Nullable final ThingRevision revision,
            @Nullable final Instant modified,
            @Nullable final Instant created,
            @Nullable final Metadata metadata) {

        return new ImmutableThing(thingId, accessControlList, policyId, definition, attributes, features, lifecycle,
                revision, modified, created, metadata);
    }

    /**
     * Creates a new Thing object which is based on the given values.
     *
     * @param thingId the ID of the Thing to be created.
     * @param policyId the Policy ID for the Thing to be created.
     * @param definition the definition of the Thing to be created.
     * @param attributes the attributes of the Thing to be created.
     * @param features the features of the Thing to be created.
     * @param lifecycle the lifecycle of the Thing to be created.
     * @param revision the revision of the Thing to be created.
     * @param modified the modified timestamp of the Thing to be created.
     * @param created the created timestamp of the Thing to be created.
     * @param metadata the metadata of the Thing to be created.
     * @return the {@code Thing} which was created from the given JSON object.
     */
    static Thing of(@Nullable final ThingId thingId,
            @Nullable final PolicyId policyId,
            @Nullable final ThingDefinition definition,
            @Nullable final Attributes attributes,
            @Nullable final Features features,
            @Nullable final ThingLifecycle lifecycle,
            @Nullable final ThingRevision revision,
            @Nullable final Instant modified,
            @Nullable final Instant created,
            @Nullable final Metadata metadata) {

        return new ImmutableThing(thingId, null, policyId, definition, attributes, features, lifecycle, revision,
                modified, created, metadata);
    }

    @Override
    public Optional<ThingRevision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public Optional<Instant> getModified() {
        return Optional.ofNullable(modified);
    }

    @Override
    public Optional<Instant> getCreated() {
        return Optional.ofNullable(created);
    }

    @Override
    public boolean isDeleted() {
        return ThingLifecycle.DELETED.equals(lifecycle);
    }

    @Override
    public Optional<ThingId> getEntityId() {
        return Optional.ofNullable(thingId);
    }

    @Override
    public Optional<String> getNamespace() {
        return Optional.ofNullable(thingId).map(ThingId::getNamespace);
    }

    @Override
    public Optional<Attributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public Thing setAttributes(@Nullable final Attributes attributes) {
        if (Objects.equals(this.attributes, attributes)) {
            return this;
        }

        return new ImmutableThing(thingId, acl, policyId, definition, attributes, features, lifecycle, revision,
                modified, created, metadata);
    }

    @Override
    public Thing removeAttributes() {
        if (null == attributes) {
            return this;
        }

        return setAttributes(null);
    }

    @Override
    public Thing setAttribute(final JsonPointer attributePath, final JsonValue attributeValue) {
        final Attributes newAttributes;
        if (null == attributes || attributes.isNull()) {
            newAttributes = ThingsModelFactory.newAttributesBuilder()
                    .set(attributePath, attributeValue)
                    .build();
        } else {
            newAttributes = attributes.setValue(attributePath, attributeValue);
        }

        return setAttributes(newAttributes);
    }

    @Override
    public Thing removeAttribute(final JsonPointer attributePath) {
        if (null == attributes || attributes.isEmpty() || attributes.isNull()) {
            return this;
        }

        return setAttributes(attributes.remove(attributePath));
    }

    @Override
    public Optional<ThingDefinition> getDefinition() {
        return (Optional.ofNullable(definition));
    }

    @Override
    public Thing setDefinition(@Nullable final CharSequence definitionIdentifier) {
        if (null != definitionIdentifier) {
            return new ImmutableThing(thingId, acl, policyId,
                    ImmutableThingDefinition.ofParsed(definitionIdentifier),
                    attributes,
                    features,
                    lifecycle,
                    revision,
                    modified,
                    created,
                    metadata);
        } else {
            return new ImmutableThing(thingId, acl, policyId,
                    NullThingDefinition.getInstance(),
                    attributes,
                    features,
                    lifecycle,
                    revision,
                    modified,
                    created,
                    metadata);
        }
    }

    @Override
    public Thing removeDefinition() {
        if (null == this.definition) {
            return this;
        }
        return new ImmutableThing(thingId, acl, policyId, null, attributes, features, lifecycle, revision,
                modified, created, metadata);
    }

    @Override
    public Optional<Features> getFeatures() {
        return Optional.ofNullable(features);
    }

    @Override
    public Thing removeFeatures() {
        if (null == features) {
            return this;
        }

        return setFeatures(null);
    }

    @Override
    public Thing setFeatures(@Nullable final Features features) {
        if (Objects.equals(this.features, features)) {
            return this;
        }

        return new ImmutableThing(thingId, acl, policyId, definition, attributes, features, lifecycle, revision,
                modified, created, metadata);
    }

    @Override
    public Thing setFeature(final Feature feature) {
        final Features newFeatures;
        if (null == features || features.isNull()) {
            newFeatures = ThingsModelFactory.newFeaturesBuilder()
                    .set(feature)
                    .build();
        } else {
            newFeatures = features.setFeature(feature);
        }

        return setFeatures(newFeatures);
    }

    @Override
    public Thing removeFeature(final String featureId) {
        return (null != features) ? setFeatures(features.removeFeature(featureId)) : this;
    }

    @Override
    public Thing setFeatureDefinition(final String featureId, final FeatureDefinition definition) {
        if (null == features || features.isNull()) {
            return setFeature(ThingsModelFactory.newFeature(featureId, definition, null));
        }
        return setFeatures(features.setDefinition(featureId, definition));
    }

    @Override
    public Thing removeFeatureDefinition(final String featureId) {
        return (null != features) ? setFeatures(features.removeDefinition(featureId)) : this;
    }

    @Override
    public Thing setFeatureProperties(final String featureId, final FeatureProperties properties) {
        if (null == features || features.isNull()) {
            return setFeature(ThingsModelFactory.newFeature(featureId, properties));
        }
        return setFeatures(features.setProperties(featureId, properties));
    }

    @Override
    public Thing removeFeatureProperties(final String featureId) {
        return (null != features) ? setFeatures(features.removeProperties(featureId)) : this;
    }

    @Override
    public Thing setFeatureProperty(final String featureId, final JsonPointer propertyJsonPointer,
            final JsonValue propertyValue) {

        final Features newFeatures;
        if (null == features || features.isNull()) {
            final FeatureProperties featureProperties = ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set(propertyJsonPointer, propertyValue)
                    .build();
            newFeatures = ThingsModelFactory.newFeatures(ThingsModelFactory.newFeature(featureId, featureProperties));
        } else {
            newFeatures = features.setProperty(featureId, propertyJsonPointer, propertyValue);
        }

        return setFeatures(newFeatures);
    }

    @Override
    public Thing removeFeatureProperty(final String featureId, final JsonPointer propertyPath) {
        return (null != features) ? setFeatures(features.removeProperty(featureId, propertyPath)) : this;
    }

    @Override
    public Thing setFeatureDesiredProperties(final CharSequence featureId, final FeatureProperties desiredProperties) {
        if (null == features || features.isNull()) {
            return setFeature(ThingsModelFactory.newFeature(featureId, null, null,
                    desiredProperties));
        }
        return setFeatures(features.setDesiredProperties(featureId, desiredProperties));
    }

    @Override
    public Thing removeFeatureDesiredProperties(final CharSequence featureId) {
        return (null != features) ? setFeatures(features.removeDesiredProperties(featureId)) : this;
    }

    @Override
    public Thing setFeatureDesiredProperty(final CharSequence featureId, final JsonPointer desiredPropertyPath,
            final JsonValue desiredPropertyValue) {

        final Features newFeatures;
        if (null == features || features.isNull()) {
            final FeatureProperties desiredProperties = ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set(desiredPropertyPath, desiredPropertyValue)
                    .build();
            newFeatures = ThingsModelFactory.newFeatures(ThingsModelFactory.newFeature(featureId, null, null, desiredProperties));
        } else {
            newFeatures = features.setDesiredProperty(featureId, desiredPropertyPath, desiredPropertyValue);
        }

        return setFeatures(newFeatures);
    }

    @Override
    public Thing removeFeatureDesiredProperty(final CharSequence featureId, final JsonPointer desiredPropertyPath) {
        return (null != features) ? setFeatures(features.removeDesiredProperty(featureId, desiredPropertyPath)) : this;
    }

    @Override
    @Deprecated
    public Optional<AccessControlList> getAccessControlList() {
        return Optional.ofNullable(acl);
    }

    @Override
    @Deprecated
    public Thing setAccessControlList(final AccessControlList accessControlList) {
        if (policyId != null) {
            throw AclInvalidException.newBuilder(thingId)
                    .description("Could not set v1 ACL to Thing already containing v2 Policy")
                    .build();
        } else if (Objects.equals(acl, accessControlList)) {
            return this;
        }

        return new ImmutableThing(thingId, accessControlList, null, definition, attributes, features, lifecycle,
                revision, modified, created, metadata);
    }

    @Override
    @Deprecated
    public Thing setAclEntry(final AclEntry aclEntry) {
        final AccessControlList newAcl;
        if (null == acl || acl.isEmpty()) {
            newAcl = ThingsModelFactory.newAcl(aclEntry);
        } else {
            newAcl = acl.setEntry(aclEntry);
        }

        return setAccessControlList(newAcl);
    }

    @Override
    @Deprecated
    public Thing removeAllPermissionsOf(final AuthorizationSubject authorizationSubject) {
        if (null == acl || acl.isEmpty()) {
            return this;
        }

        return setAccessControlList(acl.removeAllPermissionsOf(authorizationSubject));
    }

    @Override
    public Optional<PolicyId> getPolicyEntityId() {
        return Optional.ofNullable(policyId);
    }

    @Override
    public Thing setPolicyId(@Nullable final PolicyId policyId) {
        return new ImmutableThing(thingId, acl, policyId, definition, attributes, features, lifecycle, revision,
                modified, created, metadata);
    }

    @Override
    public Optional<ThingLifecycle> getLifecycle() {
        return Optional.ofNullable(lifecycle);
    }

    @Override
    public Thing setLifecycle(final ThingLifecycle newLifecycle) {
        ConditionChecker.checkNotNull(newLifecycle, "lifecycle to be set");

        return new ImmutableThing(thingId, acl, policyId, definition, attributes, features, newLifecycle, revision,
                modified, created, metadata);
    }

    @Override
    public Optional<Metadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @Override
    public JsonObject toJson(final JsonSchemaVersion schemaVersion, final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);

        final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder();
        jsonObjectBuilder.set(JsonFields.SCHEMA_VERSION, schemaVersion.toInt(), predicate);

        if (null != lifecycle) {
            jsonObjectBuilder.set(JsonFields.LIFECYCLE, lifecycle.name(), predicate);
        }

        if (null != revision) {
            jsonObjectBuilder.set(JsonFields.REVISION, revision.toLong(), predicate);
        }

        if (null != modified) {
            jsonObjectBuilder.set(JsonFields.MODIFIED, modified.toString(), predicate);
        }

        if (null != created) {
            jsonObjectBuilder.set(JsonFields.CREATED, created.toString(), predicate);
        }

        if (null != thingId) {
            jsonObjectBuilder.set(JsonFields.NAMESPACE, thingId.getNamespace(), predicate);
            jsonObjectBuilder.set(JsonFields.ID, thingId.toString(), predicate);
        }

        if (JsonSchemaVersion.V_1.equals(schemaVersion)) {
            final AccessControlList theAcl = getAccessControlList().orElseGet(ThingsModelFactory::emptyAcl);
            jsonObjectBuilder.set(JsonFields.ACL, theAcl.toJson(), predicate);
        } else {
            if (null != policyId) {
                jsonObjectBuilder.set(JsonFields.POLICY_ID, String.valueOf(policyId), predicate);
            }
            if (null != definition) {
                if (definition instanceof NullThingDefinition) {
                    jsonObjectBuilder.set(JsonFields.DEFINITION, JsonValue.nullLiteral(), predicate);
                } else {
                    jsonObjectBuilder.set(JsonFields.DEFINITION, JsonValue.of(definition.toString()), predicate);
                }
            }
        }

        if (null != attributes) {
            jsonObjectBuilder.set(JsonFields.ATTRIBUTES, attributes, predicate);
        }

        if (null != features) {
            // notice: only "not HIDDEN" sub-fields of features are included
            jsonObjectBuilder.set(JsonFields.FEATURES,
                    features.toJson(schemaVersion, thePredicate.and(FieldType.notHidden())), predicate);
        }

        if (null != metadata) {
            jsonObjectBuilder.set(JsonFields.METADATA, metadata.toJson(schemaVersion, thePredicate), predicate);
        }

        return jsonObjectBuilder.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(thingId, policyId, acl, definition, attributes, features, lifecycle, revision, modified,
                created, metadata);
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "OverlyComplexMethod"})
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImmutableThing other = (ImmutableThing) obj;
        return Objects.equals(thingId, other.thingId) &&
                Objects.equals(policyId, other.policyId) &&
                Objects.equals(acl, other.acl) &&
                Objects.equals(definition, other.definition) &&
                Objects.equals(attributes, other.attributes) &&
                Objects.equals(features, other.features) &&
                Objects.equals(lifecycle, other.lifecycle) &&
                Objects.equals(revision, other.revision) &&
                Objects.equals(modified, other.modified) &&
                Objects.equals(created, other.created) &&
                Objects.equals(metadata, other.metadata);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " ["
                + "thingId=" + thingId +
                ", acl=" + acl +
                ", policyId=" + policyId +
                ", definition=" + definition +
                ", attributes=" + attributes +
                ", features=" + features +
                ", lifecycle=" + lifecycle +
                ", revision=" + revision +
                ", modified=" + modified +
                ", created=" + created +
                ", metadata=" + metadata
                + "]";
    }

}
