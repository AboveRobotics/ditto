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
package org.eclipse.ditto.model.connectivity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.common.ConditionChecker;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

/**
 * Immutable implementation of {@link Source}.
 */
@Immutable
final class ImmutableSource implements Source {

    private static final int DEFAULT_CONSUMER_COUNT = 1;
    private static final int DEFAULT_INDEX = 0;

    private final Set<String> addresses;
    private final int consumerCount;
    private final int index;
    private final AuthorizationContext authorizationContext;
    @Nullable private final Enforcement enforcement;
    @Nullable private final HeaderMapping headerMapping;

    private ImmutableSource(final Builder builder) {
        this.addresses = Collections.unmodifiableSet(
                new HashSet<>(ConditionChecker.checkNotNull(builder.addresses, "addresses")));
        this.consumerCount = builder.consumerCount;
        this.authorizationContext = ConditionChecker.checkNotNull(builder.authorizationContext, "authorizationContext");
        this.index = builder.index;
        this.enforcement = builder.enforcement;
        this.headerMapping = builder.headerMapping;
    }

    @Override
    public Set<String> getAddresses() {
        return addresses;
    }

    @Override
    public int getConsumerCount() {
        return consumerCount;
    }

    @Override
    public AuthorizationContext getAuthorizationContext() {
        return authorizationContext;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Optional<Enforcement> getEnforcement() {
        return Optional.ofNullable(enforcement);
    }

    @Override
    public Optional<HeaderMapping> getHeaderMapping() {
        return Optional.ofNullable(headerMapping);
    }

    @Override
    public JsonObject toJson(final JsonSchemaVersion schemaVersion, final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder();

        jsonObjectBuilder.set(JsonFields.SCHEMA_VERSION, schemaVersion.toInt(), predicate);
        jsonObjectBuilder.set(JsonFields.ADDRESSES, addresses.stream()
                .map(JsonFactory::newValue)
                .collect(JsonCollectors.valuesToArray()), predicate.and(Objects::nonNull));
        jsonObjectBuilder.set(JsonFields.CONSUMER_COUNT, consumerCount, predicate);

        if (!authorizationContext.isEmpty()) {
            jsonObjectBuilder.set(JsonFields.AUTHORIZATION_CONTEXT, authorizationContext.stream()
                    .map(AuthorizationSubject::getId)
                    .map(JsonFactory::newValue)
                    .collect(JsonCollectors.valuesToArray()), predicate);
        }

        if (enforcement != null) {
            jsonObjectBuilder.set(JsonFields.ENFORCEMENT, enforcement.toJson(schemaVersion, thePredicate), predicate);
        }

        if (headerMapping != null) {
            jsonObjectBuilder.set(JsonFields.HEADER_MAPPING, headerMapping.toJson(schemaVersion, thePredicate), predicate);
        }

        return jsonObjectBuilder.build();
    }

    /**
     * Creates a new {@code Source} object from the specified JSON object.
     *
     * @param jsonObject a JSON object which provides the data for the Source to be created.
     * @param index the index to distinguish between sources that would otherwise be different
     * @return a new Source which is initialised with the extracted data from {@code jsonObject}.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if {@code jsonObject} is not an appropriate JSON object.
     */
    public static Source fromJson(final JsonObject jsonObject, final int index) {
        final Set<String> readSources = jsonObject.getValue(JsonFields.ADDRESSES)
                .map(array -> array.stream()
                        .map(JsonValue::asString)
                        .collect(Collectors.toSet())).orElse(Collections.emptySet());
        final int readConsumerCount =
                jsonObject.getValue(JsonFields.CONSUMER_COUNT).orElse(DEFAULT_CONSUMER_COUNT);
        final JsonArray authContext = jsonObject.getValue(JsonFields.AUTHORIZATION_CONTEXT)
                .orElseGet(() -> JsonArray.newBuilder().build());
        final List<AuthorizationSubject> authorizationSubjects = authContext.stream()
                .filter(JsonValue::isString)
                .map(JsonValue::asString)
                .map(AuthorizationSubject::newInstance)
                .collect(Collectors.toList());
        final AuthorizationContext readAuthorizationContext =
                AuthorizationModelFactory.newAuthContext(authorizationSubjects);

        final Enforcement readEnforcement =
                jsonObject.getValue(JsonFields.ENFORCEMENT).map(ImmutableEnforcement::fromJson).orElse(null);

        final HeaderMapping readHeaderMapping =
                jsonObject.getValue(JsonFields.HEADER_MAPPING).map(ImmutableHeaderMapping::fromJson).orElse(null);

        return new Builder()
                .addresses(readSources)
                .authorizationContext(readAuthorizationContext)
                .consumerCount(readConsumerCount)
                .index(index)
                .enforcement(readEnforcement)
                .headerMapping(readHeaderMapping)
                .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ImmutableSource that = (ImmutableSource) o;
        return consumerCount == that.consumerCount &&
                Objects.equals(addresses, that.addresses) &&
                Objects.equals(index, that.index) &&
                Objects.equals(enforcement, that.enforcement) &&
                Objects.equals(headerMapping, that.headerMapping) &&
                Objects.equals(authorizationContext, that.authorizationContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, addresses, consumerCount, authorizationContext, enforcement, headerMapping);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "index=" + index +
                ", addresses=" + addresses +
                ", consumerCount=" + consumerCount +
                ", authorizationContext=" + authorizationContext +
                ", enforcement=" + enforcement +
                ", headerMapping=" + headerMapping +
                "]";
    }

    /**
     * Builder for {@code ImmutableSource}.
     */
    @NotThreadSafe
    static final class Builder implements SourceBuilder<SourceBuilder> {

        // required but changeable:
        @Nullable private Set<String> addresses = new HashSet<>();
        @Nullable private AuthorizationContext authorizationContext;

        // optional:
        @Nullable private Enforcement enforcement;
        @Nullable private HeaderMapping headerMapping;

        // optional with default:
        private int index = DEFAULT_INDEX;
        private int consumerCount = DEFAULT_CONSUMER_COUNT;

        @Override
        public SourceBuilder addresses(final Set<String> addresses) {
            this.addresses = ConditionChecker.checkNotEmpty(addresses, "addresses");
            return this;
        }

        @Override
        public SourceBuilder address(final String address) {
            if (this.addresses == null) {
                this.addresses = new HashSet<>();
            }
            this.addresses.add(address);
            return this;
        }

        @Override
        public SourceBuilder consumerCount(final int consumerCount) {
            this.consumerCount = consumerCount;
            return this;
        }

        @Override
        public SourceBuilder index(final int index) {
            this.index = index;
            return this;
        }

        @Override
        public SourceBuilder authorizationContext(final AuthorizationContext authorizationContext) {
            this.authorizationContext = ConditionChecker.checkNotNull(authorizationContext, "authorizationContext");
            return this;
        }

        @Override
        public SourceBuilder enforcement(@Nullable final Enforcement enforcement) {
            this.enforcement = enforcement;
            return this;
        }

        @Override
        public SourceBuilder headerMapping(@Nullable final HeaderMapping headerMapping) {
            this.headerMapping = headerMapping;
            return this;
        }

        @Override
        public Source build() {
            return new ImmutableSource(this);
        }
    }
}
