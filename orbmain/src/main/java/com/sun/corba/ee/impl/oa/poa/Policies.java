/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package com.sun.corba.ee.impl.oa.poa;

import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.extension.ServantCachingPolicy;
import com.sun.corba.ee.spi.extension.ZeroPortPolicy;
import com.sun.corba.ee.spi.extension.CopyObjectPolicy;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

import org.omg.CORBA.Policy;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID;
import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.ThreadPolicyValue;

@ManagedData
@Description("A collection of Policy instances")
public final class Policies {
    /*
     * Order of *POLICY_ID : THREAD_ LIFESPAN_ ID_UNIQUENESS_ ID_ASSIGNMENT_ IMPLICIT_ACTIVATION_ SERvANT_RETENTION_
     * REQUEST_PROCESSING_ The code in this class depends on this order!
     */
    private static final int MIN_POA_POLICY_ID = THREAD_POLICY_ID.value;
    private static final int MAX_POA_POLICY_ID = REQUEST_PROCESSING_POLICY_ID.value;
    private static final int POLICY_TABLE_SIZE = MAX_POA_POLICY_ID - MIN_POA_POLICY_ID + 1;

    int defaultObjectCopierFactoryId;

    private Map<Integer, Policy> policyMap = new HashMap<Integer, Policy>();

    @ManagedAttribute
    @Description("The policies")
    Map<Integer, Policy> getPolicies() {
        return new HashMap<Integer, Policy>(policyMap);
    }

    public static final Policies defaultPolicies = new Policies();

    public static final Policies rootPOAPolicies = new Policies(ThreadPolicyValue._ORB_CTRL_MODEL, LifespanPolicyValue._TRANSIENT,
            IdUniquenessPolicyValue._UNIQUE_ID, IdAssignmentPolicyValue._SYSTEM_ID, ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION,
            ServantRetentionPolicyValue._RETAIN, RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY);

    private int[] poaPolicyValues;

    private int getPolicyValue(int id) {
        return poaPolicyValues[id - MIN_POA_POLICY_ID];
    }

    private void setPolicyValue(int id, int value) {
        poaPolicyValues[id - MIN_POA_POLICY_ID] = value;
    }

    private Policies(int threadModel, int lifespan, int idUniqueness, int idAssignment, int implicitActivation, int retention,
            int requestProcessing) {
        poaPolicyValues = new int[] { threadModel, lifespan, idUniqueness, idAssignment, implicitActivation, retention, requestProcessing };
    }

    private Policies() {
        this(ThreadPolicyValue._ORB_CTRL_MODEL, LifespanPolicyValue._TRANSIENT, IdUniquenessPolicyValue._UNIQUE_ID,
                IdAssignmentPolicyValue._SYSTEM_ID, ImplicitActivationPolicyValue._NO_IMPLICIT_ACTIVATION,
                ServantRetentionPolicyValue._RETAIN, RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Policies[");
        boolean first = true;
        for (Policy p : policyMap.values()) {
            if (first)
                first = false;
            else
                buffer.append(",");

            buffer.append(p.toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    /*
     * Returns the integer value of the POA policy, if this is a POA policy, otherwise returns -1.
     */
    private int getPOAPolicyValue(Policy policy) {
        if (policy instanceof ThreadPolicy) {
            return ((ThreadPolicy) policy).value().value();
        } else if (policy instanceof LifespanPolicy) {
            return ((LifespanPolicy) policy).value().value();
        } else if (policy instanceof IdUniquenessPolicy) {
            return ((IdUniquenessPolicy) policy).value().value();
        } else if (policy instanceof IdAssignmentPolicy) {
            return ((IdAssignmentPolicy) policy).value().value();
        } else if (policy instanceof ServantRetentionPolicy) {
            return ((ServantRetentionPolicy) policy).value().value();
        } else if (policy instanceof RequestProcessingPolicy) {
            return ((RequestProcessingPolicy) policy).value().value();
        } else if (policy instanceof ImplicitActivationPolicy) {
            return ((ImplicitActivationPolicy) policy).value().value();
        } else
            return -1;
    }

    /**
     * If any errors were found, throw INVALID_POLICY with the smallest index of any offending policy.
     */
    private void checkForPolicyError(BitSet errorSet) throws InvalidPolicy {
        for (short ctr = 0; ctr < errorSet.length(); ctr++)
            if (errorSet.get(ctr))
                throw new InvalidPolicy(ctr);
    }

    /**
     * Add the first index in policies at which the policy is of type policyId to errorSet, if the polictId is in policies
     * (it may not be).
     */
    private void addToErrorSet(Policy[] policies, int policyId, BitSet errorSet) {
        for (int ctr = 0; ctr < policies.length; ctr++)
            if (policies[ctr].policy_type() == policyId) {
                errorSet.set(ctr);
                return;
            }
    }

    /**
     * Main constructor used from POA::create_POA. This need only be visible within the POA package.
     */
    Policies(Policy[] policies, int id) throws InvalidPolicy {
        // Make sure the defaults are set according to the POA spec
        this();

        defaultObjectCopierFactoryId = id;

        if (policies == null)
            return;

        // Set to record all indices in policies for which errors
        // were observed.
        BitSet errorSet = new BitSet(policies.length);

        for (short i = 0; i < policies.length; i++) {
            Policy policy = policies[i];
            int POAPolicyValue = getPOAPolicyValue(policy);

            // Save the policy in policyMap to support
            // POA.get_effective_policy, if it was not already saved
            // in policyMap.
            int key = policy.policy_type();
            Policy prev = policyMap.get(key);
            if (prev == null)
                policyMap.put(key, policy);

            if (POAPolicyValue >= 0) {
                setPolicyValue(key, POAPolicyValue);

                // if the value of this POA policy was previously set to a
                // different value than the current value given in
                // POAPolicyValue, record an error.
                if ((prev != null) && (getPOAPolicyValue(prev) != POAPolicyValue))
                    errorSet.set(i);
            }
        }

        // Check for bad policy combinations

        // NON_RETAIN requires USE_DEFAULT_SERVANT or USE_SERVANT_MANAGER
        if (!retainServants() && useActiveMapOnly()) {
            addToErrorSet(policies, SERVANT_RETENTION_POLICY_ID.value, errorSet);
            addToErrorSet(policies, REQUEST_PROCESSING_POLICY_ID.value, errorSet);
        }

        // IMPLICIT_ACTIVATION requires SYSTEM_ID and RETAIN
        if (isImplicitlyActivated()) {
            if (!retainServants()) {
                addToErrorSet(policies, IMPLICIT_ACTIVATION_POLICY_ID.value, errorSet);
                addToErrorSet(policies, SERVANT_RETENTION_POLICY_ID.value, errorSet);
            }

            if (!isSystemAssignedIds()) {
                addToErrorSet(policies, IMPLICIT_ACTIVATION_POLICY_ID.value, errorSet);
                addToErrorSet(policies, ID_ASSIGNMENT_POLICY_ID.value, errorSet);
            }
        }

        checkForPolicyError(errorSet);
    }

    public Policy get_effective_policy(int type) {
        return policyMap.get(type);
    }

    /* Thread Policies */
    public final boolean isOrbControlledThreads() {
        return getPolicyValue(THREAD_POLICY_ID.value) == ThreadPolicyValue._ORB_CTRL_MODEL;
    }

    public final boolean isSingleThreaded() {
        return getPolicyValue(THREAD_POLICY_ID.value) == ThreadPolicyValue._SINGLE_THREAD_MODEL;
    }

    /* Lifespan */
    public final boolean isTransient() {
        return getPolicyValue(LIFESPAN_POLICY_ID.value) == LifespanPolicyValue._TRANSIENT;
    }

    public final boolean isPersistent() {
        return getPolicyValue(LIFESPAN_POLICY_ID.value) == LifespanPolicyValue._PERSISTENT;
    }

    /* ID Uniqueness */
    public final boolean isUniqueIds() {
        return getPolicyValue(ID_UNIQUENESS_POLICY_ID.value) == IdUniquenessPolicyValue._UNIQUE_ID;
    }

    public final boolean isMultipleIds() {
        return getPolicyValue(ID_UNIQUENESS_POLICY_ID.value) == IdUniquenessPolicyValue._MULTIPLE_ID;
    }

    /* ID Assignment */
    public final boolean isUserAssignedIds() {
        return getPolicyValue(ID_ASSIGNMENT_POLICY_ID.value) == IdAssignmentPolicyValue._USER_ID;
    }

    public final boolean isSystemAssignedIds() {
        return getPolicyValue(ID_ASSIGNMENT_POLICY_ID.value) == IdAssignmentPolicyValue._SYSTEM_ID;
    }

    /* Servant Rentention */
    public final boolean retainServants() {
        return getPolicyValue(SERVANT_RETENTION_POLICY_ID.value) == ServantRetentionPolicyValue._RETAIN;
    }

    /* Request Processing */
    public final boolean useActiveMapOnly() {
        return getPolicyValue(REQUEST_PROCESSING_POLICY_ID.value) == RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY;
    }

    public final boolean useDefaultServant() {
        return getPolicyValue(REQUEST_PROCESSING_POLICY_ID.value) == RequestProcessingPolicyValue._USE_DEFAULT_SERVANT;
    }

    public final boolean useServantManager() {
        return getPolicyValue(REQUEST_PROCESSING_POLICY_ID.value) == RequestProcessingPolicyValue._USE_SERVANT_MANAGER;
    }

    /* Implicit Activation */
    public final boolean isImplicitlyActivated() {
        return getPolicyValue(IMPLICIT_ACTIVATION_POLICY_ID.value) == ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION;
    }

    /* proprietary servant caching policy */
    public final int servantCachingLevel() {
        ServantCachingPolicy policy = ServantCachingPolicy.class.cast(policyMap.get(ORBConstants.SERVANT_CACHING_POLICY));

        if (policy == null)
            return ServantCachingPolicy.NO_SERVANT_CACHING;
        else
            return policy.getType();
    }

    public final boolean forceZeroPort() {
        ZeroPortPolicy policy = ZeroPortPolicy.class.cast(policyMap.get(ORBConstants.ZERO_PORT_POLICY));

        if (policy == null)
            return false;
        else
            return policy.forceZeroPort();
    }

    public final int getCopierId() {
        CopyObjectPolicy policy = CopyObjectPolicy.class.cast(policyMap.get(ORBConstants.COPY_OBJECT_POLICY));

        if (policy != null)
            return policy.getValue();
        else
            return defaultObjectCopierFactoryId;
    }
}
