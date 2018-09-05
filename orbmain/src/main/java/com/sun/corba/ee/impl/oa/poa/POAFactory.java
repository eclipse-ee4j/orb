/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.poa ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.WeakHashMap ;

import org.omg.CORBA.OBJECT_NOT_EXIST ;
import org.omg.CORBA.TRANSIENT ;

import org.omg.CORBA.ORBPackage.InvalidName ;

import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.POAManager ;

import com.sun.corba.ee.spi.oa.ObjectAdapter ;
import com.sun.corba.ee.spi.oa.ObjectAdapterFactory ;

import com.sun.corba.ee.spi.ior.ObjectAdapterId ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.logging.POASystemException ;
import com.sun.corba.ee.spi.logging.OMGSystemException ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.pfl.basic.func.NullaryFunction;

@ManagedObject
@Description( "The factory for all POAs and POAManagers")
@AMXMetadata( isSingleton=true )
public class POAFactory implements ObjectAdapterFactory 
{
    private static final POASystemException wrapper =
        POASystemException.self ;
    private static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    // Maps servants to POAs for deactivating servants when unexportObject is called.
    // Maintained by POAs activate_object and deactivate_object.
    private Map<Servant,POA> exportedServantsToPOA = new WeakHashMap<Servant,POA>();

    private java.util.concurrent.ConcurrentHashMap<POAManager,Boolean> poaManagers ;

    private int poaManagerId ;
    private int poaId ;
    private POAImpl rootPOA ;
    private DelegateImpl delegateImpl;
    private ORB orb ;
    private boolean isShuttingDown = false ;
    private ManagedObjectManager mom ;

    public POASystemException getWrapper() 
    {
        return wrapper ;
    }

    /** All object adapter factories must have a no-arg constructor.
    */
    public POAFactory( )
    {
        poaManagers = new java.util.concurrent.ConcurrentHashMap<POAManager,Boolean>(4);
        poaManagerId = 0 ;
        poaId = 0 ;
        rootPOA = null ;
        delegateImpl = null ;
        orb = null ;
    }

    @ManagedData
    @Description( "A servant registered with a particular POA" )
    public static class ServantPOAPair {
        private Servant servant ;
        private POAImpl poa ;

        public ServantPOAPair( Servant servant, POAImpl poa ) {
            this.servant = servant ;
            this.poa = poa ;
        }

        @ManagedAttribute 
        @Description( "Servant" ) 
        Servant getServant() { return servant ; }

        @ManagedAttribute
        @Description( "POA for Servant" ) 
        POAImpl getPOA() { return poa ; } 
    }

    @ManagedAttribute
    @Description( "The servants managed by a particular POA" )
    private synchronized List<ServantPOAPair> getExportedServants() {
        List<ServantPOAPair> result = new ArrayList<ServantPOAPair>() ;
        for (Map.Entry<Servant,POA> entry : exportedServantsToPOA.entrySet()) {
            POAImpl pimpl = (POAImpl)entry.getValue() ;
            result.add( new ServantPOAPair( entry.getKey(), pimpl ) ) ;
        }
        return result ;
    }

    @ManagedAttribute
    @Description( "The POAManagers")
    private synchronized Set<POAManager> getPOAManagers() {
        return new HashSet<POAManager>( poaManagers.keySet() ) ;
    }

    @ManagedAttribute
    @Description( "The last allocated POAManager id")
    private synchronized int getPOAManagerId() {
        return poaManagerId ;
    }

    @ManagedAttribute
    @Description( "The last allocated POAManager id")
    private synchronized int getPOAId() {
        return poaId ;
    }

    @ManagedAttribute( id = "RootPOA" )
    @Description( "The root POA")
    private synchronized POAImpl getDisplayRootPOA() {
        return rootPOA ;
    }

    public synchronized POA lookupPOA (Servant servant) 
    {
        return exportedServantsToPOA.get(servant);
    }

    public synchronized void registerPOAForServant(POA poa, Servant servant) 
    {
        exportedServantsToPOA.put(servant, poa);
    }

    public synchronized void unregisterPOAForServant(POA poa, Servant servant) 
    {
        exportedServantsToPOA.remove(servant);
    }

// Implementation of ObjectAdapterFactory interface

    public void init( ORB orb ) 
    {
        this.orb = orb ;
        delegateImpl = new DelegateImpl( orb, this ) ;
        registerRootPOA() ;

        POACurrent poaCurrent = new POACurrent(orb);
        orb.getLocalResolver().register( ORBConstants.POA_CURRENT_NAME, 
            NullaryFunction.Factory.makeConstant( 
                (org.omg.CORBA.Object)poaCurrent ) ) ;
        this.mom = orb.mom() ;
        mom.registerAtRoot( this ) ;
    }

    public ObjectAdapter find( ObjectAdapterId oaid )
    {
        POA poa=null;
        try {
            boolean first = true ;
            Iterator iter = oaid.iterator() ;
            poa = getRootPOA();
            while (iter.hasNext()) {
                String name = (String)(iter.next()) ;

                if (first) {
                    if (!name.equals( ORBConstants.ROOT_POA_NAME )) {
                        throw wrapper.makeFactoryNotPoa(name);
                    }
                    first = false ;
                } else {
                    poa = poa.find_POA( name, true ) ;
                }
            }
        } catch ( org.omg.PortableServer.POAPackage.AdapterNonExistent ex ){
            throw omgWrapper.noObjectAdaptor( ex ) ;
        } catch ( OBJECT_NOT_EXIST ex ) {
            throw ex;
        } catch ( TRANSIENT ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw wrapper.poaLookupError( ex ) ;
        }

        if ( poa == null ) {
            throw wrapper.poaLookupError();
        }

        return (ObjectAdapter)poa;
    }

    public void shutdown( boolean waitForCompletion )
    {
        // It is important to copy the list of POAManagers first because 
        // pm.deactivate removes itself from poaManagers!
        Iterator<POAManager> managers = null ;
        synchronized (this) {
            isShuttingDown = true ;
            managers = poaManagers.keySet().iterator();
        }

        while ( managers.hasNext() ) {
            try {
                managers.next().deactivate(true, waitForCompletion);
            } catch ( org.omg.PortableServer.POAManagerPackage.AdapterInactive e ) {}
        }
    }

// Special methods used to manipulate global POA related state

    public synchronized void removePoaManager( POAManager manager ) 
    {
        poaManagers.remove(manager);
        mom.unregister( manager ) ;
    }

    public void addPoaManager( POAManager manager )
    {
        poaManagers.putIfAbsent(manager, Boolean.TRUE); // Store the key. Value is dummy
    }

    synchronized public int newPOAManagerId()
    {
        return poaManagerId++ ;
    }

    public void registerRootPOA()
    {
        // We delay the evaluation of makeRootPOA until
        // a call to resolve_initial_references( "RootPOA" ).
        // The Future guarantees that makeRootPOA is only called once.
        NullaryFunction<org.omg.CORBA.Object> rpClosure =
            new NullaryFunction<org.omg.CORBA.Object>() {
                public org.omg.CORBA.Object evaluate() {
                    return POAImpl.makeRootPOA( orb ) ;
                }
            } ;

        orb.getLocalResolver().register( ORBConstants.ROOT_POA_NAME, 
            NullaryFunction.Factory.makeFuture( rpClosure ) ) ;
    }

    public synchronized POA getRootPOA()
    {
        if (rootPOA == null) {
            if (isShuttingDown) {
                throw omgWrapper.noObjectAdaptor() ;
            }

            try {
                Object obj = orb.resolve_initial_references(
                    ORBConstants.ROOT_POA_NAME ) ;
                rootPOA = (POAImpl)obj ;
            } catch (InvalidName inv) {
                throw wrapper.cantResolveRootPoa( inv ) ;
            } 
        }

        return rootPOA;
    }

    public org.omg.PortableServer.portable.Delegate getDelegateImpl() 
    {
        return delegateImpl ;
    }

    synchronized public int newPOAId()
    {
        return poaId++ ;
    }

    public ORB getORB() 
    {
        return orb ;
    }
} 
