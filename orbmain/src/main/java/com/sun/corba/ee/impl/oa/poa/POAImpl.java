/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.poa;

import java.util.Collection ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Iterator ;

import java.util.concurrent.locks.Condition ;
import java.util.concurrent.locks.ReentrantLock ;

import javax.management.ObjectName ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.SystemException ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.POAManager ;
import org.omg.PortableServer.AdapterActivator ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.ImplicitActivationPolicy;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.ThreadPolicyValue ;
import org.omg.PortableServer.LifespanPolicyValue ;
import org.omg.PortableServer.IdUniquenessPolicyValue ;
import org.omg.PortableServer.IdAssignmentPolicyValue ;
import org.omg.PortableServer.ImplicitActivationPolicyValue ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists ;
import org.omg.PortableServer.POAPackage.AdapterNonExistent ;
import org.omg.PortableServer.POAPackage.InvalidPolicy ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.WrongAdapter ;
import org.omg.PortableServer.POAPackage.NoServant ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;

import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.NON_EXISTENT ;

import com.sun.corba.ee.spi.copyobject.CopierManager ;
import com.sun.corba.ee.spi.oa.OADestroyed ;
import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.spi.oa.ObjectAdapterBase ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectAdapterId ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.IORTemplateList ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.protocol.ForwardException ;

import com.sun.corba.ee.impl.ior.POAObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.ObjectAdapterIdArray ;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.spi.logging.POASystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.trace.Poa;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.NameValue;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * POAImpl is the implementation of the Portable Object Adapter. It 
 * contains an implementation of the POA interfaces specified in
 * COBRA 2.3.1 chapter 11 (formal/99-10-07).  This implementation
 * is moving to comply with CORBA 3.0 due to the many clarifications
 * that have been made to the POA semantics since CORBA 2.3.1.
 * Specific comments have been added where 3.0 applies, but note that
 * we do not have the new 3.0 APIs yet.
 */
@Poa
@ManagedObject
public class POAImpl extends ObjectAdapterBase implements POA 
{
    private static final POASystemException wrapper =
        POASystemException.self ;
    private static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    private static final long serialVersionUID = -1746388801294205323L;

    // POA Creation
    //
    // POA creation takes place in 2 stages: 
    //     first, the POAImpl constructor is called 
    //     then, the initialize method is called.  
    // This separation is needed because an AdapterActivator does not know 
    // the POAManager or the policies when 
    // the unknown_adapter method is invoked.  However, the POA must be created
    // before the unknown_adapter method is invoked, so that the parent knows
    // when concurrent attempts are made to create the same POA.
    //
    // Calling the POAImpl constructor results in a new POA without initializing 
    // the new POA.  new POAImpl is called in two places:
    // 1. inside create_POA: state = STATE_START
    // 2. inside find_POA: state = STATE_INIT 
    //
    // Calling initialize( POAManager, Policies ) results in state STATE_RUN 
    // (if the POA create created directly in create_POA) or
    // state STATE_INIT_DONE (if the POA was create in an AdapterActivator).
    //
    // Calling destroy results in STATE_DESTROYING, which marks the beginning of
    // POA destruction.
    //
    // destroyIfNotInitDone completes moves the POA state from STATE_INIT_DONE
    // to STATE_RUN.  It is called from find_POA after the unknown_adapter
    // call returns.  Note that unknown_adapter MUST call create_POA at some
    // point.

    // Notes on concurrency
    //
    // The POA requires careful design for concurrency management to correctly
    // implement the specification and avoid deadlocks.  The order of acquiring
    // locks must respect the following locking hierarchy:
    //
    // 1. Lock POAs before POAManagers
    // 2. Lock a POA before locking its child POA
    //
    // Also note that there are 3 separate conditions on which threads may wait
    // in the POA, as defined by invokeCV, beingDestroyedCV, and 
    // adapterActivatorCV.  This means that (for this reason as well as others) 
    // we cannot simply use the standard Java synchronized primitive.  
    // This implementation uses a modified version of Doug Lea's 
    // util.concurrent (version 1.3.0) that supports reentrant
    // mutexes to handle the locking.  This will all be replaced by the new JSR 
    // 166 concurrency primitives in J2SE 1.5 and later once the ORB moves to 
    // J2SE 1.5.
    //
    // Recently I've modified the POA to support read/write locking.  This works
    // well for reducing lock contention, but it can be a bit tricky.  ALl 3
    // condition variables are creates from poaMutex.writeLock, so 
    // poaMutex.writeLock() must be held whenever an acquire or signal method
    // is called on one of the condition variables.  Since it is not possible
    // to upgrade a read lock to a write lock, using a condition variable
    // requires dropping the read lock, acquiring the write lock, and possibly
    // then checking again to make sure that an invariant is still satisified.
    
    // POA state constants
    //
    // Note that ordering is important here: we must have the state defined in 
    // this order so that ordered comparison is possible.
    // DO NOT CHANGE THE VALUES OF THE STATE CONSTANTS!!!  In particular, the
    // initialization related states must be lower than STATE_RUN, and the 
    // destruction related state must be higher.
    //
    // A POA is created in STATE_START
    //
    // Valid state transitions:
    //
    // START to INIT                        after find_POA constructor call 
    // START to RUN                         after initialize completes
    // INIT to INIT_DONE                    after initialize completes
    // INIT to DESTROYED                    after failed unknown_adapter 
    // INIT_DONE to RUN                     after successful unknown_adapter 
    // STATE_RUN to STATE_DESTROYING        after start of destruction
    // STATE_DESTROYING to STATE_DESTROYED  after destruction completes.

    private static final int STATE_START        = 0 ; // constructor complete
    private static final int STATE_INIT         = 1 ; // waiting for adapter activator
    private static final int STATE_INIT_DONE    = 2 ; // adapter activator called create_POA
    private static final int STATE_RUN          = 3 ; // initialized and running
    private static final int STATE_DESTROYING   = 4 ; // being destroyed
    private static final int STATE_DESTROYED    = 5 ; // destruction complete

    private String stateToString()
    {
        switch (state) {
            case STATE_START :
                return "START" ;
            case STATE_INIT :
                return "INIT" ;
            case STATE_INIT_DONE :
                return "INIT_DONE" ;
            case STATE_RUN :
                return "RUN" ;
            case STATE_DESTROYING :
                return "DESTROYING" ;
            case STATE_DESTROYED :
                return "DESTROYED" ;
            default :
                return "UNKNOWN(" + state + ")" ;
        } 
    }

    // Current state of the POA
    private int state ;

    // The POA request handler that performs all policy specific operations
    // Note that POAImpl handles all synchronization, so mediator is (mostly)
    // unsynchronized.
    private POAPolicyMediator mediator;

    // Representation of object adapter ID
    private final int numLevels;            // counts depth of tree.  Root = 1.
    private final ObjectAdapterId poaId ; // the actual object adapter ID for this POA

    private final String poaName;           // the name of this POA

    private POAManagerImpl manager; // This POA's POAManager
    private final int uniquePOAId ;         // ID for this POA that is unique relative
                                    // to the POAFactory, which has the same 
                                    // lifetime as the ORB.
    private POAImpl parent;         // The POA that created this POA.
    private final Map<String,POAImpl> children; // Map from name to POA of POAs
                                          // created by this POA.

    private AdapterActivator activator;
    private final AtomicInteger invocationCount ; // pending invocations on this POA.

    // Data used to control POA concurrency

    // Master lock for all POA synchronization.  See lock and unlock.
    // package private for access by AOMEntry.
    final ReadWriteLock poaMutex ;

    // Wait on this CV for AdapterActivator upcalls to complete 
    private final Condition adapterActivatorCV ;

    // Wait on this CV for all active invocations to complete 
    private final Condition invokeCV ;

    // Wait on this CV for the destroy method to complete doing its work
    private final Condition beingDestroyedCV ;

    // thread local variable to store a boolean to detect deadlock in 
    // POA.destroy().
    private final ThreadLocal<Boolean> isDestroying ;

    // Used for synchronized access to the ManagedObjectManager.
    private static final Object momLock = new Object() ;

    // This includes the most important information for debugging
    // POA problems.
    @Override
    public String toString()
    {
        return "POA[" + poaId.toString() + 
            ", uniquePOAId=" + uniquePOAId + 
            ", state=" + stateToString() + 
            ", invocationCount=" + invocationCount.get() + "]" ;
    }

    @ManagedAttribute( id="POAState")
    @Description( "The current state of the POA")
    private String getDisplayState() {
        lock() ;
        try {
            return stateToString() ;
        } finally {
            unlock() ;
        }
    }

    @ManagedAttribute
    @Description( "The POA's mediator")
    POAPolicyMediator getMediator() {
        return mediator ;
    }

    @ManagedAttribute
    @Description( "The ObjectAdapterId for this POA")
    private ObjectAdapterId getObjectAdapterId() {
        return poaId ;
    }

    // package private for access to servant to POA map
    static POAFactory getPOAFactory( ORB orb )
    {
        return (POAFactory)orb.getRequestDispatcherRegistry().
            getObjectAdapterFactory( ORBConstants.TRANSIENT_SCID ) ;
    }

    @Poa
    private static void registerMBean( ORB orb, Object obj ) {
        orb.mom().register( getPOAFactory( orb ), obj ) ;
    }

    // package private so that POAFactory can access it.
    static POAImpl makeRootPOA( ORB orb )
    {
        POAManagerImpl poaManager = new POAManagerImpl( getPOAFactory( orb ), 
            orb.getPIHandler() ) ;
        registerMBean( orb, poaManager ) ;

        POAImpl result = new POAImpl( ORBConstants.ROOT_POA_NAME, 
            null, orb, STATE_START ) ;
        result.initialize( poaManager, Policies.rootPOAPolicies ) ;
        // must come after initialize!
        registerMBean( orb, result ) ;

        return result ;
    }

    // package private so that POAPolicyMediatorBase can access it.
    @ManagedAttribute()
    @Description( "The unique ID for this POA")
    int getPOAId()
    {
        return uniquePOAId ;
    }

    @InfoMethod
    private void thisPoa( POAImpl p ) { }

    @InfoMethod
    private void acquireLockWaiting( int count )  {}

    @Poa
    // Be paranoid about lost wakeup problems like 6822370
    // GLASSFISH_CORBA-11, GLASSFISH-16217, Bug #14247062, Bug #14390811 :
    // - 6822370 is resolved in JDK 6u21, hence simply call lock()
    private void acquireLock( Lock lock ) {
        lock.lock();
    }

    // package private so that POAPolicyMediator can access it.
    @Poa
    void lock()
    {
        acquireLock( poaMutex.writeLock() ) ;
        thisPoa( this ) ;
    }

    // package private so that POAPolicyMediator can access it.
    @Poa
    void unlock()
    {
        thisPoa( this ) ;
        poaMutex.writeLock().unlock() ;
    }

    @Poa
    void readLock()
    {
        acquireLock( poaMutex.readLock() ) ;
        thisPoa( this ) ;
    }

    // package private so that POAPolicyMediator can access it.
    @Poa
    void readUnlock()
    {
        thisPoa( this ) ;
        poaMutex.readLock().unlock() ;
    }

    @Poa
    final Condition makeCondition() {
        return poaMutex.writeLock().newCondition() ;
    }

    // package private so that DelegateImpl can access it.
    Policies getPolicies() 
    {
        return mediator.getPolicies() ;
    }

    @Poa
    private void newPOACreated( String name, String parentName ) { }

    // Note that the parent POA must be write locked when this constructor
    // is called.
    private POAImpl( String name, POAImpl parent, ORB orb, int initialState ) {
        super( orb ) ;

        if (parent == null) {
            newPOACreated( name, "null parent for root POA" ) ;
        } else {
            newPOACreated( name, parent.poaName ) ;
        }

        this.state     = initialState ;
        this.poaName   = name ;
        this.parent    = parent;
        children = new HashMap<String,POAImpl>();
        activator = null ;

        // This was done in initialize, but I moved it here
        // to get better searchability when tracing.
        uniquePOAId = getPOAFactory( orb ).newPOAId() ;

        if (parent == null) {
            // This is the root POA, which counts as 1 level
            numLevels = 1 ;
        } else {
            // My level is one more than that of my parent
            numLevels = parent.numLevels + 1 ;

            parent.children.put(name, this);
        }

        // Get an array of all of the POA names in order to
        // create the poaid.
        String[] names = new String[ numLevels ] ;
        POAImpl poaImpl = this ;
        int ctr = numLevels - 1 ;
        while (poaImpl != null) {
            names[ctr] = poaImpl.poaName ;
            ctr-- ;
            poaImpl = poaImpl.parent ;
        }

        poaId = new ObjectAdapterIdArray( names ) ;

        invocationCount = new AtomicInteger(0) ;

        poaMutex = new ReentrantReadWriteLock() ;
        adapterActivatorCV = makeCondition() ;
        invokeCV           = makeCondition() ;
        beingDestroyedCV   = makeCondition() ;

        isDestroying = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
    }

    @NameValue
    private String getName() {
        StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for (String str : poaId.getAdapterName() ) {
            if (first) {
                first = false ;
            } else {
                sb.append( '.' ) ;
            }
            sb.append( str ) ;
        }
        return sb.toString() ;
    }

    @InfoMethod
    private void initializingPoa( int scid, int serverid, String orbid,
        ObjectAdapterId poaId ) { }

    // The POA write lock must be held when this method is called.
    @Poa
    private void initialize( POAManagerImpl manager, Policies policies ) 
    {
        this.manager = manager;
        manager.addPOA(this);

        mediator = POAPolicyMediatorFactory.create( policies, this ) ;

        // Construct the object key template
        int serverid = mediator.getServerId() ;
        int scid = mediator.getScid() ;
        String orbId = getORB().getORBData().getORBId();

        ObjectKeyTemplate oktemp = new POAObjectKeyTemplate( getORB(), 
            scid, serverid, orbId, poaId ) ;


        initializingPoa( scid, serverid, orbId, poaId ) ;

        // Note that parent == null iff this is the root POA.
        // This was used to avoid executing interceptors on the RootPOA.
        // That is no longer necessary.
        boolean objectAdapterCreated = true; // parent != null ;
    
        // XXX extract codebase from policies and pass into initializeTemplate
        // after the codebase policy change is finalized.
        initializeTemplate( oktemp, objectAdapterCreated,
                            policies, 
                            null, // codebase
                            null, // manager id
                            oktemp.getObjectAdapterId()
                            ) ;

        if (state == STATE_START) {
            state = STATE_RUN;
        } else if (state == STATE_INIT) {
            state = STATE_INIT_DONE;
        } else {
            throw wrapper.illegalPoaStateTrans();
        }
    }

    @InfoMethod
    private void interruptedAwait( InterruptedException exc ) {}

    // The POA write lock must be held when this method is called
    // The lock may be upgraded to write.
    @Poa
    private boolean waitUntilRunning() 
    {
        while (state < STATE_RUN) {
            try {
                adapterActivatorCV.await( 1, TimeUnit.SECONDS ) ;
            } catch (InterruptedException exc) {
                interruptedAwait( exc ) ;
            }
        } 

        // Note that a POA could be destroyed while in STATE_INIT due to a 
        // failure in the AdapterActivator upcall.
        return (state == STATE_RUN) ;
    }

    // This method checks that the AdapterActivator finished the 
    // initialization of a POA activated in find_POA.  This is
    // determined by checking the state of the POA.  If the state is
    // STATE_INIT, the AdapterActivator did not complete the 
    // inialization.  In this case, we destroy the POA that was
    // partially created and return false.  Otherwise, we return true.
    // In any case, we must wake up all threads waiting for the adapter
    // activator, either to continue their invocations, or to return
    // errors to their client.
    //
    // No POA lock may be held when this method is called.
    private boolean destroyIfNotInitDone()
    {
        lock() ;

        try {
            boolean success = (state == STATE_INIT_DONE) ;

            if (success) {
                state = STATE_RUN ;
                unlock() ;
            } else {
                // Issue 7061: do not hold POA lock while calling destroyer.doIt,
                // because this would violate the lock ordering constraints,
                // since doIt locks the parent first, then the child.
                unlock() ;

                // Don't just use destroy, because the check for 
                // deadlock is too general, and can prevent this from
                // functioning properly.
                DestroyThread destroyer = new DestroyThread( false );
                destroyer.doIt( this, true ) ;
            }

            return success ;
        } finally {
            lock() ;

            try {
                adapterActivatorCV.signalAll() ;
            } finally {
                unlock() ;
            }
        }
    }

    private byte[] internalReferenceToId( 
        org.omg.CORBA.Object reference ) throws WrongAdapter 
    {
        IOR ior = getORB().getIOR( reference, false ) ;
        IORTemplateList thisTemplate = ior.getIORTemplates() ;

        ObjectReferenceFactory orf = getCurrentFactory() ;
        IORTemplateList poaTemplate = 
            IORFactories.getIORTemplateList( orf ) ;

        if (!poaTemplate.isEquivalent( thisTemplate )) {
            throw new WrongAdapter();
        }
            
        // Extract the ObjectId from the first TaggedProfile in the IOR.
        // If ior was created in this POA, the same ID was used for 
        // every profile through the profile templates in the currentFactory,
        // so we will get the same result from any profile.
        Iterator<TaggedProfile> iter = ior.iterator() ;
        if (!iter.hasNext()) {
            throw wrapper.noProfilesInIor();
        }
        TaggedProfile prof = (iter.next()) ;
        ObjectId oid = prof.getObjectId() ;

        return oid.getId();
    }

    // Converted from anonymous class to local class
    // so that we can call performDestroy() directly.
    @Poa
    private static class DestroyThread extends Thread {
        private boolean wait ;
        private boolean etherealize ;
        private POAImpl thePoa ;

        DestroyThread( boolean etherealize ) {
            this.etherealize = etherealize ;
        }

        @Poa
        public void doIt( POAImpl thePoa, boolean wait ) {
            this.thePoa = thePoa ;
            this.wait = wait ;
    
            if (wait) {
                run() ;
            } else {
                // Catch exceptions since setDaemon can cause a
                // security exception to be thrown under netscape
                // in the Applet mode
                try { 
                    setDaemon(true); 
                } catch (Exception e) {
                    thePoa.wrapper.couldNotSetDaemon( e ) ;
                }

                start() ;
            }
        }

        @Poa
        @Override
        public void run() 
        {
            final Set<ObjectReferenceTemplate> destroyedPOATemplates =
                new HashSet<ObjectReferenceTemplate>() ;

            performDestroy( thePoa, destroyedPOATemplates );

            Iterator<ObjectReferenceTemplate> iter = destroyedPOATemplates.iterator() ;
            ObjectReferenceTemplate[] orts = new ObjectReferenceTemplate[ 
                destroyedPOATemplates.size() ] ;
            int index = 0 ;
            while (iter.hasNext()) {
                orts[index] = iter.next();
                index++ ;
            }

            ORB myORB = thePoa.getORB() ;

            if (destroyedPOATemplates.size() > 0) {
                myORB.getPIHandler().adapterStateChanged( orts,
                    NON_EXISTENT.value ) ;
            }
        }
    
        // Returns true if destruction must be completed, false 
        // if not, which means that another thread is already
        // destroying poa.
        @Poa
        private boolean prepareForDestruction( POAImpl poa, 
            Set<ObjectReferenceTemplate> destroyedPOATemplates )
        {
            POAImpl[] childPoas = null ;

            // Note that we do not synchronize on this, since this is
            // the PerformDestroy instance, not the POA.
            poa.lock() ;

            try {
                if (poa.state <= STATE_RUN) {
                    poa.state = STATE_DESTROYING ;
                } else {
                    // destroy may be called multiple times, and each call
                    // is allowed to proceed with its own setting of the wait 
                    // flag, but the etherealize value is used from the first 
                    // call to destroy.  Also all children should be destroyed 
                    // before the parent POA.  If the poa is already destroyed, 
                    // we can just return.  If the poa has started destruction, 
                    // but not completed, and wait is true, we need to wait 
                    // until destruction is complete, then just return.
                    if (wait) {
                        while (poa.state != STATE_DESTROYED) {
                            try {
                                poa.beingDestroyedCV.await( 1, TimeUnit.SECONDS );
                            } catch (InterruptedException exc) {
                                interruptedAwait( exc ) ;
                            }
                        }
                    }

                    return false ;
                }

                poa.isDestroying.set(Boolean.TRUE);

                // Make a copy since we can't hold the lock while destroying
                // the children, and an iterator is not deletion-safe.
                childPoas = poa.children.values().toArray( new POAImpl[0] );
            } finally {
                poa.unlock() ;
            }

            // We are not holding the POA mutex here to avoid holding it 
            // while destroying the POA's children, since this may involve 
            // upcalls to etherealize methods.

            for (int ctr=0; ctr<childPoas.length; ctr++ ) {
                performDestroy( childPoas[ctr], destroyedPOATemplates ) ;
            }

            return true ;
        }

        @Poa
        public void performDestroy( POAImpl poa, 
            Set<ObjectReferenceTemplate> destroyedPOATemplates )
        {
            if (!prepareForDestruction( poa, destroyedPOATemplates )) {
                return;
            }

            // NOTE: If we are here, poa is in STATE_DESTROYING state. All 
            // other state checks are taken care of in prepareForDestruction.
            // No other threads may either be starting new invocations 
            // by calling enter or starting to destroy poa.  There may
            // still be pending invocations.

            POAImpl parent = poa.parent ;
            boolean isRoot = parent == null ;

            // Note that we must lock the parent before the child.
            // The parent lock is required (if poa is not the root)
            // to safely remove poa from parent's children Map.
            if (!isRoot) {
                parent.lock();
            }

            try {
                poa.lock() ;
                try {
                    completeDestruction( poa, parent, 
                        destroyedPOATemplates ) ;
                } finally {
                    poa.unlock() ;

                    if (isRoot) {
                        poa.manager.getFactory().registerRootPOA();
                    }
                }
            } finally {
                if (!isRoot) {
                    parent.unlock() ;
                    poa.parent = null ;          
                }
            }
        }

        @InfoMethod
        private void unregisteringMBean( ObjectName oname, POAImpl poa ) { }

        @InfoMethod
        private void noMBean( POAImpl poa ) { }

        @InfoMethod
        private void interruptedAwait( InterruptedException exc ) {}

        @Poa
        private void completeDestruction( POAImpl poa, POAImpl parent, 
            Set<ObjectReferenceTemplate> destroyedPOATemplates )
        {
            try {
                while (poa.invocationCount.get() != 0) {
                    try {
                        poa.invokeCV.await( 1, TimeUnit.SECONDS ) ;
                    } catch (InterruptedException ex) {
                        interruptedAwait( ex ) ;
                    } 
                }

                if (poa.mediator != null) {
                    if (etherealize) {
                        poa.mediator.etherealizeAll();
                    }
                        
                    poa.mediator.clearAOM() ;
                }

                if (poa.manager != null) {
                    poa.manager.removePOA(poa);
                }

                if (parent != null) {
                    parent.children.remove(poa.poaName);
                }

                destroyedPOATemplates.add( poa.getAdapterTemplate() ) ;

                synchronized (momLock) {
                    // Only unregister if the poa is still registered: we may get
                    // here because another thread concurrently destroyed this POA.
                    // XXX This may not be necessary now.
                    ObjectName oname = poa.getORB().mom().getObjectName( poa ) ; 
                    if (oname != null) {
                        unregisteringMBean( oname, poa ) ;
                        poa.getORB().mom().unregister( poa );
                    } else {
                        noMBean( poa ) ;
                    }
                }
            } catch (Throwable thr) {
                if (thr instanceof ThreadDeath) {
                    throw (ThreadDeath) thr;
                }

                wrapper.unexpectedException( thr, poa.toString() ) ;
            } finally {
                poa.state = STATE_DESTROYED ;
                poa.beingDestroyedCV.signalAll();
                poa.isDestroying.set(Boolean.FALSE);
            }
        }
    }

    @Poa
    void etherealizeAll()
    {
        lock() ;

        try {
            mediator.etherealizeAll() ;
        } finally {
            unlock() ;
        }
    }

 //*******************************************************************
 // Public POA API 
 //*******************************************************************
    @InfoMethod
    private void newPOA( POAImpl poa ) { }

    /**
     * <code>create_POA</code>
     * <b>Section 3.3.8.2</b>
     */
    @Poa
    public POA create_POA(String name, POAManager 
        theManager, Policy[] policies) throws AdapterAlreadyExists, 
        InvalidPolicy 
    {
        lock() ;

        try {
            // We cannot create children of a POA that is (being) destroyed.
            // This has been added to the CORBA 3.0 spec.
            if (state > STATE_RUN) {
                throw omgWrapper.createPoaDestroy();
            }
                
            POAImpl poa = children.get(name) ;

            if (poa == null) {
                poa = new POAImpl( name, this, getORB(), STATE_START ) ;
            }

            try {
                poa.lock() ;
                newPOA( poa ) ;

                if ((poa.state != STATE_START) && (poa.state != STATE_INIT)) {
                    throw new AdapterAlreadyExists();
                }

                POAManagerImpl newManager = (POAManagerImpl)theManager ;
                if (newManager == null) {
                    newManager = new POAManagerImpl( manager.getFactory(),
                        getORB().getPIHandler() );
                    registerMBean( getORB(), newManager ) ;
                }

                int defaultCopierId = 
                    getORB().getCopierManager().getDefaultId() ;
                Policies POAPolicies = 
                    new Policies( policies, defaultCopierId ) ;

                poa.initialize( newManager, POAPolicies ) ;

                // Issue 11334: Must come after poa.initialize!
                registerMBean( getORB(), poa ) ;

                return poa;
            } finally {
                poa.unlock() ;
            }
        } finally {
            unlock() ;
        }
    }

    @InfoMethod
    private void foundPOA( POAImpl poa ) { }

    @InfoMethod
    private void createdPOA( POAImpl poa ) { }

    @InfoMethod
    private void noPOA() { }

    @InfoMethod
    private void callingAdapterActivator() { }

    @InfoMethod
    private void adapterActivatorResult( boolean result ) { }

    /** <code>find_POA</code>
     * <b>Section 3.3.8.3</b>
     */
    @Poa
    public POA find_POA(String name, boolean activate) throws AdapterNonExistent {
        AdapterActivator act = null ;
        boolean readLocked = false ;
        boolean writeLocked = false ;
        boolean childReadLocked = false ;
        POAImpl child = null ;

        try {
            // Issue 14318: Use readLock where possible to reduce contention.
            readLock() ; readLocked = true ;

            child = children.get(name);
            if (child != null) {
                child.readLock() ; childReadLocked = true ;
                foundPOA( child ) ;
                try {
                    // No parent lock while waiting for child init to complete!
                    readUnlock() ; readLocked = false ;

                    if (child.state != STATE_RUN) {
                        child.readUnlock() ; childReadLocked = false ;
                        child.lockAndWaitUntilRunning() ;
                    }
                    // child may be in DESTROYING or DESTROYED at this point.  
                    // That's OK, since destruction could start at any time.
                } finally {
                    if (childReadLocked) { child.readUnlock() ; childReadLocked = false ; }
                }
            } else {
                try {
                    noPOA() ;

                    if (activate && (activator != null)) {
                        readUnlock() ; readLocked = false ; // need writeLock: drop readLock

                        // Note that another thread could create the child here
                        // in between the unlock and the lock.

                        lock() ; writeLocked = true ;
                        try {
                            child = children.get(name);
                            if (child == null) {
                                child = new POAImpl( name, this, getORB(), STATE_INIT ) ;
                                createdPOA( child ) ;
                                act = activator ; // Issue 14917: Only set if child NOT found
                            } else { // Child created before writeLock
                                unlock() ; writeLocked = false ;  // don't hold parent lock!
                                child.lockAndWaitUntilRunning() ;
                            }
                        } finally {
                            if (writeLocked) { unlock() ; writeLocked = false ; }
                        }
                    } else {
                        throw new AdapterNonExistent();
                    }
                } finally {
                    if (readLocked) { readUnlock() ; } // Issue 14917: was unlock()
                }
            }

            // assert (child != null) and not holding locks on this or child (must avoid deadlock)
            if (act != null) {
                doActivate( act, name, child ) ;
            }

            return child;
        } finally {
            cleanUpLocks( child, readLocked, writeLocked, childReadLocked ) ;
        }
    }

    @Poa
    private void lockAndWaitUntilRunning() {
        // Issue 14695: waitUntilRunning requires writeLock.
        lock() ;
        try {
            // wait for child init to complete
            if (!waitUntilRunning()) {
                // OMG 3.0 11.3.9.3, in reference to unknown_adapter
                throw omgWrapper.poaDestroyed();
            }
        } finally {
            unlock() ;
        }
    }

    @Poa
    private void doActivate( AdapterActivator act, 
        String name, POAImpl child ) throws AdapterNonExistent {

        boolean status = false ;
        boolean adapterResult = false ;
        callingAdapterActivator() ;

        try {
            // Prevent more than one thread at a time from executing in act
            // in case act is shared between multiple POAs.
            synchronized (act) {
                status = act.unknown_adapter(this, name);
            }
        } catch (SystemException exc) {
            throw omgWrapper.adapterActivatorException( exc,
                poaName, poaId ) ;
        } catch (Throwable thr) {
            // ignore most non-system exceptions, but log them for
            // diagnostic purposes.
            wrapper.unexpectedException( thr, this.toString() ) ;

            if (thr instanceof ThreadDeath) {
                throw (ThreadDeath) thr;
            }
        } finally {
            // At this point, we have completed adapter activation.
            // Whether this was successful or not, we must call
            // destroyIfNotInitDone so that calls to enter() and create_POA()
            // that are waiting can execute again.  Failing to do this
            // will cause the system to hang in complex tests.
            adapterResult = child.destroyIfNotInitDone() ;
        }

        adapterActivatorResult(status);

        if (status) {
            if (!adapterResult) {
                throw omgWrapper.adapterActivatorException(name, poaId);
            }
        } else {
            // OMG Issue 3740 is resolved to throw AdapterNonExistent if
            // unknown_adapter() returns false.
            throw new AdapterNonExistent();
        }
    }

    @InfoMethod
    private void locksWereHeld() {}

    @Poa
    private void cleanUpLocks( POAImpl child, boolean readLocked, boolean writeLocked,
        boolean childReadLocked ) {
        // Log an error if we ever get here with a lock held!
        if (readLocked || writeLocked || childReadLocked) {
            locksWereHeld();
            wrapper.findPOALocksNotReleased( readLocked, writeLocked,
                childReadLocked ) ;

            if (readLocked) {
                readUnlock() ;
            }

            if (writeLocked) {
                unlock() ;
            }

            if (childReadLocked && child != null) {
                child.readUnlock() ;
            }
        }
    }

    /**
     * <code>destroy</code>
     * <b>Section 3.3.8.4</b>
     */
    public void destroy(boolean etherealize, boolean wait_for_completion) 
    {
        // This is to avoid deadlock
        if (wait_for_completion && getORB().isDuringDispatch()) {
            throw wrapper.destroyDeadlock() ;
        }

        DestroyThread destroyer = new DestroyThread( etherealize );
        destroyer.doIt( this, wait_for_completion ) ;
    }

    /**
     * <code>create_thread_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public ThreadPolicy create_thread_policy(
        ThreadPolicyValue value) 
    {
        return new ThreadPolicyImpl(value);
    }

    /**
     * <code>create_lifespan_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public LifespanPolicy create_lifespan_policy(
        LifespanPolicyValue value) 
    {
        return new LifespanPolicyImpl(value);
    }

    /**
     * <code>create_id_uniqueness_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public IdUniquenessPolicy create_id_uniqueness_policy(
        IdUniquenessPolicyValue value) 
    {
        return new IdUniquenessPolicyImpl(value);
    }

    /**
     * <code>create_id_assignment_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public IdAssignmentPolicy create_id_assignment_policy(
        IdAssignmentPolicyValue value) 
    {
        return new IdAssignmentPolicyImpl(value);
    }

    /**
     * <code>create_implicit_activation_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public ImplicitActivationPolicy create_implicit_activation_policy(
        ImplicitActivationPolicyValue value) 
    {
        return new ImplicitActivationPolicyImpl(value);
    }

    /**
     * <code>create_servant_retention_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public ServantRetentionPolicy create_servant_retention_policy(
        ServantRetentionPolicyValue value) 
    {
        return new ServantRetentionPolicyImpl(value);
    }
    
    /**
     * <code>create_request_processing_policy</code>
     * <b>Section 3.3.8.5</b>
     */
    public RequestProcessingPolicy create_request_processing_policy(
        RequestProcessingPolicyValue value) 
    {
        return new RequestProcessingPolicyImpl(value);
    }
    
    /**
     * <code>the_name</code>
     * <b>Section 3.3.8.6</b>
     */
    @ManagedAttribute( id="POAName")
    @Description( "The name of this POA")
    public String the_name() 
    {
        try {
            lock() ;

            return poaName;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>the_parent</code>
     * <b>Section 3.3.8.7</b>
     */
    @ManagedAttribute( id="POAParent")
    @Description( "The parent of this POA")
    public POA the_parent() 
    {
        try {
            lock() ;

            return parent;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>the_children</code>
     */
    @ManagedAttribute( id="POAChildren")
    @Description( "The children of this POA")
    private List<POAImpl> children() {
        try {
            lock() ;
            return new ArrayList<POAImpl>( children.values() ) ;
        } finally {
            unlock() ;
        }
    }

    public org.omg.PortableServer.POA[] the_children() 
    {
        try {
            lock() ;

            Collection<POAImpl> coll = children.values() ;
            int size = coll.size() ;
            POA[] result = new POA[ size ] ;
            int index = 0 ;
            Iterator<POAImpl> iter = coll.iterator() ;
            while (iter.hasNext()) {
                POA poa = iter.next() ;
                result[ index ] = poa ;
                index++ ;
            }

            return result ;
        } finally {
            unlock() ;
        }
    }

    // We need this in order to return the correct type.
    // I'm not sure a covariant return could be used here.
    @ManagedAttribute( id="POAManager")
    @Description( "The POAManager of this POA")
    private POAManagerImpl getPOAManager() {
        try {
            lock() ;

            return manager;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>the_POAManager</code>
     * <b>Section 3.3.8.8</b>
     */
    public POAManager the_POAManager() 
    {
        try {
            lock() ;

            return manager;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>the_activator</code>
     * <b>Section 3.3.8.9</b>
     */
    @ManagedAttribute( id="Activator")
    @Description( "The AdapterActivator of this POA")
    public AdapterActivator the_activator() 
    {
        try {
            lock() ;

            return activator;
        } finally {
            unlock() ;
        }
    }
    
    /**
     * <code>the_activator</code>
     * <b>Section 3.3.8.9</b>
     */
    @Poa
    public void the_activator(AdapterActivator activator) 
    {
        try {
            lock() ;

            this.activator = activator;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>get_servant_manager</code>
     * <b>Section 3.3.8.10</b>
     */
    public ServantManager get_servant_manager() throws WrongPolicy 
    {
        try {
            lock() ;

            return mediator.getServantManager() ;
        } finally {
            unlock() ;
        }
    }

    @ManagedAttribute
    @Description( "The servant manager of this POA (may be null)")
    private ServantManager servantManager() {
        try {
            return get_servant_manager();
        } catch (WrongPolicy ex) {
            return null ;
        }
    }

    /**
     * <code>set_servant_manager</code>
     * <b>Section 3.3.8.10</b>
     */
    @Poa
    public void set_servant_manager(ServantManager servantManager)
        throws WrongPolicy 
    {
        try {
            lock() ;

            mediator.setServantManager( servantManager ) ;
        } finally {
            unlock() ;
        }
    }
        
    /**
     * <code>get_servant</code>
     * <b>Section 3.3.8.12</b>
     */
    public Servant get_servant() throws NoServant, WrongPolicy 
    {
        try {
            lock() ;

            return mediator.getDefaultServant() ;
        } finally {
            unlock() ;
        }
    }

    @ManagedAttribute
    @Description( "The default servant of this POA (may be null)")
    private Servant servant() {
        try {
            return get_servant();
        } catch (NoServant ex) {
            return null ;
        } catch (WrongPolicy ex) {
            return null ;
        }
    }

    /**
     * <code>set_servant</code>
     * <b>Section 3.3.8.13</b>
     */
    @Poa
    public void set_servant(Servant defaultServant)
        throws WrongPolicy 
    {
        try {
            lock() ;

            mediator.setDefaultServant( defaultServant ) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>activate_object</code>
     * <b>Section 3.3.8.14</b>
     */
    @Poa
    public byte[] activate_object(Servant servant)
        throws ServantAlreadyActive, WrongPolicy 
    {
        try {
            lock() ;

            // Allocate a new system-generated object-id.
            // This will throw WrongPolicy if not SYSTEM_ID
            // policy.
            byte[] id = mediator.newSystemId();

            try {
                mediator.activateObject( id, servant ) ;
            } catch (ObjectAlreadyActive oaa) {
                // This exception can not occur in this case,
                // since id is always brand new.
                // 
            }

            return id ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>activate_object_with_id</code>
     * <b>Section 3.3.8.15</b>
     */
    @Poa
    public void activate_object_with_id(byte[] id,
                                                     Servant servant)
        throws ObjectAlreadyActive, ServantAlreadyActive, WrongPolicy
    {
        try {
            lock() ;

            // Clone the id to avoid possible errors due to aliasing
            // (e.g. the client passes the id in and then changes it later).
            byte[] idClone = id.clone() ;

            mediator.activateObject( idClone, servant ) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>deactivate_object</code>
     * <b>3.3.8.16</b>
     */
    @Poa
    public void deactivate_object(byte[] id)
        throws ObjectNotActive, WrongPolicy 
    {
        try {
            lock() ;

            mediator.deactivateObject( id ) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>create_reference</code>
     * <b>3.3.8.17</b>
     */
    @Poa
    public org.omg.CORBA.Object create_reference(String repId)
        throws WrongPolicy 
    {
        try {
            lock() ;

            return makeObject( repId, mediator.newSystemId()) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>create_reference_with_id</code>
     * <b>3.3.8.18</b>
     */
    @Poa
    public org.omg.CORBA.Object
        create_reference_with_id(byte[] oid, String repId) 
    {
        try {
            lock() ;

            // Clone the id to avoid possible errors due to aliasing
            // (e.g. the client passes the id in and then changes it later).
            byte[] idClone = (oid.clone()) ;

            return makeObject( repId, idClone ) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>servant_to_id</code>
     * <b>3.3.8.19</b>
     */
    @Poa
    public byte[] servant_to_id(Servant servant)
        throws ServantNotActive, WrongPolicy 
    {
        try {
            lock() ;

            return mediator.servantToId( servant ) ;
        } finally {
            unlock() ;
        }
    }
                
    /**
     * <code>servant_to_reference</code>
     * <b>3.3.8.20</b>
     */
    @Poa
    public org.omg.CORBA.Object servant_to_reference(Servant servant)
        throws ServantNotActive, WrongPolicy 
    {
        try {
            lock() ;

            byte[] oid = mediator.servantToId(servant);
            String repId = servant._all_interfaces( this, oid )[0] ;
            return create_reference_with_id(oid, repId);
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>reference_to_servant</code>
     * <b>3.3.8.21</b>
     */
    @Poa
    public Servant reference_to_servant(org.omg.CORBA.Object reference)
        throws ObjectNotActive, WrongPolicy, WrongAdapter 
    {
        try {
            lock() ;

            if ( state >= STATE_DESTROYING ) {
                throw wrapper.adapterDestroyed() ;
            }

            // reference_to_id should throw WrongAdapter
            // if the objref was not created by this POA
            byte [] id = internalReferenceToId(reference);
            
            return mediator.idToServant( id ) ; 
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>reference_to_id</code>
     * <b>3.3.8.22</b>
     */
    @Poa
    public byte[] reference_to_id(org.omg.CORBA.Object reference)
        throws WrongAdapter, WrongPolicy 
    {
        try {
            lock() ;
            
            if( state >= STATE_DESTROYING ) {
                throw wrapper.adapterDestroyed() ;
            }
            
            return internalReferenceToId( reference ) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>id_to_servant</code>
     * <b>3.3.8.23</b>
     */
    @Poa
    public Servant id_to_servant(byte[] id)
        throws ObjectNotActive, WrongPolicy 
    {
        try {
            lock() ;
            
            if( state >= STATE_DESTROYING ) {
                throw wrapper.adapterDestroyed() ;
            }
            return mediator.idToServant( id ) ;
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>id_to_reference</code>
     * <b>3.3.8.24</b>
     */
    @Poa
    public org.omg.CORBA.Object id_to_reference(byte[] id)
        throws ObjectNotActive, WrongPolicy 

    {
        try {
            lock() ;
            
            if( state >= STATE_DESTROYING ) {
                throw wrapper.adapterDestroyed() ;
            }
            
            Servant s = mediator.idToServant( id ) ;
            String repId = s._all_interfaces( this, id )[0] ;
            return makeObject(repId, id );
        } finally {
            unlock() ;
        }
    }

    /**
     * <code>id</code>
     * <b>11.3.8.26 in ptc/00-08-06</b>
     */
    public byte[] id() 
    {
        try {
            lock() ;

            return getAdapterId() ;
        } finally {
            unlock() ;
        }
    }

    //***************************************************************
    //Implementation of ObjectAdapter interface
    //***************************************************************

    public Policy getEffectivePolicy( int type ) 
    {
        return mediator.getPolicies().get_effective_policy( type ) ;
    }

    public int getManagerId()
    {
        return manager.getManagerId() ;
    }

    public short getState()
    {
        return manager.getORTState() ;
    }

    public String[] getInterfaces( java.lang.Object servant, byte[] objectId )
    {
        Servant serv = (Servant)servant ;
        return serv._all_interfaces( this, objectId ) ;
    }

    protected ObjectCopierFactory getObjectCopierFactory()
    {
        int copierId = mediator.getPolicies().getCopierId() ;
        CopierManager cm = getORB().getCopierManager() ;
        return cm.getObjectCopierFactory( copierId ) ;
    }

    @Poa
    public void enter() throws OADestroyed
    {
        manager.enter();

        readLock() ;
        try {
            // Hold only the read lock to check the state
            if (state == STATE_RUN) {
                // fast path
                invocationCount.incrementAndGet();
                return ;
            }
        } finally {
            readUnlock();
        }

        // acquire lock: may need slow path
        lock() ;

        try {
            // Avoid deadlock if this is the thread that is processing the
            // POA.destroy because this is the only thread that can notify
            // waiters on beingDestroyedCV.  This can happen if an
            // etherealize upcall invokes a method on a colocated object
            // served by this POA.
            while ((state == STATE_DESTROYING) &&
                (isDestroying.get() == Boolean.FALSE)) {
                try {
                    beingDestroyedCV.await( 1, TimeUnit.SECONDS );
                } catch (InterruptedException ex) {
                    interruptedAwait( ex ) ;
                }
            }

            if (!waitUntilRunning()) {
                manager.exit() ;
                throw new OADestroyed() ;
            }

            invocationCount.incrementAndGet();
        } finally {
            unlock() ;
        }
    }

    @Poa
    public void exit() 
    {
        try {
            readLock() ;
            try {
                // Hold only a read lock to check the state
                if (state == STATE_RUN) {
                    // fast path
                    invocationCount.decrementAndGet();
                    return ;
                }
            } finally {
                readUnlock();
            }

            lock() ;
            try {
                if ((invocationCount.decrementAndGet() == 0)
                    && (state == STATE_DESTROYING)) {
                    invokeCV.signalAll();
                }
            } finally {
                unlock() ;
            }
        } finally {
            manager.exit();
        }

    }

    @ManagedAttribute
    @Description( "The current invocation count of this POA")
    @Poa
    private int getInvocationCount() {
        try {
            lock() ;
            return invocationCount.get() ;
        } finally {
            unlock() ;
        }
    }

    @Poa
    public void getInvocationServant( OAInvocationInfo info ) {
        // 6878245
        if (info == null) {
            return ;
        }

        java.lang.Object servant = null ;

        try {
            servant = mediator.getInvocationServant( info.id(),
                info.getOperation() );
        } catch (ForwardRequest freq) {
            throw new ForwardException( getORB(), freq.forward_reference ) ;
        }

        info.setServant( servant ) ;
    }

    public org.omg.CORBA.Object getLocalServant( byte[] objectId ) 
    {
        return null ;
    }

    /** Called from the subcontract to let this POA cleanup after an
     *  invocation. Note: If getServant was called, then returnServant
     *  MUST be called, even in the case of exceptions.  This may be
     *  called multiple times for a single request.
     */
    @Poa
    public void returnServant() {
        try {
            mediator.returnServant();
        } catch (Throwable thr) {
            if (thr instanceof Error) {
                throw (Error) thr;
            } else if (thr instanceof RuntimeException) {
                throw (RuntimeException)thr ;
            }
        } 
    }
}
