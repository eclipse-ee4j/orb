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

package com.sun.corba.ee.impl.oa.poa;

import java.util.Set;
import java.util.HashSet;


import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.State;

import org.omg.PortableInterceptor.DISCARDING ;
import org.omg.PortableInterceptor.ACTIVE ;
import org.omg.PortableInterceptor.HOLDING ;
import org.omg.PortableInterceptor.INACTIVE ;
import org.omg.PortableInterceptor.NON_EXISTENT ;

import com.sun.corba.ee.spi.protocol.PIHandler ;

import com.sun.corba.ee.spi.logging.POASystemException ;

import com.sun.corba.ee.spi.trace.Poa;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedOperation ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ParameterNames ;
import org.glassfish.gmbal.NameValue ;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.glassfish.pfl.basic.contain.MultiSet ;

/** POAManagerImpl is the implementation of the POAManager interface.
 *  Its public methods are activate(), hold_requests(), discard_requests()
 *  and deactivate().
 */

@Poa
@ManagedObject
@Description( "A POAManager which controls invocations of its POAs")
public class POAManagerImpl extends org.omg.CORBA.LocalObject implements 
    POAManager
{
    private static final POASystemException wrapper =
        POASystemException.self ;

    private static final long serialVersionUID = -3308938242684343402L;

    // final fields: no synchronization needed
    private final POAFactory factory ;  // factory which contains global state 
                                        // for all POAManagers
    private final PIHandler pihandler ; // for AdapterManagerStateChanged
    private final int myId ;            // This POAManager's ID

    // Making this a fair lock due to expected very high numbers of readers
    // with an occasional writer that must NOT be starved.
    //
    // The state and poas fields may be READ while holding the read lock.
    // Updates require holding the write lock.
    private final ReentrantReadWriteLock stateLock =
        new ReentrantReadWriteLock( true ) ;

    // Condition var used for waiting.  Can only wait on this when the
    // write lock is held!
    private final Condition stateCV = stateLock.writeLock().newCondition() ;

    // fields protected by stateLock
    private State state;                // current state of this POAManager

    private Set<POAImpl> poas =
        new HashSet<POAImpl>(4) ;       // all poas controlled by this POAManager

    // fields using other synchronization methods
    private AtomicInteger nInvocations=
        new AtomicInteger(0);           // Number of invocations in progress
    private AtomicInteger nWaiters =
        new AtomicInteger(0) ;          // Number of threads waiting for
                                        // invocations to complete
    private volatile boolean explicitStateChange ; // initially false, set true as soon as
                                        // one of activate, hold_request, 
                                        // discard_request, or deactivate is called.

    /** activeManagers is the set of POAManagerImpls for which a thread has called
     * enter without exit 1 or more times.  Once a thread has entered a POAManager,
     * it must be able to re-enter the POAManager, even if the manager is HOLDING,
     * because state transitions can be deferred until all threads have completed execution
     * and called exit().  Without this change, one thread can be blocked on the
     * state change method, and another thread that has entered the POAManager once
     * can be blocked from re-entry on a nested co-located call.  This leads to a
     * permanent deadlock between the two threads.  See Bug 6586417.
     *
     * To avoid this, we create a set of active managers, and record which managers
     * a particular thread is using.  A thread may re-enter any manager in HOLDING state
     * once it has entered it for the first time.  Note that POAManagerImpl uses the
     * default equals and hashCode methods inherited from Object.  This is fine,
     * because two distinct POAManagerImpl instances always represent distinct
     * POAManagerImpls.
     *
     * This is only a partial solution to the problem, but it should be sufficient for
     * the app server, because all EJBs in the app server share the same POAManager.
     * The problem in general is that state changes between multiple POAManager and
     * invocation threads that make co-located calls to different POAManagers can still
     * deadlock.  This problem requires a different solution, because the hold_requests
     * method may have already returned when the active thread needs to enter the
     * holding POAManager, so we can't just let the thread in.  I think in this case
     * we need to reject the request because it may cause a deadlock.  So, checkState
     * needs to throw a TRANSIENT exception if it finds that the thread is already active
     * in one or more POAManagers, AND it tries to enter a new POAManager.  Such exceptions
     * should be re-tried by the client, and will succeed after
     * the holding POAManagers have been resumed.
     *
     * Another possible route to fix the app server bug (more globally) is to have the RFM
     * suspend method use discard instead of hold.  This may be better in some ways,
     * but early tests with that approach led to some problems (which I can't recall right now).
     * I suspect the issues may have been related to problems with the client-side retry logic,
     * but those problems have now been fixed.  In any case, we need to fix the POAManager
     * issues.
     */
    private static ThreadLocal<MultiSet<POAManagerImpl>> activeManagers =
        new ThreadLocal<MultiSet<POAManagerImpl>>() {
        @Override
            public MultiSet<POAManagerImpl> initialValue() {
                return new MultiSet<POAManagerImpl>() ;
            }
        } ;

    private String stateToString( State state ) {
        switch (state.value()) {
            case State._HOLDING : return "HOLDING" ;
            case State._ACTIVE : return "ACTIVE" ;
            case State._DISCARDING : return "DISCARDING" ;
            case State._INACTIVE : return "INACTIVE" ;
        }

        return "State[UNKNOWN]" ;
    }

    @Override
    public int hashCode()
    {
        return myId ;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj == this) {
            return true ;
        }

        if (!(obj instanceof POAManagerImpl)) {
            return false ;
        }

        POAManagerImpl other = (POAManagerImpl)obj ;

        return other.myId == myId ;
    }

    @Override
    public String toString() {
        stateLock.readLock().lock();
        try {
            return "POAManagerImpl[" + myId +
                "," + stateToString(state) +
                ",nInvocations=" + nInvocations +
                ",nWaiters=" + nWaiters + "]" ;
        } finally {
            stateLock.readLock().unlock();
        }
    }

    @ManagedAttribute
    @Description( "The set of POAs managed by this POAManager" )
    Set<POAImpl> getManagedPOAs() {
        return new HashSet<POAImpl>( poas ) ;
    }

    @ManagedAttribute
    @Description( "Number of active invocations executing in this POAManager" )
    public int numberOfInvocations() {
        return nInvocations.get() ;
    }

    @ManagedAttribute
    @Description( "Number of threads waiting for invocations to complete in this POAManager" )
    public int numberOfWaiters() {
        return nWaiters.get() ;
    }

    @ManagedAttribute
    @Description( "The current state of this POAManager" ) 
    public String displayState() {
        stateLock.readLock().lock();
        try {
            return stateToString( state ) ;
        } finally {
            stateLock.readLock().unlock();
        }
    }

    @ManagedAttribute
    @Description( "The POAFactory that manages this POAManager" )
    POAFactory getFactory()
    {
        return factory ;
    }

    PIHandler getPIHandler()
    {
        return pihandler ;
    }

    @InfoMethod
    private void numWaitersStart( int value ) {}

    @InfoMethod
    private void numWaitersEnd( int value ) {}

    @Poa
    // Note: caller MUST hold write lock
    private void countedWait()
    {
        try {
            int num = nWaiters.incrementAndGet() ;
            numWaitersStart( num ) ;

            // 6878245: I can see some sense in the timeout, but why this value?
            stateCV.await(num*1000L, TimeUnit.MILLISECONDS);
        } catch ( java.lang.InterruptedException ex ) {
            // NOP
        } finally {
            int num = nWaiters.decrementAndGet() ;
            numWaitersEnd( num ) ;
        }
    }

    @InfoMethod
    private void nWaiters( int value ) { }

    @Poa
    // Note: caller MUST hold write lock
    private void notifyWaiters() 
    {
        int num = nWaiters.get() ;
        nWaiters( num ) ;

        if (num >0) {
            stateCV.signalAll() ;
        }
    }

    @ManagedAttribute
    @NameValue
    @Description( "The ID of this POAManager" )
    public int getManagerId() 
    {
        return myId ;
    }

    POAManagerImpl( POAFactory factory, PIHandler pih )
    {
        this.factory = factory ;
        factory.addPoaManager(this);
        pihandler = pih ;
        myId = factory.newPOAManagerId() ;
        state = State.HOLDING;
        explicitStateChange = false ;
    }

    void addPOA(POAImpl poa)
    {
        stateLock.writeLock().lock();
        try {
            if (state.value() == State._INACTIVE) {
                throw wrapper.addPoaInactive() ;
            }

            poas.add( poa);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    void removePOA(POAImpl poa)
    {
        stateLock.writeLock().lock();
        try {
            poas.remove( poa);
            if ( poas.isEmpty() ) {
                factory.removePoaManager(this);
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    @ManagedAttribute
    @Description( "The ObjectReferenceTemplate state of this POAManager" )
    public short getORTState() 
    {
        switch (state.value()) {
            case State._HOLDING    : return HOLDING.value ;
            case State._ACTIVE     : return ACTIVE.value ;
            case State._INACTIVE   : return INACTIVE.value ;
            case State._DISCARDING : return DISCARDING.value ;
            default                : return NON_EXISTENT.value ;
        }
    }

/****************************************************************************
 * The following four public methods are used to change the POAManager's state.
 ****************************************************************************/

    /**
     * <code>activate</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     */
    @Poa
    @ManagedOperation
    @Description( "Make this POAManager active, so it can handle new requests" ) 
    public void activate()
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
        explicitStateChange = true ;

        stateLock.writeLock().lock() ;

        try {
            if ( state.value() == State._INACTIVE ) {
                throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
            }

            // set the state to ACTIVE
            state = State.ACTIVE;

            pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

            // Notify any invocations that were waiting because the previous
            // state was HOLDING, as well as notify any threads that were waiting
            // inside hold_requests() or discard_requests().
            notifyWaiters();
        } finally {
            stateLock.writeLock().unlock() ;
        }
    }

    /**
     * <code>hold_requests</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     */
    @Poa
    @ManagedOperation
    @Description( "Hold all requests to this POAManager" ) 
    public void hold_requests(boolean wait_for_completion)
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
        explicitStateChange = true ;

        stateLock.writeLock().lock() ;

        try {
            if ( state.value() == State._INACTIVE ) {
                throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
            }
            // set the state to HOLDING
            state  = State.HOLDING;

            pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

            // Notify any threads that were waiting in the wait() inside
            // discard_requests. This will cause discard_requests to return
            // (which is in conformance with the spec).
            notifyWaiters();

            if ( wait_for_completion ) {
                while ( state.value() == State._HOLDING
                    && nInvocations.get() > 0 ) {

                    countedWait() ;
                }
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * <code>discard_requests</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     */
    @Poa
    @ManagedOperation
    @ParameterNames( { "waitForCompletion" } )
    @Description( "Make this POAManager discard all incoming requests" ) 
    public void discard_requests(boolean wait_for_completion)
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
        explicitStateChange = true ;

        stateLock.writeLock().lock();

        try {
            if ( state.value() == State._INACTIVE ) {
                throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
            }

            // set the state to DISCARDING
            state = State.DISCARDING;

            pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

            // Notify any invocations that were waiting because the previous
            // state was HOLDING. Those invocations will henceforth be rejected with
            // a TRANSIENT exception. Also notify any threads that were waiting
            // inside hold_requests().
            //
            // Must hold writeLock for this call.
            notifyWaiters();

            if ( wait_for_completion ) {
                while ( state.value() == State._DISCARDING
                    && nInvocations.get() > 0 ) {

                    // Must hold writeLock for this call.
                    countedWait() ;
                }
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * <code>deactivate</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     * Note: INACTIVE is a permanent state.
     */

    @Poa
    public void deactivate(boolean etherealize_objects, boolean wait_for_completion)
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
        stateLock.writeLock().lock();

        try {
            explicitStateChange = true ;

            if ( state.value() == State._INACTIVE ) {
                throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
            }

            state = State.INACTIVE;

            pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

            // Notify any invocations that were waiting because the previous
            // state was HOLDING. Those invocations will then be rejected with
            // an OBJ_ADAPTER exception. Also notify any threads that were waiting
            // inside hold_requests() or discard_requests().
            notifyWaiters();
        } finally {
            stateLock.writeLock().unlock();
        }

        POAManagerDeactivator deactivator = new POAManagerDeactivator( this,
            etherealize_objects ) ;

        if (wait_for_completion) {
            deactivator.run();
        } else {
            Thread thr = new Thread(deactivator) ;
            thr.start() ;
        }
    }

    @Poa
    private static class POAManagerDeactivator implements Runnable
    {
        private boolean etherealize_objects ;
        private final POAManagerImpl pmi ;

        @InfoMethod
        private void poaManagerDeactivatorCall(
            boolean etherealizeObjects, POAManagerImpl pmi ) { }

        @InfoMethod
        private void preparingToEtherealize( POAManagerImpl pmi ) { }

        @InfoMethod
        private void removeAndClear( POAManagerImpl pmi ) { }

        POAManagerDeactivator( POAManagerImpl pmi, boolean etherealize_objects )
        {
            this.etherealize_objects = etherealize_objects ;
            this.pmi = pmi ;
        }

        @Poa
        public void run() 
        {
            pmi.stateLock.writeLock().lock();
            try {
                poaManagerDeactivatorCall( etherealize_objects, pmi ) ;
                while ( pmi.nInvocations.get() > 0 ) {
                    pmi.countedWait() ;
                }
            } finally {
                pmi.stateLock.writeLock().unlock();
            }

            if (etherealize_objects) {
                Set<POAImpl> copyOfPOAs ;

                // Make sure that poas cannot change while we copy it!
                pmi.stateLock.readLock().lock();
                try {
                    preparingToEtherealize( pmi ) ;
                    copyOfPOAs = new HashSet<POAImpl>( pmi.poas ) ;
                } finally {
                    pmi.stateLock.readLock().unlock();
                }

                for (POAImpl poa : copyOfPOAs) {
                    // Each RETAIN+USE_SERVANT_MGR poa
                    // must call etherealize for all its objects
                    poa.etherealizeAll();
                }

                pmi.stateLock.writeLock().lock();
                try {
                    removeAndClear( pmi ) ;

                    pmi.factory.removePoaManager(pmi);
                    pmi.poas.clear();
                } finally {
                    pmi.stateLock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Added according to the spec CORBA V2.3; this returns the
     * state of the POAManager
     */

    public org.omg.PortableServer.POAManagerPackage.State get_state () {
        return state;
    }

/****************************************************************************
 * The following methods are used on the invocation path.
 ****************************************************************************/

    @InfoMethod
    private void activeManagers( MultiSet<POAManagerImpl> am ) { }

    @InfoMethod
    private void alreadyActive( POAManagerImpl pm ) { }

    @InfoMethod
    private void activeInDifferentPoaManager() { }

    @Poa
    private void checkState()
    {
        MultiSet<POAManagerImpl> am = activeManagers.get() ;
        activeManagers( am ) ;

        stateLock.readLock().lock();
        try {
            while ( state.value() != State._ACTIVE ) {
                switch ( state.value() ) {
                    case State._HOLDING:
                        // Never block a thread that is already active in this POAManager.
                        if (am.contains( this )) {
                            alreadyActive( this ) ;

                            return ;
                        } else {
                            if (am.size() == 0) {
                                if (state.value() == State._HOLDING) {
                                    // Can't upgrade, so drop the read lock,
                                    // then separately acquite the write lock.
                                    stateLock.readLock().unlock();
                                    stateLock.writeLock().lock();
                                }

                                try {
                                    while ( state.value() == State._HOLDING ) {
                                        countedWait() ;
                                    }
                                } finally {
                                    // downgrade to read lock
                                    stateLock.writeLock().unlock();
                                    stateLock.readLock().lock();
                                }
                            } else {
                                activeInDifferentPoaManager() ;

                                // This thread is already active in one or more other POAManagers.
                                // This could cause a deadlock, so throw a TRANSIENT exception
                                // to prevent it.
                                throw factory.getWrapper().poaManagerMightDeadlock() ;
                            }
                        }
                        break;

                    case State._DISCARDING:
                        throw factory.getWrapper().poaDiscarding() ;

                    case State._INACTIVE:
                        throw factory.getWrapper().poaInactive() ;
                }
            }
        } finally {
            stateLock.readLock().unlock();
        }
    }

    @InfoMethod
    private void addingThreadToActiveManagers( POAManagerImpl pmi ) { }

    @InfoMethod
    private void removingThreadFromActiveManagers( POAManagerImpl pmi ) { }

    @Poa
    void enter()
    {
        checkState();
        nInvocations.getAndIncrement() ;

        activeManagers.get().add( this ) ;
        addingThreadToActiveManagers( this ) ;
    }

    @Poa
    void exit()
    {
        try {
            activeManagers.get().remove( this ) ;
            removingThreadFromActiveManagers( this ) ;
        } finally {
            if ( nInvocations.decrementAndGet() == 0 ) {
                // Note: this is essentially notifyWaiters, but
                // we cannot afford to acquire the writeLock unless
                // there actually are waiters on the lock (GF issue 14348).
                // Note that a spurious wakeup is possible here, if
                // an invocation comes in between nInvocation and nWaiters 
                // reads, but that's OK, because the looped condition checks
                // around countedWait will simply wait again.
                final int num = nWaiters.get() ;
                nWaiters( num ) ;

                if (num >0) {
                    stateLock.writeLock().lock();

                    try {
                        stateCV.signalAll() ;
                    } finally {
                        stateLock.writeLock().unlock();
                    }
                }
            }
        }
    }

    /** Activate the POAManager if no explicit state change has ever been
     * previously invoked.
     */
    public void implicitActivation() 
    {
        if (!explicitStateChange) {
            try {
                activate();
            } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive ai) {
            }
        }
    }
}
