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
package org.eclipse.ditto.services.policies.persistence.actors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.eclipse.ditto.services.policies.persistence.TestConstants.Policy.SUBJECT_TYPE;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.modifyPolicyEntryResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.modifyPolicyResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.modifyResourceResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.modifySubjectResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.retrievePolicyEntryResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.retrievePolicyResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.retrieveResourceResponse;
import static org.eclipse.ditto.services.policies.persistence.testhelper.ETagTestUtils.retrieveSubjectResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.eclipse.ditto.model.base.entity.Revision;
import org.eclipse.ditto.model.base.entity.id.DefaultEntityId;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.EffectedPermissions;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.PoliciesResourceType;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyBuilder;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyRevision;
import org.eclipse.ditto.model.policies.PolicyTooLargeException;
import org.eclipse.ditto.model.policies.Resource;
import org.eclipse.ditto.model.policies.Resources;
import org.eclipse.ditto.model.policies.Subject;
import org.eclipse.ditto.model.policies.SubjectExpiry;
import org.eclipse.ditto.model.policies.SubjectExpiryInvalidException;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.model.policies.SubjectIssuer;
import org.eclipse.ditto.model.policies.SubjectType;
import org.eclipse.ditto.model.policies.Subjects;
import org.eclipse.ditto.model.policies.assertions.DittoPolicyAssertions;
import org.eclipse.ditto.services.models.policies.PoliciesMessagingConstants;
import org.eclipse.ditto.services.models.policies.PolicyTag;
import org.eclipse.ditto.services.models.policies.commands.sudo.SudoRetrievePolicy;
import org.eclipse.ditto.services.models.policies.commands.sudo.SudoRetrievePolicyResponse;
import org.eclipse.ditto.services.policies.persistence.TestConstants;
import org.eclipse.ditto.services.policies.persistence.serializer.PolicyMongoSnapshotAdapter;
import org.eclipse.ditto.services.utils.cluster.ShardRegionExtractor;
import org.eclipse.ditto.services.utils.persistence.SnapshotAdapter;
import org.eclipse.ditto.services.utils.persistentactors.AbstractShardedPersistenceActor;
import org.eclipse.ditto.signals.commands.cleanup.CleanupPersistence;
import org.eclipse.ditto.signals.commands.cleanup.CleanupPersistenceResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandSizeValidator;
import org.eclipse.ditto.signals.commands.policies.exceptions.PolicyEntryModificationInvalidException;
import org.eclipse.ditto.signals.commands.policies.exceptions.PolicyEntryNotAccessibleException;
import org.eclipse.ditto.signals.commands.policies.exceptions.PolicyModificationInvalidException;
import org.eclipse.ditto.signals.commands.policies.exceptions.PolicyNotAccessibleException;
import org.eclipse.ditto.signals.commands.policies.exceptions.ResourceNotAccessibleException;
import org.eclipse.ditto.signals.commands.policies.exceptions.SubjectNotAccessibleException;
import org.eclipse.ditto.signals.commands.policies.modify.CreatePolicy;
import org.eclipse.ditto.signals.commands.policies.modify.CreatePolicyResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicy;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicyEntry;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicyEntryResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicyResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeleteResource;
import org.eclipse.ditto.signals.commands.policies.modify.DeleteResourceResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeleteSubject;
import org.eclipse.ditto.signals.commands.policies.modify.DeleteSubjectResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicy;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyEntry;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyResource;
import org.eclipse.ditto.signals.commands.policies.modify.ModifySubject;
import org.eclipse.ditto.signals.commands.policies.modify.ModifySubjects;
import org.eclipse.ditto.signals.commands.policies.query.PolicyQueryCommandResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicy;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyEntry;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveResource;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveSubject;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveSubjectResponse;
import org.eclipse.ditto.signals.events.policies.PolicyCreated;
import org.eclipse.ditto.signals.events.policies.PolicyEntryCreated;
import org.eclipse.ditto.signals.events.policies.SubjectCreated;
import org.eclipse.ditto.signals.events.policies.SubjectDeleted;
import org.junit.Before;
import org.junit.Test;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.cluster.Cluster;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import scala.concurrent.duration.FiniteDuration;

/**
 * Unit test for the {@link PolicyPersistenceActor}.
 */
public final class PolicyPersistenceActorTest extends PersistenceActorTestBase {

    private static final long POLICY_SIZE_LIMIT_BYTES = Long.parseLong(
            System.getProperty(PolicyCommandSizeValidator.DITTO_LIMITS_POLICIES_MAX_SIZE_BYTES, "-1"));

    @Before
    public void setup() {
        setUpBase();
    }

    @Test
    public void tryToRetrievePolicyWhichWasNotYetCreated() {
        final PolicyId policyId = PolicyId.of("test.ns", "23420815");
        final PolicyCommand retrievePolicyCommand = RetrievePolicy.of(policyId, dittoHeadersV2);

        new TestKit(actorSystem) {
            {
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policyId);
                policyPersistenceActor.tell(retrievePolicyCommand, getRef());
                expectMsgClass(PolicyNotAccessibleException.class);
            }
        };
    }

    @Test
    public void createPolicy() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);
            }
        };
    }

    @Test
    public void modifyPolicy() {
        final Policy policy = createPolicyWithRandomId();
        final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);

        final Policy modifiedPolicy = policy.setEntry(
                PoliciesModelFactory.newPolicyEntry(Label.of("anotherOne"), POLICY_SUBJECTS, POLICY_RESOURCES_ALL));
        final ModifyPolicy modifyPolicyCommand =
                ModifyPolicy.of(policy.getEntityId().get(), modifiedPolicy, dittoHeadersV2);

        new TestKit(actorSystem) {
            {
                final ActorRef underTest = createPersistenceActorFor(this, policy);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicyResponse = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicyResponse.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                underTest.tell(modifyPolicyCommand, getRef());
                expectMsgEquals(modifyPolicyResponse(incrementRevision(policy, 2), dittoHeadersV2, false));
            }
        };
    }

    @Test
    public void retrievePolicy() {
        final Policy policy = createPolicyWithRandomId();
        final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
        final PolicyCommand retrievePolicyCommand =
                RetrievePolicy.of(policy.getEntityId().orElse(null), dittoHeadersV2);
        final PolicyQueryCommandResponse expectedResponse =
                retrievePolicyResponse(incrementRevision(policy, 1), retrievePolicyCommand.getDittoHeaders());

        new TestKit(actorSystem) {
            {
                final ActorRef underTest = createPersistenceActorFor(this, policy);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                underTest.tell(retrievePolicyCommand, getRef());
                expectMsgEquals(expectedResponse);
            }
        };
    }

    @Test
    public void sudoRetrievePolicy() {
        final Policy policy = createPolicyWithRandomId();
        final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);

        final SudoRetrievePolicy sudoRetrievePolicyCommand =
                SudoRetrievePolicy.of(policy.getEntityId().orElseThrow(NoSuchElementException::new), dittoHeadersV2);

        new TestKit(actorSystem) {
            {
                final ActorRef underTest = createPersistenceActorFor(this, policy);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                underTest.tell(sudoRetrievePolicyCommand, getRef());
                expectMsgClass(SudoRetrievePolicyResponse.class);
            }
        };
    }

    @Test
    public void deletePolicy() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().orElse(null))
                        .isEqualEqualToButModified(policy);

                final PolicyId policyId = policy.getEntityId().orElseThrow(NoSuchElementException::new);
                final DeletePolicy deletePolicy = DeletePolicy.of(policyId, dittoHeadersV2);
                policyPersistenceActor.tell(deletePolicy, getRef());
                expectMsgEquals(DeletePolicyResponse.of(policyId, dittoHeadersV2));
            }
        };
    }

    @Test
    public void createPolicyEntry() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final PolicyEntry policyEntryToAdd =
                        PoliciesModelFactory.newPolicyEntry(Label.of("anotherLabel"),
                                POLICY_SUBJECTS, POLICY_RESOURCES_ALL);

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifyPolicyEntry modifyPolicyEntry =
                        ModifyPolicyEntry.of(policyId, policyEntryToAdd, headersMockWithOtherAuth);
                underTest.tell(modifyPolicyEntry, getRef());
                expectMsgEquals(
                        modifyPolicyEntryResponse(policyId, policyEntryToAdd, headersMockWithOtherAuth, true));
                final RetrievePolicyEntry retrievePolicyEntry =
                        RetrievePolicyEntry.of(policyId, policyEntryToAdd.getLabel(), headersMockWithOtherAuth);
                underTest.tell(retrievePolicyEntry, getRef());
                expectMsgEquals(retrievePolicyEntryResponse(policyId, policyEntryToAdd, headersMockWithOtherAuth));
            }
        };
    }

    @Test
    public void modifyPolicyEntry() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Subject newSubject = Subject.newInstance(SubjectIssuer.GOOGLE, "anotherOne");
                final PolicyEntry policyEntryToModify = PoliciesModelFactory.newPolicyEntry(POLICY_LABEL,
                        Subjects.newInstance(POLICY_SUBJECT, newSubject), POLICY_RESOURCES_ALL);

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifyPolicyEntry modifyPolicyEntry =
                        ModifyPolicyEntry.of(policyId, policyEntryToModify, headersMockWithOtherAuth);
                underTest.tell(modifyPolicyEntry, getRef());
                expectMsgEquals(
                        modifyPolicyEntryResponse(policyId, policyEntryToModify, headersMockWithOtherAuth, false));

                final RetrievePolicyEntry retrievePolicyEntry =
                        RetrievePolicyEntry.of(policyId, policyEntryToModify.getLabel(), headersMockWithOtherAuth);
                underTest.tell(retrievePolicyEntry, getRef());
                expectMsgEquals(retrievePolicyEntryResponse(policyId, policyEntryToModify, headersMockWithOtherAuth));
            }
        };
    }

    @Test
    public void tryToModifyPolicyEntryWithInvalidPermissions() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Subject newSubject = Subject.newInstance(SubjectIssuer.GOOGLE, "anotherOne");

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final PolicyEntry policyEntryToModify = PoliciesModelFactory.newPolicyEntry(POLICY_LABEL,
                        Subjects.newInstance(POLICY_SUBJECT, newSubject), POLICY_RESOURCES_READ);

                final ModifyPolicyEntry modifyPolicyEntry =
                        ModifyPolicyEntry.of(policyId, policyEntryToModify, headersMockWithOtherAuth);
                underTest.tell(modifyPolicyEntry, getRef());
                expectMsgClass(PolicyEntryModificationInvalidException.class);
            }
        };
    }

    @Test
    public void modifyPolicyEntrySoThatPolicyGetsTooLarge() {
        new TestKit(actorSystem) {
            {
                final PolicyBuilder policyBuilder = Policy.newBuilder(PolicyId.of("new", "policy"));
                int i = 0;
                Policy policy;
                do {
                    policyBuilder.forLabel("ENTRY-NO" + i)
                            .setSubject("nginx:ditto", SubjectType.UNKNOWN)
                            .setGrantedPermissions("policy", "/", "READ", "WRITE")
                            .setGrantedPermissions("thing", "/", "READ", "WRITE");
                    policy = policyBuilder.build();
                    i++;
                } while (policy.toJsonString().length() < POLICY_SIZE_LIMIT_BYTES);

                policy = policy.removeEntry("ENTRY-NO" + (i - 1));

                final ActorRef underTest = createPersistenceActorFor(this, policy);

                // creating the Policy should be possible as we are below the limit:
                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final PolicyEntry policyEntry = Policy.newBuilder(PolicyId.of("new", "policy"))
                        .forLabel("TEST")
                        .setSubject("nginx:ditto", SubjectType.UNKNOWN)
                        .setGrantedPermissions("policy", "/", "READ", "WRITE")
                        .build()
                        .getEntryFor("TEST")
                        .get();

                final ModifyPolicyEntry command =
                        ModifyPolicyEntry.of(policy.getEntityId().get(), policyEntry, DittoHeaders.empty());

                // but modifying the policy entry which would cause the Policy to exceed the limit should not be allowed:
                underTest.tell(command, getRef());
                expectMsgClass(PolicyTooLargeException.class);
            }
        };
    }

    @Test
    public void removePolicyEntry() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final DeletePolicyEntry deletePolicyEntry =
                        DeletePolicyEntry.of(policyId, ANOTHER_POLICY_LABEL, headersMockWithOtherAuth);
                underTest.tell(deletePolicyEntry, getRef());
                expectMsgEquals(DeletePolicyEntryResponse.of(policyId, ANOTHER_POLICY_LABEL, headersMockWithOtherAuth));

                final RetrievePolicyEntry retrievePolicyEntry =
                        RetrievePolicyEntry.of(policyId, ANOTHER_POLICY_LABEL, headersMockWithOtherAuth);
                final PolicyEntryNotAccessibleException expectedResponse = PolicyEntryNotAccessibleException
                        .newBuilder(retrievePolicyEntry.getEntityId(), retrievePolicyEntry.getLabel())
                        .dittoHeaders(retrievePolicyEntry.getDittoHeaders())
                        .build();
                underTest.tell(retrievePolicyEntry, getRef());
                expectMsgEquals(expectedResponse);
            }
        };
    }

    @Test
    public void tryToRemoveLastPolicyEntry() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final DeletePolicyEntry deletePolicyEntry =
                        DeletePolicyEntry.of(policyId, POLICY_LABEL, headersMockWithOtherAuth);
                underTest.tell(deletePolicyEntry, getRef());
                expectMsgClass(PolicyEntryModificationInvalidException.class);
            }
        };
    }

    @Test
    public void createResource() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Resource resourceToAdd = Resource.newInstance(PoliciesResourceType.policyResource(
                        "/attributes"), EffectedPermissions.newInstance(PoliciesModelFactory.noPermissions(),
                        TestConstants.Policy.PERMISSIONS_ALL));

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifyResource modifyResource =
                        ModifyResource.of(policyId, POLICY_LABEL, resourceToAdd, headersMockWithOtherAuth);
                underTest.tell(modifyResource, getRef());
                expectMsgEquals(
                        modifyResourceResponse(policyId, resourceToAdd, POLICY_LABEL, headersMockWithOtherAuth, true));

                final RetrieveResource retrieveResource =
                        RetrieveResource.of(policyId, POLICY_LABEL, resourceToAdd.getResourceKey(),
                                headersMockWithOtherAuth);
                underTest.tell(retrieveResource, getRef());
                expectMsgEquals(
                        retrieveResourceResponse(policyId, POLICY_LABEL, resourceToAdd, headersMockWithOtherAuth));
            }
        };
    }

    @Test
    public void modifyResource() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Resource resourceToModify = Resource.newInstance(PoliciesResourceType.policyResource(
                        POLICY_RESOURCE_PATH), EffectedPermissions.newInstance(TestConstants.Policy.PERMISSIONS_ALL,
                        PoliciesModelFactory.noPermissions()));

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifyResource modifyResource =
                        ModifyResource.of(policyId, ANOTHER_POLICY_LABEL, resourceToModify, headersMockWithOtherAuth);
                underTest.tell(modifyResource, getRef());
                expectMsgEquals(modifyResourceResponse(policyId, resourceToModify, ANOTHER_POLICY_LABEL,
                        headersMockWithOtherAuth, false));

                final RetrieveResource retrieveResource = RetrieveResource.of(policyId, ANOTHER_POLICY_LABEL,
                        resourceToModify.getResourceKey(), headersMockWithOtherAuth);
                underTest.tell(retrieveResource, getRef());
                expectMsgEquals(retrieveResourceResponse(policyId, ANOTHER_POLICY_LABEL, resourceToModify,
                        headersMockWithOtherAuth));
            }
        };
    }

    @Test
    public void tryToModifyResource() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Resource resourceToModify = Resource.newInstance(
                        PoliciesResourceType.policyResource(POLICY_RESOURCE_PATH),
                        EffectedPermissions.newInstance(PoliciesModelFactory.noPermissions(),
                                TestConstants.Policy.PERMISSIONS_ALL));

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifyResource modifyResource =
                        ModifyResource.of(policyId, ANOTHER_POLICY_LABEL, resourceToModify, headersMockWithOtherAuth);
                underTest.tell(modifyResource, getRef());
                expectMsgClass(PolicyEntryModificationInvalidException.class);
            }
        };
    }

    @Test
    public void removeResource() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final DeleteResource deleteResource =
                        DeleteResource.of(policyId, ANOTHER_POLICY_LABEL,
                                PoliciesResourceType.policyResource(POLICY_RESOURCE_PATH),
                                headersMockWithOtherAuth);
                underTest.tell(deleteResource, getRef());
                expectMsgEquals(DeleteResourceResponse.of(policyId, ANOTHER_POLICY_LABEL,
                        PoliciesResourceType.policyResource(POLICY_RESOURCE_PATH), headersMockWithOtherAuth));

                final RetrieveResource retrieveResource =
                        RetrieveResource.of(policyId, ANOTHER_POLICY_LABEL,
                                PoliciesResourceType.policyResource(POLICY_RESOURCE_PATH),
                                headersMockWithOtherAuth);
                final ResourceNotAccessibleException expectedResponse = ResourceNotAccessibleException
                        .newBuilder(retrieveResource.getEntityId(),
                                retrieveResource.getLabel(),
                                retrieveResource.getResourceKey().toString())
                        .dittoHeaders(retrieveResource.getDittoHeaders())
                        .build();
                underTest.tell(retrieveResource, getRef());
                expectMsgEquals(expectedResponse);
            }
        };
    }

    @Test
    public void createSubject() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Subject subjectToAdd =
                        Subject.newInstance(SubjectIssuer.GOOGLE, "anotherSubjectId");

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifySubject modifySubject =
                        ModifySubject.of(policyId, POLICY_LABEL, subjectToAdd, headersMockWithOtherAuth);
                underTest.tell(modifySubject, getRef());
                expectMsgEquals(
                        modifySubjectResponse(policyId, POLICY_LABEL, subjectToAdd, headersMockWithOtherAuth, true));

                final RetrieveSubject retrieveSubject =
                        RetrieveSubject.of(policyId, POLICY_LABEL, subjectToAdd.getId(), headersMockWithOtherAuth);
                final RetrieveSubjectResponse expectedResponse =
                        retrieveSubjectResponse(policyId, POLICY_LABEL, subjectToAdd, headersMockWithOtherAuth);
                underTest.tell(retrieveSubject, getRef());
                expectMsgEquals(expectedResponse);
            }
        };
    }

    @Test
    public void modifySubject() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Subject subjectToModify =
                        Subject.newInstance(POLICY_SUBJECT_ID, SUBJECT_TYPE);

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final ModifySubject modifySubject =
                        ModifySubject.of(policyId, POLICY_LABEL, subjectToModify, headersMockWithOtherAuth);
                underTest.tell(modifySubject, getRef());
                expectMsgEquals(modifySubjectResponse(policyId, POLICY_LABEL, subjectToModify, headersMockWithOtherAuth,
                        false));

                final RetrieveSubject retrieveSubject =
                        RetrieveSubject.of(policyId, POLICY_LABEL, subjectToModify.getId(), headersMockWithOtherAuth);
                underTest.tell(retrieveSubject, getRef());
                expectMsgEquals(
                        retrieveSubjectResponse(policyId, POLICY_LABEL, subjectToModify, headersMockWithOtherAuth));
            }
        };
    }

    @Test
    public void removeSubject() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final DeleteSubject deleteSubject =
                        DeleteSubject.of(policyId, ANOTHER_POLICY_LABEL, POLICY_SUBJECT_ID, headersMockWithOtherAuth);
                underTest.tell(deleteSubject, getRef());
                expectMsgEquals(
                        DeleteSubjectResponse.of(policyId, ANOTHER_POLICY_LABEL, POLICY_SUBJECT_ID,
                                headersMockWithOtherAuth));

                final RetrieveSubject retrieveSubject =
                        RetrieveSubject.of(policyId, ANOTHER_POLICY_LABEL, POLICY_SUBJECT_ID, headersMockWithOtherAuth);
                final SubjectNotAccessibleException expectedResponse = SubjectNotAccessibleException
                        .newBuilder(retrieveSubject.getEntityId(),
                                retrieveSubject.getLabel(),
                                retrieveSubject.getSubjectId())
                        .dittoHeaders(retrieveSubject.getDittoHeaders())
                        .build();
                underTest.tell(retrieveSubject, getRef());
                expectMsgEquals(expectedResponse);
            }
        };
    }

    @Test
    public void createSubjectWithExpiry() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final Instant expiryInstant = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        .plus(2, ChronoUnit.SECONDS)
                        .atZone(ZoneId.systemDefault()).toInstant();
                final SubjectExpiry subjectExpiry = SubjectExpiry.newInstance(expiryInstant);
                final Subject subjectToAdd =
                        Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "anotherSubjectId"),
                                SubjectType.GENERATED, subjectExpiry);

                final DittoHeaders headersMockWithOtherAuth =
                        createDittoHeaders(JsonSchemaVersion.LATEST, AUTH_SUBJECT, UNAUTH_SUBJECT);

                final PolicyId policyId = policy.getEntityId().orElse(null);
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                // GIVEN: a Policy is created without a Subject having an "expiry" date
                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                // THEN: a PolicyCreated event should be emitted
                final DistributedPubSubMediator.Publish policyCreatedPublish =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                assertThat(policyCreatedPublish.msg()).isInstanceOf(PolicyCreated.class);

                // WHEN: the Policy's subject is modified having an "expiry" in the near future
                final ModifySubject modifySubject =
                        ModifySubject.of(policyId, POLICY_LABEL, subjectToAdd, headersMockWithOtherAuth);
                underTest.tell(modifySubject, getRef());

                // THEN: a SubjectCreated event should be emitted
                final DistributedPubSubMediator.Publish subjectCreatedPublish =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                assertThat(subjectCreatedPublish.msg()).isInstanceOf(SubjectCreated.class);

                final long secondsToAdd = 10 - (expiryInstant.getEpochSecond() % 10);
                final Instant expectedRoundedExpiryInstant =
                        expiryInstant.plusSeconds(secondsToAdd); // to next 10s rounded up
                final SubjectExpiry expectedSubjectExpiry = SubjectExpiry.newInstance(expectedRoundedExpiryInstant);
                final Subject expectedAdjustedSubjectToAdd = Subject.newInstance(subjectToAdd.getId(),
                        subjectToAdd.getType(), expectedSubjectExpiry);

                // THEN: the subject expiry should be rounded up to the configured "subject-expiry-granularity"
                //  (10s for this test)
                expectMsgEquals(
                        modifySubjectResponse(policyId, POLICY_LABEL, expectedAdjustedSubjectToAdd,
                                headersMockWithOtherAuth, true));

                final RetrieveSubject retrieveSubject =
                        RetrieveSubject.of(policyId, POLICY_LABEL, subjectToAdd.getId(), headersMockWithOtherAuth);
                final RetrieveSubjectResponse expectedResponse =
                        retrieveSubjectResponse(policyId, POLICY_LABEL, expectedAdjustedSubjectToAdd,
                                headersMockWithOtherAuth);
                underTest.tell(retrieveSubject, getRef());
                expectMsgEquals(expectedResponse);

                // THEN: waiting until the expiry interval should emit a SubjectDeleted event
                final Duration between =
                        Duration.between(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
                                expectedRoundedExpiryInstant);
                final long secondsToWaitForSubjectDeletedEvent = between.getSeconds() + 2;
                final DistributedPubSubMediator.Publish policySubjectDeleted =
                        pubSubMediatorTestProbe.expectMsgClass(
                                FiniteDuration.apply(secondsToWaitForSubjectDeletedEvent, TimeUnit.SECONDS),
                                DistributedPubSubMediator.Publish.class);
                final Object subjectDeletedMsg = policySubjectDeleted.msg();
                assertThat(subjectDeletedMsg).isInstanceOf(SubjectDeleted.class);
                assertThat(((SubjectDeleted) subjectDeletedMsg).getSubjectId())
                        .isEqualTo(expectedAdjustedSubjectToAdd.getId());

                // THEN: a PolicyTag should be emitted via pub/sub indicating that the policy enforcer caches should be invalidated
                final DistributedPubSubMediator.Publish policyTagForCacheInvalidation =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                final Object policyTagForCacheInvalidationMsg = policyTagForCacheInvalidation.msg();
                assertThat(policyTagForCacheInvalidationMsg).isInstanceOf(PolicyTag.class);
                assertThat(((PolicyTag) policyTagForCacheInvalidationMsg).getEntityId())
                        .isEqualTo(policyId);

                // THEN: retrieving the expired subject should fail
                underTest.tell(retrieveSubject, getRef());
                expectMsgClass(SubjectNotAccessibleException.class);
            }
        };
    }

    @Test
    public void createPolicyWith2SubjectsWithExpiry() {
        new TestKit(actorSystem) {{
            final Instant expiryInstant = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .plus(2, ChronoUnit.SECONDS)
                    .atZone(ZoneId.systemDefault()).toInstant();
            final SubjectExpiry subjectExpiry = SubjectExpiry.newInstance(expiryInstant);
            final Subject subject1 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject1"),
                            SubjectType.GENERATED, subjectExpiry);
            final Subject subject2 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject2"),
                            SubjectType.GENERATED, subjectExpiry);
            final Subject subject3 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject3"), SubjectType.GENERATED);
            final Policy policy = PoliciesModelFactory.newPolicyBuilder(PolicyId.of("policy:id"))
                    .forLabel(POLICY_LABEL)
                    .setSubjects(Subjects.newInstance(subject1, subject2, subject3))
                    .setResources(POLICY_RESOURCES_ALL)
                    .build();

            final ActorRef underTest = createPersistenceActorFor(this, policy);

            // GIVEN: a Policy is created with 2 subjects having an "expiry" date
            final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
            underTest.tell(createPolicyCommand, getRef());
            expectMsgClass(CreatePolicyResponse.class);

            // THEN: a PolicyCreated event should be emitted
            final DistributedPubSubMediator.Publish policyCreatedPublish =
                    pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
            assertThat(policyCreatedPublish.msg()).isInstanceOf(PolicyCreated.class);
            assertThat(((PolicyCreated) policyCreatedPublish.msg()).getRevision()).isEqualTo(1L);

            // THEN: subject1 is deleted after expiry
            final long secondsToAdd = 10 - (expiryInstant.getEpochSecond() % 10);
            final Instant expectedRoundedExpiryInstant = expiryInstant.plusSeconds(secondsToAdd);
            final Duration between =
                    Duration.between(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
                            expectedRoundedExpiryInstant);
            final long secondsToWaitForSubjectDeletedEvent = between.getSeconds() + 2;
            final DistributedPubSubMediator.Publish subject1Deleted =
                    pubSubMediatorTestProbe.expectMsgClass(
                            FiniteDuration.apply(secondsToWaitForSubjectDeletedEvent, TimeUnit.SECONDS),
                            DistributedPubSubMediator.Publish.class);
            assertThat(subject1Deleted.msg()).isInstanceOf(SubjectDeleted.class);
            assertThat(((SubjectDeleted) subject1Deleted.msg()).getSubjectId()).isEqualTo(subject1.getId());
            assertThat(((SubjectDeleted) subject1Deleted.msg()).getRevision()).isEqualTo(2L);
            assertThat(pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class).msg())
                    .isInstanceOf(PolicyTag.class);

            // THEN: subject2 is deleted immediately
            final DistributedPubSubMediator.Publish subject2Deleted =
                    pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
            assertThat(subject2Deleted.msg()).isInstanceOf(SubjectDeleted.class);
            assertThat(((SubjectDeleted) subject2Deleted.msg()).getSubjectId()).isEqualTo(subject2.getId());
            assertThat(((SubjectDeleted) subject2Deleted.msg()).getRevision()).isEqualTo(3L);

            // THEN: the policy has only subject3 left.
            underTest.tell(RetrievePolicy.of(policy.getEntityId().orElseThrow(), DittoHeaders.empty()), getRef());
            final RetrievePolicyResponse response = expectMsgClass(RetrievePolicyResponse.class);
            Assertions.assertThat(response.getPolicy().getEntryFor(POLICY_LABEL).orElseThrow().getSubjects())
                    .containsOnly(subject3);
        }};
    }

    @Test
    public void impossibleToMakePolicyInvalidByExpiringSubjects() {
        new TestKit(actorSystem) {{
            final Instant futureInstant = Instant.now().plus(Duration.ofDays(1L));
            final SubjectExpiry subjectExpiry = SubjectExpiry.newInstance(futureInstant);
            final Subject subject1 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject1"),
                            SubjectType.GENERATED, subjectExpiry);
            final Subject subject2 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject2"),
                            SubjectType.GENERATED, subjectExpiry);
            final Subject subject3 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject3"),
                            SubjectType.GENERATED);
            final Subject subject4 =
                    Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "subject4"),
                            SubjectType.GENERATED,
                            SubjectExpiry.newInstance(Instant.EPOCH));
            final PolicyId policyId = PolicyId.of("policy:id");
            final DittoHeaders headers = DittoHeaders.empty();

            final Policy validPolicy = PoliciesModelFactory.newPolicyBuilder(policyId)
                    .forLabel(POLICY_LABEL)
                    .setSubjects(Subjects.newInstance(subject3))
                    .setResources(POLICY_RESOURCES_ALL)
                    .build();

            final Policy policyWithExpiredSubject = PoliciesModelFactory.newPolicyBuilder(policyId)
                    .forLabel(POLICY_LABEL)
                    .setSubjects(Subjects.newInstance(subject1, subject2, subject3, subject4))
                    .setResources(POLICY_RESOURCES_ALL)
                    .build();

            final Subjects expiringSubjects = Subjects.newInstance(subject1, subject2);
            final PolicyEntry expiringEntry =
                    PoliciesModelFactory.newPolicyEntry(POLICY_LABEL, expiringSubjects, POLICY_RESOURCES_ALL);
            final Policy policyWithoutPermanentSubject = PoliciesModelFactory.newPolicyBuilder(policyId)
                    .set(expiringEntry)
                    .build();

            // GIVEN: policy is not created
            final ActorRef underTest = createPersistenceActorFor(this, policyId);

            // WHEN/THEN CreatePolicy fails if policy contains expired subject
            underTest.tell(CreatePolicy.of(policyWithExpiredSubject, headers), getRef());
            expectMsgClass(SubjectExpiryInvalidException.class);

            // GIVEN: policy is created
            underTest.tell(CreatePolicy.of(validPolicy, headers), getRef());
            expectMsgClass(CreatePolicyResponse.class);

            // WHEN/THEN ModifyPolicy fails if policy contains only expiring subjects afterwards
            underTest.tell(ModifyPolicy.of(policyId, policyWithoutPermanentSubject, headers), getRef());
            expectMsgClass(PolicyModificationInvalidException.class);

            // WHEN/THEN ModifyPolicyEntry fails if policy contains only expiring subjects afterwards
            underTest.tell(ModifyPolicyEntry.of(policyId, expiringEntry, headers), getRef());
            expectMsgClass(PolicyEntryModificationInvalidException.class);

            // WHEN/THEN ModifySubjects fails if policy contains only expiring subjects afterwards
            underTest.tell(ModifySubjects.of(policyId, POLICY_LABEL, expiringSubjects, headers), getRef());
            expectMsgClass(PolicyEntryModificationInvalidException.class);

            // WHEN/THEN DeleteSubject fails if policy contains only expiring subjects afterwards
            underTest.tell(DeleteSubject.of(policyId, POLICY_LABEL, subject3.getId(), headers), getRef());
            expectMsgClass(PolicyEntryModificationInvalidException.class);
        }};
    }

    @Test
    public void recoverPolicyCreated() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                // restart
                terminate(this, policyPersistenceActor);
                final ActorRef policyPersistenceActorRecovered = createPersistenceActorFor(this, policy);
                final RetrievePolicy retrievePolicy =
                        RetrievePolicy.of(policy.getEntityId().orElse(null), dittoHeadersV2);

                final RetrievePolicyResponse expectedResponse =
                        retrievePolicyResponse(incrementRevision(policy, 1), dittoHeadersV2);

                Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                    policyPersistenceActorRecovered.tell(retrievePolicy, getRef());
                    expectMsgEquals(expectedResponse);
                });

                assertThat(getLastSender()).isEqualTo(policyPersistenceActorRecovered);
            }
        };
    }

    @Test
    public void recoverPolicyDeleted() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final DeletePolicy deletePolicy = DeletePolicy.of(policy.getEntityId().get(), dittoHeadersV2);
                policyPersistenceActor.tell(deletePolicy, getRef());
                expectMsgEquals(DeletePolicyResponse.of(policy.getEntityId().get(), dittoHeadersV2));

                // restart
                terminate(this, policyPersistenceActor);
                final ActorRef policyPersistenceActorRecovered = createPersistenceActorFor(this, policy);

                // A deleted Policy cannot be retrieved anymore.
                final RetrievePolicy retrievePolicy = RetrievePolicy.of(policy.getEntityId().get(), dittoHeadersV2);
                policyPersistenceActorRecovered.tell(retrievePolicy, getRef());
                expectMsgClass(PolicyNotAccessibleException.class);

                assertThat(getLastSender()).isEqualTo(policyPersistenceActorRecovered);
            }
        };
    }

    @Test
    public void recoverPolicyEntryModified() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                // event published with group:
                final DistributedPubSubMediator.Publish policyCreatedPublishSecond =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                assertThat(policyCreatedPublishSecond.msg()).isInstanceOf(PolicyCreated.class);

                final Subject newSubject =
                        Subject.newInstance(SubjectIssuer.GOOGLE, "anotherOne");
                final Resource newResource =
                        Resource.newInstance(PoliciesResourceType.policyResource("/attributes"), EffectedPermissions
                                .newInstance(PoliciesModelFactory.noPermissions(),
                                        TestConstants.Policy.PERMISSIONS_ALL));
                final PolicyEntry policyEntry = PoliciesModelFactory.newPolicyEntry(Label.of("anotherLabel"),
                        Subjects.newInstance(newSubject), Resources.newInstance(newResource));

                final ModifyPolicyEntry modifyPolicyEntry =
                        ModifyPolicyEntry.of(policy.getEntityId().orElse(null), policyEntry, dittoHeadersV2);
                policyPersistenceActor.tell(modifyPolicyEntry, getRef());
                expectMsgEquals(modifyPolicyEntryResponse(policy.getEntityId().get(), policyEntry,
                        dittoHeadersV2, true));

                // event published with group:
                final DistributedPubSubMediator.Publish policyEntryModifiedPublishSecond =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                assertThat(policyEntryModifiedPublishSecond.msg()).isInstanceOf(PolicyEntryCreated.class);

                // restart
                terminate(this, policyPersistenceActor);
                final ActorRef policyPersistenceActorRecovered = createPersistenceActorFor(this, policy);

                final Policy policyWithUpdatedPolicyEntry = policy.setEntry(policyEntry);
                final RetrievePolicy retrievePolicy =
                        RetrievePolicy.of(policyWithUpdatedPolicyEntry.getEntityId().orElse(null), dittoHeadersV2);
                final RetrievePolicyResponse expectedResponse =
                        retrievePolicyResponse(incrementRevision(policyWithUpdatedPolicyEntry, 2), dittoHeadersV2);

                Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                    policyPersistenceActorRecovered.tell(retrievePolicy, getRef());
                    expectMsgEquals(expectedResponse);
                });

                assertThat(getLastSender()).isEqualTo(policyPersistenceActorRecovered);
            }
        };
    }

    @Test
    public void recoverPolicyEntryDeleted() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                final DeletePolicyEntry deletePolicyEntry =
                        DeletePolicyEntry.of(policy.getEntityId().get(), ANOTHER_POLICY_LABEL, dittoHeadersV2);
                policyPersistenceActor.tell(deletePolicyEntry, getRef());
                expectMsgEquals(DeletePolicyEntryResponse.of(policy.getEntityId().orElse(null), ANOTHER_POLICY_LABEL,
                        dittoHeadersV2));

                // restart
                terminate(this, policyPersistenceActor);
                final ActorRef policyPersistenceActorRecovered = createPersistenceActorFor(this, policy);

                final RetrievePolicy retrievePolicy =
                        RetrievePolicy.of(policy.getEntityId().get(), dittoHeadersV2);
                final RetrievePolicyResponse expectedResponse =
                        retrievePolicyResponse(incrementRevision(policy, 2).removeEntry(ANOTHER_POLICY_LABEL),
                                dittoHeadersV2);

                Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                    policyPersistenceActorRecovered.tell(retrievePolicy, getRef());
                    expectMsgEquals(expectedResponse);
                });

                assertThat(getLastSender()).isEqualTo(policyPersistenceActorRecovered);
            }
        };
    }

    @Test
    public void ensureSubjectExpiryIsCleanedUpAfterRecovery() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final PolicyId policyId = policy.getEntityId().orElseThrow();
                final ActorRef underTest = createPersistenceActorFor(this, policy);

                final Instant expiryInstant = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        .plus(2, ChronoUnit.SECONDS)
                        .atZone(ZoneId.systemDefault()).toInstant();
                final SubjectExpiry subjectExpiry = SubjectExpiry.newInstance(expiryInstant);
                final Subject expiringSubject =
                        Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "about-to-expire"),
                                SubjectType.GENERATED, subjectExpiry);
                final Policy policyWithExpiringSubject = policy.toBuilder()
                        .setSubjectFor(TestConstants.Policy.SUPPORT_LABEL, expiringSubject)
                        .build();

                final long secondsToAdd = 10 - (expiryInstant.getEpochSecond() % 10);
                final Instant expectedRoundedExpiryInstant =
                        expiryInstant.plusSeconds(secondsToAdd); // to next 10s rounded up
                final SubjectExpiry expectedSubjectExpiry = SubjectExpiry.newInstance(expectedRoundedExpiryInstant);
                final Subject expectedAdjustedSubjectToAdd = Subject.newInstance(expiringSubject.getId(),
                        expiringSubject.getType(), expectedSubjectExpiry);

                final Policy adjustedPolicyWithExpiringSubject = policyWithExpiringSubject.toBuilder()
                        .setSubjectFor(TestConstants.Policy.SUPPORT_LABEL, expectedAdjustedSubjectToAdd)
                        .build();

                // WHEN: a new Policy is created containing an expiring subject
                final CreatePolicy createPolicyCommand = CreatePolicy.of(policyWithExpiringSubject, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());

                // THEN: the response should contain the adjusted (rounded up) expiry time
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(adjustedPolicyWithExpiringSubject);

                // THEN: the created event should be emitted
                final DistributedPubSubMediator.Publish policyCreatedPublishSecond =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                assertThat(policyCreatedPublishSecond.msg()).isInstanceOf(PolicyCreated.class);

                // WHEN: now the persistence actor is restarted
                terminate(this, underTest);
                final ActorRef underTestRecovered = createPersistenceActorFor(this, policy);

                // AND WHEN: the policy is retrieved (and restored as a consequence)
                final RetrievePolicy retrievePolicy = RetrievePolicy.of(policyId, dittoHeadersV2);
                final RetrievePolicyResponse expectedResponse =
                        retrievePolicyResponse(incrementRevision(adjustedPolicyWithExpiringSubject, 1), dittoHeadersV2);

                Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                    underTestRecovered.tell(retrievePolicy, getRef());
                    expectMsgEquals(expectedResponse);
                });
                assertThat(getLastSender()).isEqualTo(underTestRecovered);

                // THEN: waiting until the expiry interval should emit a SubjectDeleted event
                final Duration between =
                        Duration.between(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
                                expectedRoundedExpiryInstant);
                final long secondsToWaitForSubjectDeletedEvent = between.getSeconds() + 2;
                final DistributedPubSubMediator.Publish policySubjectDeleted =
                        pubSubMediatorTestProbe.expectMsgClass(
                                FiniteDuration.apply(secondsToWaitForSubjectDeletedEvent, TimeUnit.SECONDS),
                                DistributedPubSubMediator.Publish.class);
                final Object subjectDeletedMsg = policySubjectDeleted.msg();
                assertThat(subjectDeletedMsg).isInstanceOf(SubjectDeleted.class);
                assertThat(((SubjectDeleted) subjectDeletedMsg).getSubjectId())
                        .isEqualTo(expectedAdjustedSubjectToAdd.getId());

                // THEN: a PolicyTag should be emitted via pub/sub indicating that the policy enforcer caches should be invalidated
                final DistributedPubSubMediator.Publish policyTagForCacheInvalidation =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                final Object policyTagForCacheInvalidationMsg = policyTagForCacheInvalidation.msg();
                assertThat(policyTagForCacheInvalidationMsg).isInstanceOf(PolicyTag.class);
                assertThat(((PolicyTag) policyTagForCacheInvalidationMsg).getEntityId())
                        .isEqualTo(policyId);

                // THEN: retrieving the expired subject should fail
                final RetrieveSubject retrieveSubject =
                        RetrieveSubject.of(policyId, POLICY_LABEL, expiringSubject.getId(), dittoHeadersV2);
                underTestRecovered.tell(retrieveSubject, getRef());
                expectMsgClass(SubjectNotAccessibleException.class);
            }
        };
    }

    @Test
    public void ensureExpiredSubjectIsRemovedDuringRecovery() throws InterruptedException {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final PolicyId policyId = policy.getEntityId().orElseThrow();
                final ClusterShardingSettings shardingSettings =
                        ClusterShardingSettings.apply(actorSystem).withRole("policies");
                final Props props =
                        PolicyPersistenceActor.props(policyId, new PolicyMongoSnapshotAdapter(), pubSubMediator);
                final Cluster cluster = Cluster.get(actorSystem);
                cluster.join(cluster.selfAddress());
                final ActorRef underTest = ClusterSharding.get(actorSystem)
                        .start(PoliciesMessagingConstants.SHARD_REGION, props, shardingSettings,
                                ShardRegionExtractor.of(30, actorSystem));

                final Instant expiryInstant = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        .plus(2, ChronoUnit.SECONDS)
                        .atZone(ZoneId.systemDefault()).toInstant();
                final SubjectExpiry subjectExpiry = SubjectExpiry.newInstance(expiryInstant);
                final Subject expiringSubject =
                        Subject.newInstance(SubjectId.newInstance(SubjectIssuer.GOOGLE, "about-to-expire"),
                                SubjectType.GENERATED, subjectExpiry);
                final Policy policyWithExpiringSubject = policy.toBuilder()
                        .setSubjectFor(TestConstants.Policy.SUPPORT_LABEL, expiringSubject)
                        .build();

                final long secondsToAdd = 10 - (expiryInstant.getEpochSecond() % 10);
                final Instant expectedRoundedExpiryInstant = (secondsToAdd == 10) ? expiryInstant :
                        expiryInstant.plusSeconds(secondsToAdd); // to next 10s rounded up
                final SubjectExpiry expectedSubjectExpiry = SubjectExpiry.newInstance(expectedRoundedExpiryInstant);
                final Subject expectedAdjustedSubjectToAdd = Subject.newInstance(expiringSubject.getId(),
                        expiringSubject.getType(), expectedSubjectExpiry);

                final Policy adjustedPolicyWithExpiringSubject = policyWithExpiringSubject.toBuilder()
                        .setSubjectFor(TestConstants.Policy.SUPPORT_LABEL, expectedAdjustedSubjectToAdd)
                        .build();

                // WHEN: a new Policy is created containing an expiring subject
                final CreatePolicy createPolicyCommand = CreatePolicy.of(policyWithExpiringSubject, dittoHeadersV2);
                underTest.tell(createPolicyCommand, getRef());

                // THEN: the response should contain the adjusted (rounded up) expiry time
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                final ActorRef firstPersistenceActor = getLastSender();
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().orElseThrow())
                        .isEqualEqualToButModified(adjustedPolicyWithExpiringSubject);

                // THEN: the created event should be emitted
                final DistributedPubSubMediator.Publish policyCreatedPublishSecond =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                assertThat(policyCreatedPublishSecond.msg()).isInstanceOf(PolicyCreated.class);

                // WHEN: now the persistence actor is terminated
                firstPersistenceActor.tell(PoisonPill.getInstance(), ActorRef.noSender());

                // WHEN: it is waited until the subject expired
                final Duration between =
                        Duration.between(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
                                expectedRoundedExpiryInstant);
                TimeUnit.MILLISECONDS.sleep(between.toMillis() + 200L);

                // AND WHEN: the policy is retrieved via concierge (and restored as a consequence)
                final SudoRetrievePolicy sudoRetrievePolicy = SudoRetrievePolicy.of(policyId, dittoHeadersV2);
                underTest.tell(sudoRetrievePolicy, getRef());
                final SudoRetrievePolicyResponse sudoRetrievePolicyResponse =
                        expectMsgClass(SudoRetrievePolicyResponse.class);
                final ActorRef secondPersistenceActor = getLastSender();

                // THEN: the restored policy persistence actor has a different reference and the same actor path
                assertThat(secondPersistenceActor).isNotEqualTo(firstPersistenceActor);
                assertThat(secondPersistenceActor.path()).isEqualTo(firstPersistenceActor.path());

                // THEN: returned policy via SudoRetrievePolicyResponse does no longer contain the already expired subject:
                final Policy expectedPolicyWithoutExpiredSubject = incrementRevision(policy.toBuilder()
                        .removeSubjectFor(TestConstants.Policy.SUPPORT_LABEL, expiringSubject.getId())
                        .build(), 2);
                assertThat(sudoRetrievePolicyResponse.getPolicy().getRevision())
                        .isEqualTo(expectedPolicyWithoutExpiredSubject.getRevision());
                assertThat(sudoRetrievePolicyResponse.getPolicy().getEntriesSet())
                        .isEqualTo(expectedPolicyWithoutExpiredSubject.getEntriesSet());

                // THEN: waiting until the expiry interval should emit a SubjectDeleted event
                final DistributedPubSubMediator.Publish policySubjectDeleted =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                final Object subjectDeletedMsg = policySubjectDeleted.msg();
                assertThat(subjectDeletedMsg).isInstanceOf(SubjectDeleted.class);
                assertThat(((SubjectDeleted) subjectDeletedMsg).getSubjectId())
                        .isEqualTo(expectedAdjustedSubjectToAdd.getId());

                // THEN: a PolicyTag should be emitted via pub/sub indicating that the policy enforcer caches should be invalidated
                final DistributedPubSubMediator.Publish policyTagForCacheInvalidation =
                        pubSubMediatorTestProbe.expectMsgClass(DistributedPubSubMediator.Publish.class);
                final Object policyTagForCacheInvalidationMsg = policyTagForCacheInvalidation.msg();
                assertThat(policyTagForCacheInvalidationMsg).isInstanceOf(PolicyTag.class);
                assertThat(((PolicyTag) policyTagForCacheInvalidationMsg).getEntityId())
                        .isEqualTo(policyId);

                // THEN: retrieving the expired subject should fail
                final RetrieveSubject retrieveSubject =
                        RetrieveSubject.of(policyId, POLICY_LABEL, expiringSubject.getId(), dittoHeadersV2);
                underTest.tell(retrieveSubject, getRef());
                expectMsgClass(SubjectNotAccessibleException.class);
            }
        };
    }

    @Test
    public void ensureSequenceNumberCorrectness() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                // create the policy - results in sequence number 1
                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                // modify the policy's entries - results in sequence number 2
                final DeletePolicyEntry deletePolicyEntry =
                        DeletePolicyEntry.of(policy.getEntityId().orElse(null), ANOTHER_POLICY_LABEL, dittoHeadersV2);
                policyPersistenceActor.tell(deletePolicyEntry, getRef());
                expectMsgEquals(DeletePolicyEntryResponse.of(policy.getEntityId().orElse(null), ANOTHER_POLICY_LABEL,
                        dittoHeadersV2));

                // retrieve the policy's sequence number
                final long versionExpected = 2;
                final Policy policyExpected = PoliciesModelFactory.newPolicyBuilder(policy) //
                        .remove(ANOTHER_POLICY_LABEL) //
                        .setRevision(versionExpected) //
                        .build();
                final RetrievePolicy retrievePolicy =
                        RetrievePolicy.of(policy.getEntityId().orElse(null), dittoHeadersV2);
                policyPersistenceActor.tell(retrievePolicy, getRef());
                expectMsgEquals(retrievePolicyResponse(policyExpected, retrievePolicy.getDittoHeaders()));
            }
        };
    }

    @Test
    public void ensureSequenceNumberCorrectnessAfterRecovery() {
        new TestKit(actorSystem) {
            {
                final Policy policy = createPolicyWithRandomId();
                final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

                // create the policy - results in sequence number 1
                final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
                policyPersistenceActor.tell(createPolicyCommand, getRef());
                final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
                DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                        .isEqualEqualToButModified(policy);

                // modify the policy's entries - results in sequence number 2
                final DeletePolicyEntry deletePolicyEntry =
                        DeletePolicyEntry.of(policy.getEntityId().orElse(null), ANOTHER_POLICY_LABEL, dittoHeadersV2);
                policyPersistenceActor.tell(deletePolicyEntry, getRef());
                expectMsgEquals(DeletePolicyEntryResponse.of(policy.getEntityId().orElse(null), ANOTHER_POLICY_LABEL,
                        dittoHeadersV2));

                // retrieve the policy's sequence number from recovered actor
                final long versionExpected = 2;
                final Policy policyExpected = PoliciesModelFactory.newPolicyBuilder(policy) //
                        .remove(ANOTHER_POLICY_LABEL) //
                        .setRevision(versionExpected) //
                        .build();

                // restart
                terminate(this, policyPersistenceActor);
                final ActorRef policyPersistenceActorRecovered = createPersistenceActorFor(this, policy);
                final RetrievePolicy retrievePolicy =
                        RetrievePolicy.of(policy.getEntityId().orElse(null), dittoHeadersV2);

                final RetrievePolicyResponse expectedResponse =
                        retrievePolicyResponse(policyExpected, retrievePolicy.getDittoHeaders());

                Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                    policyPersistenceActorRecovered.tell(retrievePolicy, getRef());
                    expectMsgEquals(expectedResponse);
                });

                assertThat(getLastSender()).isEqualTo(policyPersistenceActorRecovered);
            }
        };
    }

    @Test
    public void testPolicyPersistenceActorRespondsToCleanupCommandInCreatedState() {
        new TestKit(actorSystem) {{
            final Policy policy = createPolicyWithRandomId();
            final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

            final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
            policyPersistenceActor.tell(createPolicyCommand, getRef());
            final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
            DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                    .isEqualEqualToButModified(policy);

            final EntityId entityId = DefaultEntityId.of(PolicyPersistenceActor.PERSISTENCE_ID_PREFIX +
                    policy.getEntityId().orElseThrow(IllegalStateException::new));
            policyPersistenceActor.tell(CleanupPersistence.of(entityId, DittoHeaders.empty()), getRef());
            expectMsg(CleanupPersistenceResponse.success(entityId, DittoHeaders.empty()));
        }};
    }

    @Test
    public void testPolicyPersistenceActorRespondsToCleanupCommandInDeletedState() {
        new TestKit(actorSystem) {{
            final Policy policy = createPolicyWithRandomId();
            final ActorRef policyPersistenceActor = createPersistenceActorFor(this, policy);

            final CreatePolicy createPolicyCommand = CreatePolicy.of(policy, dittoHeadersV2);
            policyPersistenceActor.tell(createPolicyCommand, getRef());
            final CreatePolicyResponse createPolicy1Response = expectMsgClass(CreatePolicyResponse.class);
            DittoPolicyAssertions.assertThat(createPolicy1Response.getPolicyCreated().get())
                    .isEqualEqualToButModified(policy);

            final DeletePolicy deletePolicyCommand =
                    DeletePolicy.of(policy.getId().orElseThrow(() -> new IllegalStateException("no id")),
                            dittoHeadersV2);
            policyPersistenceActor.tell(deletePolicyCommand, getRef());
            expectMsgClass(DeletePolicyResponse.class);

            final EntityId entityId = DefaultEntityId.of(PolicyPersistenceActor.PERSISTENCE_ID_PREFIX +
                    policy.getEntityId().orElseThrow(IllegalStateException::new));
            policyPersistenceActor.tell(CleanupPersistence.of(entityId, DittoHeaders.empty()), getRef());
            expectMsg(CleanupPersistenceResponse.success(entityId, DittoHeaders.empty()));
        }};
    }

    @Test
    public void checkForActivityOfNonexistentPolicy() {
        new TestKit(actorSystem) {
            {
                // GIVEN: a PolicyPersistenceActor is created in a parent that forwards all messages to us
                final PolicyId policyId = PolicyId.of("test.ns", "nonexistent.policy");
                final Props persistentActorProps =
                        PolicyPersistenceActor.props(policyId, new PolicyMongoSnapshotAdapter(), pubSubMediator);

                final TestProbe errorsProbe = TestProbe.apply(actorSystem);

                final Props parentProps = Props.create(Actor.class, () -> new AbstractActor() {

                    @Override
                    public void preStart() {
                        getContext().actorOf(persistentActorProps);
                    }

                    @Override
                    public SupervisorStrategy supervisorStrategy() {
                        return new OneForOneStrategy(true,
                                DeciderBuilder.matchAny(throwable -> {
                                    errorsProbe.ref().tell(throwable, getSelf());
                                    return (SupervisorStrategy.Directive) SupervisorStrategy.restart();
                                }).build());
                    }

                    @Override
                    public Receive createReceive() {
                        return ReceiveBuilder.create()
                                .matchAny(message -> {
                                    if (getTestActor().equals(getSender())) {
                                        getContext().actorSelection(getSelf().path().child("*"))
                                                .forward(message, getContext());
                                    } else {
                                        getTestActor().forward(message, getContext());
                                    }
                                })
                                .build();
                    }
                });

                // WHEN: CheckForActivity is sent to a persistence actor of nonexistent policy after startup
                final ActorRef underTest = actorSystem.actorOf(parentProps);

                final Object checkForActivity = AbstractShardedPersistenceActor.checkForActivity(1L);
                underTest.tell(checkForActivity, getRef());
                underTest.tell(checkForActivity, getRef());
                underTest.tell(checkForActivity, getRef());

                // THEN: persistence actor requests shutdown
                expectMsg(PolicySupervisorActor.Control.PASSIVATE);

                // THEN: persistence actor should not throw anything.
                errorsProbe.expectNoMessage(scala.concurrent.duration.Duration.create(3, TimeUnit.SECONDS));
            }
        };
    }

    private ActorRef createPersistenceActorFor(final TestKit testKit, final Policy policy) {
        return createPersistenceActorFor(testKit, policy.getEntityId().orElseThrow(NoSuchElementException::new));
    }

    private ActorRef createPersistenceActorFor(final TestKit testKit, final PolicyId policyId) {
        final SnapshotAdapter<Policy> snapshotAdapter = new PolicyMongoSnapshotAdapter();
        final Props props = PolicyPersistenceActor.props(policyId, snapshotAdapter, pubSubMediator);
        return testKit.watch(actorSystem.actorOf(props));
    }

    private static Policy incrementRevision(final Policy policy, final int n) {
        Policy withIncrementedRevision = policy;
        for (int i = 0; i < n; i++) {
            withIncrementedRevision = withIncrementedRevision.toBuilder()
                    .setRevision(withIncrementedRevision.getRevision()
                            .map(Revision::increment)
                            .orElseGet(() -> PolicyRevision.newInstance(1)))
                    .build();
        }
        return withIncrementedRevision;
    }

    private static void terminate(final TestKit testKit, final ActorRef actor) {
        actor.tell(PoisonPill.getInstance(), ActorRef.noSender());
        testKit.expectTerminated(actor);
    }

}
