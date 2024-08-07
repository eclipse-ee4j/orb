/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.oa.rfm;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import org.omg.CORBA.Policy;
import org.omg.CORBA.LocalObject;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.AdapterActivator;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;

import com.sun.corba.ee.spi.logging.POASystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.trace.Poa;
import java.util.HashSet;
import java.util.Set;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.pfl.basic.contain.Pair;

@Poa
@ManagedObject
@Description("The ReferenceFactoryManager, used to handle dynamic cluster membership updates")
public class ReferenceFactoryManagerImpl extends org.omg.CORBA.LocalObject implements ReferenceFactoryManager {
    private static final POASystemException wrapper = POASystemException.self;

    private static final long serialVersionUID = -6689846523143143228L;

    private static final String PARENT_POA_NAME = "#RFMBase#";

    // Initialized in the constructor
    private RFMState state;
    private final ReentrantLock lock;
    private final Condition suspendCondition;
    private final ORB orb;
    // poatable contains the mapping from the ReferenceFactory name to
    // the ServantLocator and list of policies. Note that the policy
    // list is stored in the form passed to the create() call: that is,
    // it does not contain the standard policies.
    private final Map<String, Pair<ServantLocator, List<Policy>>> poatable;
    private final Map<String, ReferenceFactory> factories;
    private final Set<POAManager> managers;
    private final AdapterActivator activator;
    private volatile boolean isActive;

    // Initialized on activation because the root POA is required.
    private POA rootPOA;
    private List<Policy> standardPolicies;
    private POA parentPOA;
    private String[] parentPOAAdapterName;

    public ReferenceFactoryManagerImpl(ORB orb) {
        lock = new ReentrantLock();
        suspendCondition = lock.newCondition();
        state = RFMState.READY;
        this.orb = orb;
        poatable = new HashMap<String, Pair<ServantLocator, List<Policy>>>();
        factories = new HashMap<String, ReferenceFactory>();
        managers = new HashSet<POAManager>();
        activator = new AdapterActivatorImpl();
        isActive = false;
    }

    @Poa
    private class AdapterActivatorImpl extends LocalObject implements AdapterActivator {
        private static final long serialVersionUID = 7922226881290146012L;

        @Poa
        public boolean unknown_adapter(POA parent, String name) {
            Pair<ServantLocator, List<Policy>> data = null;
            synchronized (poatable) {
                // REVISIT: make sure that data can't change concurrently!
                // Should be OK because Pair is immutable.
                data = poatable.get(name);
            }

            if (data == null) {
                return false;
            } else {
                try {
                    List<Policy> policies = new ArrayList<Policy>();
                    // XXX What should we do if data.second() contains
                    // policies with the same ID as standard policies?
                    if (data.second() != null) {
                        policies.addAll(data.second());
                    }
                    policies.addAll(standardPolicies);
                    Policy[] arr = policies.toArray(new Policy[policies.size()]);

                    POA child = parentPOA.create_POA(name, null, arr);
                    POAManager pm = child.the_POAManager();

                    lock.lock();
                    try {
                        managers.add(pm);
                    } finally {
                        lock.unlock();
                    }

                    child.set_servant_manager(data.first());
                    pm.activate();
                    return true;
                } catch (Exception exc) {
                    wrapper.rfmAdapterActivatorFailed(exc);
                    return false;
                }
            }
        }
    };

    // Policy used to indicate that a POA may particpate in the reference manager.
    // If this policy is not present, and a create_POA call is made under base POA,
    // an IORInterceptor will be used reject the POA creation.
    private static class ReferenceManagerPolicy extends LocalObject implements Policy {
        private static Policy thisPolicy = new ReferenceManagerPolicy();
        private static final long serialVersionUID = -4780983694679451387L;

        public static Policy getPolicy() {
            return thisPolicy;
        }

        private ReferenceManagerPolicy() {
        }

        public int policy_type() {
            return ORBConstants.REFERENCE_MANAGER_POLICY;
        }

        public Policy copy() {
            return this;
        }

        public void destroy() {
        }
    }

    public RFMState getState() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    @Poa
    public void activate() {
        lock.lock();
        try {
            if (isActive) {
                throw wrapper.rfmAlreadyActive();
            }

            rootPOA = (POA) orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME);

            standardPolicies = Arrays.asList(ReferenceManagerPolicy.getPolicy(),
                    rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
                    rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                    rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT));

            Policy[] policies = { ReferenceManagerPolicy.getPolicy() };
            parentPOA = rootPOA.create_POA(PARENT_POA_NAME, null, policies);
            parentPOAAdapterName = ObjectAdapter.class.cast(parentPOA).getIORTemplate().getObjectKeyTemplate().getObjectAdapterId()
                    .getAdapterName();

            POAManager pm = parentPOA.the_POAManager();
            parentPOA.the_activator(activator);
            pm.activate();

            // Don't activate if there is a failure
            isActive = true;
        } catch (Exception exc) {
            throw wrapper.rfmActivateFailed(exc);
        } finally {
            lock.unlock();
        }
    }

    // XXX rfmMightDeadlock exceptions are a problem, because it is possible
    // to attempt to deploy an EJB while the cluster shape is changing.
    // We really need to enqueue (at least) create calls while suspended.
    // It may also be better to get rid of separate suspend/resume calls, instead
    // passing an object to a method that does suspend/resume (as in
    // doPrivileged). See GF issue 4560.
    @Poa
    public ReferenceFactory create(final String name, final String repositoryId, final List<Policy> policies,
            final ServantLocator locator) {
        lock.lock();
        try {
            if (state == RFMState.SUSPENDED) {
                throw wrapper.rfmMightDeadlock();
            }

            if (!isActive) {
                throw wrapper.rfmNotActive();
            }

            List<Policy> newPolicies = null;
            if (policies != null) {
                newPolicies = new ArrayList<Policy>(policies);
            }

            // Store an entry for the appropriate POA in the POA table,
            // which is used by the AdapterActivator on the root.
            synchronized (poatable) {
                poatable.put(name, new Pair<ServantLocator, List<Policy>>(locator, newPolicies));
            }

            ReferenceFactory factory = new ReferenceFactoryImpl(this, name, repositoryId);
            factories.put(name, factory);
            return factory;
        } finally {
            lock.unlock();
        }
    }

    @Poa
    public ReferenceFactory find(String[] adapterName) {
        lock.lock();
        try {
            if (state == RFMState.SUSPENDED) {
                throw wrapper.rfmMightDeadlock();
            }

            if (!isActive) {
                return null;
            }

            int expectedLength = parentPOAAdapterName.length + 1;

            if (expectedLength != adapterName.length) {
                return null;
            }

            for (int ctr = 0; ctr < expectedLength - 1; ctr++) {
                if (!adapterName[ctr].equals(parentPOAAdapterName[ctr])) {
                    return null;
                }
            }

            return factories.get(adapterName[expectedLength - 1]);
        } finally {
            lock.unlock();
        }
    }

    public ReferenceFactory find(String name) {
        lock.lock();
        try {
            if (state == RFMState.SUSPENDED) {
                throw wrapper.rfmMightDeadlock();
            }

            if (!isActive) {
                return null;
            }

            return factories.get(name);
        } finally {
            lock.unlock();
        }
    }

    // We need to prevent new requests from being
    // processed while we reconfigure the POAs
    // in the RFM. This could be done either by
    // hold_requests or discard_requests. Hold will
    // cause incoming requests to be suspended, which
    // could rapidly consume all of the threads in the
    // threadpool. It may be preferable to discard the
    // requests, which would cause a TRANSIENT system
    // exception to be sent to the client. All
    // TRANSIENT system exceptions will be retried
    // on the client side to the same endpoint.
    // But at one time, the client retry logic was not
    // robust enough to handle the resulting TRANSIENT
    // exceptions. The retry logic has since been fixed.
    //
    // XXX We may still want to switch to discard semantics,
    // but that would require significant testing.

    @Poa
    public void suspend() {
        lock.lock();

        // Clone managers so we can safely iterator over it.
        final Set<POAManager> pms = new HashSet<POAManager>(managers);

        // wait until all requests in the manager have completed.
        try {
            if (!isActive) {
                throw wrapper.rfmNotActive();
            }

            while (state == RFMState.SUSPENDED) {
                try {
                    suspendCondition.await();
                } catch (InterruptedException exc) {
                    throw wrapper.rfmSuspendConditionWaitInterrupted();
                }
            }

            // At this point, the state must be READY, and any other
            // suspending thread has released the lock. So now
            // we set the state back to SUSPENDED, drop the lock,
            // and continue.

            state = RFMState.SUSPENDED;
        } finally {
            lock.unlock();
        }

        // do NOT hold the RFM lock here, because then we would hold
        // first the RFM, and then the POAManager lock. Another thread
        // could reverse the order, leading to a deadlock. See bug
        // 6586417.
        try {
            for (POAManager pm : pms) {
                pm.hold_requests(true);
            }
        } catch (AdapterInactive ai) {
            // This should never happen
            throw wrapper.rfmManagerInactive(ai);
        }
    }

    @Poa
    public void resume() {
        lock.lock();

        // Clone managers so we can safely iterator over it.
        final Set<POAManager> pms = new HashSet<POAManager>(managers);

        try {
            if (!isActive) {
                throw wrapper.rfmNotActive();
            }

            state = RFMState.READY;
            suspendCondition.signalAll();
        } finally {
            lock.unlock();
        }

        // Allow new requests to start. This will lazily
        // re-create POAs as needed through the parentPOA's
        // AdapterActivator.
        // 6933574. But note that these POAManagers belong to
        // destroyed POAs (from the restartFactories call),
        // so the activation here results in
        // an PADestrpyed exception, which causes a retry in
        // the server request dispatcher. Re-creating the
        // POA also re-creates the POAManager, so after
        // activating the POAmanagers, we remove the (old)
        // POAManagers from the list. Note that the list
        // may (probably will) have changed by then, since other
        // threads may (probably will) be re-creating the POAs and hence
        // re-creates the POAmanagers and adding the new POAmanagers
        // to managers.
        try {
            for (POAManager pm : pms) {
                pm.activate();
            }
        } catch (AdapterInactive ai) {
            // This should never happen
            throw wrapper.rfmManagerInactive(ai);
        }

        lock.lock();
        try {
            // 6933574: As noted above, we need to remove all
            // of the old POAManagers (to avoid a memory leak).
            // Note that managers may contain new POAManagers by
            // this time, so we can't just call managers.clear().
            managers.removeAll(pms);
        } finally {
            lock.unlock();
        }
    }

    @Poa
    public void restartFactories(Map<String, Pair<ServantLocator, List<Policy>>> updates) {
        lock.lock();
        try {
            if (!isActive) {
                throw wrapper.rfmNotActive();
            }

            if (state != RFMState.SUSPENDED) {
                throw wrapper.rfmMethodRequiresSuspendedState("restartFactories");
            }
        } finally {
            lock.unlock();
        }

        if (updates == null) {
            throw wrapper.rfmNullArgRestart();
        }

        synchronized (poatable) {
            // Update the poatable with the updates information.
            poatable.putAll(updates);
        }

        try {
            // Now destroy all POAs that are used to
            // implement ReferenceFactory instances.
            for (POA poa : parentPOA.the_children()) {
                poa.destroy(false, true);
            }
        } catch (Exception exc) {
            throw wrapper.rfmRestartFailed(exc);
        }
    }

    public void restartFactories() {
        restartFactories(new HashMap<String, Pair<ServantLocator, List<Policy>>>());
    }

    /**
     * Restart all ReferenceFactories. This is done safely, so that any request against object references created from these
     * factories complete correctly. Restart does not return until all restart activity completes.
     * 
     * @param updates is a map giving the updated policies for some or all of the ReferenceFactory instances in this
     * ReferenceFactoryManager. This parameter must not be null.
     */
    @Poa
    public void restart(Map<String, Pair<ServantLocator, List<Policy>>> updates) {
        suspend();
        try {
            restartFactories(updates);
        } finally {
            resume();
        }
    }

    /**
     * Restart all ReferenceFactories. This is done safely, so that any request against object references created from these
     * factories complete correctly. Restart does not return until all restart activity completes. Equivalent to calling
     * restart( new Map() ).
     */
    public void restart() {
        restart(new HashMap<String, Pair<ServantLocator, List<Policy>>>());
    }

    // Methods used to implement the ReferenceFactory interface.
    // ReferenceFactoryImpl just delegates to these methods.
    @Poa
    org.omg.CORBA.Object createReference(String name, byte[] key, String repositoryId) {
        try {
            POA child = parentPOA.find_POA(name, true);
            return child.create_reference_with_id(key, repositoryId);
        } catch (Exception exc) {
            throw wrapper.rfmCreateReferenceFailed(exc);
        }
    }

    // Called from ReferenceFactoryImpl.
    @Poa
    void destroy(String name) {
        try {
            POA child = parentPOA.find_POA(name, true);
            synchronized (poatable) {
                poatable.remove(name);
            }

            lock.lock();
            try {
                factories.remove(name);
                POAManager pm = child.the_POAManager();
                managers.remove(pm);
            } finally {
                lock.unlock();
            }

            // Wait for all requests to complete before completing
            // destruction of the POA.
            child.destroy(false, true);
        } catch (Exception exc) {
            throw wrapper.rfmDestroyFailed(exc);
        }
    }

    // Called from ReferenceManagerConfigurator.
    void validatePOACreation(POA poa) {
        // Some POAs are created before the ReferenceFactoryManager is created.
        // In particular, the root POA and parentPOA are created before isActive
        // is set to true. Don't check in these case.
        if (!isActive) {
            return;
        }

        // Only check the case where poa does not have the reference manager
        // policy. We assume that we handle the policy correctly inside
        // the RFM itself.
        Policy policy = ObjectAdapter.class.cast(poa).getEffectivePolicy(ORBConstants.REFERENCE_MANAGER_POLICY);
        if (policy != null) {
            return;
        }

        // At this point, we know that poa comes from outside the
        // active RFM. If poa's parent POA has the policy, we have an
        // error.
        POA parent = poa.the_parent();
        Policy parentPolicy = ObjectAdapter.class.cast(parent).getEffectivePolicy(ORBConstants.REFERENCE_MANAGER_POLICY);
        if (parentPolicy != null) {
            throw wrapper.rfmIllegalParentPoaUsage();
        }

        // If poa's POAManager is the manager for the RFM, we have
        // an error.
        lock.lock();
        try {
            if (managers.contains(poa.the_POAManager())) {
                throw wrapper.rfmIllegalPoaManagerUsage();
            }
        } finally {
            lock.unlock();
        }
    }

    // locking not required
    @Poa
    public boolean isRfmName(String[] adapterName) {
        if (!isActive) {
            return false;
        }

        int expectedLength = parentPOAAdapterName.length + 1;

        if (expectedLength != adapterName.length) {
            return false;
        }

        for (int ctr = 0; ctr < expectedLength - 1; ctr++) {
            if (!adapterName[ctr].equals(parentPOAAdapterName[ctr])) {
                return false;
            }
        }

        return true;
    }
}

// End of file.
