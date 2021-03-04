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
package org.eclipse.ditto.services.models.policies;

import java.util.List;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.PoliciesResourceType;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyBuilder;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.SubjectIssuer;
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.AclEntry;
import org.eclipse.ditto.model.things.Thing;

/**
 * Utilities for migrating Policies and ACLs between {@link org.eclipse.ditto.model.base.json.JsonSchemaVersion#V_1} to
 * {@link org.eclipse.ditto.model.base.json.JsonSchemaVersion#V_2}.
 */
public final class PoliciesAclMigrations {

    /**
     * The prefix for migrated labels.
     */
    public static final String ACL_LABEL_PREFIX = "acl_";

    private static final JsonPointer ROOT_PATH = JsonPointer.empty();

    private PoliciesAclMigrations() {
        throw new AssertionError();
    }

    /**
     * Migrates the passed {@code AccessControlList} into a {@code Policy}.
     *
     * @param accessControlList the ACL to migrate.
     * @param policyId the ID for the migrated Policy.
     * @param subjectIssuers subjectIssuers to generate subjects for
     * @return the Policy.
     */
    public static Policy accessControlListToPolicyEntries(final AccessControlList accessControlList,
            final PolicyId policyId, final List<SubjectIssuer> subjectIssuers) {
        final PolicyBuilder policyBuilder = PoliciesModelFactory.newPolicyBuilder(policyId);
        accessControlList.getEntriesSet().forEach(aclEntry -> {
            final String sid = getSubjectWithoutIssuer(aclEntry);
            final PolicyBuilder.LabelScoped labelScoped = policyBuilder.forLabel(ACL_LABEL_PREFIX + sid);

            subjectIssuers.forEach(
                    subjectIssuer -> labelScoped.setSubject(subjectIssuer, sid));

            if (aclEntry.getPermissions().contains(org.eclipse.ditto.model.things.Permission.READ) &&
                    aclEntry.getPermissions()
                            .contains(org.eclipse.ditto.model.things.Permission.WRITE)) {
                labelScoped.setGrantedPermissions(PoliciesResourceType.policyResource(ROOT_PATH), Permission.READ);
                labelScoped.setGrantedPermissions(PoliciesResourceType.thingResource(ROOT_PATH), Permission.READ,
                        Permission.WRITE);
                labelScoped.setGrantedPermissions(PoliciesResourceType.messageResource(ROOT_PATH), Permission.READ,
                        Permission.WRITE);
            } else if (aclEntry.getPermissions().contains(org.eclipse.ditto.model.things.Permission.READ)) {
                labelScoped.setGrantedPermissions(PoliciesResourceType.policyResource(ROOT_PATH), Permission.READ);
                labelScoped.setGrantedPermissions(PoliciesResourceType.thingResource(ROOT_PATH), Permission.READ);
                labelScoped.setGrantedPermissions(PoliciesResourceType.messageResource(ROOT_PATH), Permission.READ);
            } else if (aclEntry.getPermissions().contains(org.eclipse.ditto.model.things.Permission.WRITE)) {
                labelScoped.setGrantedPermissions(PoliciesResourceType.thingResource(ROOT_PATH), Permission.WRITE);
                labelScoped.setGrantedPermissions(PoliciesResourceType.messageResource(ROOT_PATH), Permission.WRITE);
            }

            if (aclEntry.getPermissions().contains(org.eclipse.ditto.model.things.Permission.ADMINISTRATE)) {
                // allow reading+writing policy:/ if the ACL entry has Administrate permission
                labelScoped.setGrantedPermissions(PoliciesResourceType.policyResource(ROOT_PATH),
                        Permission.READ, Permission.WRITE);
                // allow writing thing:/acl if the ACL entry has Administrate permission
                labelScoped.setGrantedPermissions(PoliciesResourceType.thingResource(Thing.JsonFields.ACL.getPointer()),
                        Permission.READ, Permission.WRITE);
            } else {
                // forbid writing thing:/acl if the ACL entry was missing Administrate permission
                labelScoped.setRevokedPermissions(PoliciesResourceType.thingResource(Thing.JsonFields.ACL.getPointer()),
                        Permission.WRITE);
            }
        });
        return policyBuilder.build();
    }

    private static String getSubjectWithoutIssuer(final AclEntry aclEntry) {
        final String sid = aclEntry.getAuthorizationSubject().getId();
        if (sid.contains(":")) {
            return sid.split(":", 2)[1];
        }
        return sid;
    }
}
