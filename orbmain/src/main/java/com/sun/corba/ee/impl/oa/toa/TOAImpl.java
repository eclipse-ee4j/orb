/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.toa ;

import org.omg.CORBA.Policy ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableInterceptor.ACTIVE;

import com.sun.corba.ee.spi.copyobject.CopierManager ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.spi.oa.OADestroyed ;
import com.sun.corba.ee.spi.oa.ObjectAdapterBase ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher ;
import com.sun.corba.ee.spi.transport.ContactInfoList ;

import com.sun.corba.ee.impl.ior.JIDLObjectKeyTemplate ;
import com.sun.corba.ee.impl.oa.NullServantImpl;
import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.impl.protocol.JIDLLocalCRDImpl ;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import java.util.concurrent.atomic.AtomicLong;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.NameValue;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

/** The Transient Object Adapter (TOA) represents the OA for purely transient
* objects.  It is used for standard RMI-IIOP as well as backwards compatible
* server support (i.e. the ORB.connect() method)
* Its characteristics include:
* <UL>
* <LI>There is only one OA instance of the TOA.  Its OAId is { "TOA" }</LI>
* <LI>There is not adapter manager.  The TOA manager ID is fixed.<LI>
* <LI>State is the same as ORB state (TBD)</LI>
* </UL>
* Other requirements:
* <UL>
* <LI>All object adapters must invoke ORB.adapterCreated when they are created.
* </LI>
* <LI>All adapter managers must invoke ORB.adapterManagerStateChanged when
* their state changes, mapping the internal state to an ORT state.</LI>
* <LI>AdapterStateChanged must be invoked (from somewhere) whenever
* an adapter state changes that is not due to an adapter manager state change.</LI>
* </UL>
*/
@ManagedObject
@Description( "The Transient Object Adapter")
public class TOAImpl extends ObjectAdapterBase implements TOA 
{
    private static AtomicLong currentId = new AtomicLong( 0 );

    private TransientObjectManager servants ;
    private long id ;
    private String codebase ;

    @NameValue
    private long getId() {
        return id ;
    }

    @ManagedAttribute
    @Description( "The codebase used to create this TOA")
    private String getCodebase() {
        return codebase ;
    }

    @ManagedAttribute
    @Description( "The TransientObjectManager")
    private TransientObjectManager getTransientObjectManager() {
        return servants ;
    }

    public TOAImpl( ORB orb, TransientObjectManager tom, String codebase ) 
    {
        super( orb ) ;
        servants = tom ;
        this.codebase = codebase ;
        id = currentId.getAndIncrement() ;

        // Make the object key template
        int serverid = (getORB()).getTransientServerId();
        int scid = ORBConstants.TOA_SCID ;

        ObjectKeyTemplate oktemp = new JIDLObjectKeyTemplate( orb, scid, serverid ) ;

        // REVISIT - POA specific
        Policies policies = Policies.defaultPolicies;

        // REVISIT - absorb codebase into a policy
        initializeTemplate( oktemp, true,
                            policies, 
                            codebase,
                            null, // manager id
                            oktemp.getObjectAdapterId()
                            ) ;
    }

    // Methods required for dispatching requests

    public ObjectCopierFactory getObjectCopierFactory()
    {
        CopierManager cm = getORB().getCopierManager() ;
        return cm.getDefaultObjectCopierFactory() ;
    }

    public org.omg.CORBA.Object getLocalServant( byte[] objectId ) 
    {
        return (org.omg.CORBA.Object)(servants.lookupServant( objectId ) ) ;
    }

    /** Get the servant for the request given by the parameters. 
    * This will update thread Current, so that subsequent calls to
    * returnServant and removeCurrent from the same thread are for the
    * same request.
    * @param request is the request containing the rest of the request
    */
    public void getInvocationServant( OAInvocationInfo info ) 
    {
        java.lang.Object servant = servants.lookupServant( info.id() ) ;
        if (servant == null) {
            servant =
                new NullServantImpl(wrapper.nullServant());
        }
        info.setServant( servant ) ;
    }

    public void returnServant()
    {
        // NO-OP
    }

    /** Return the most derived interface for the given servant and objectId.
    */
    public String[] getInterfaces( Object servant, byte[] objectId ) 
    {
        return StubAdapter.getTypeIds( servant ) ;
    }

    public Policy getEffectivePolicy( int type ) 
    {
        return null ;
    }

    public int getManagerId() 
    {
        return -1 ;
    }

    public short getState() 
    {
        return ACTIVE.value ;
    }

    public void enter() throws OADestroyed
    {
    }

    public void exit() 
    {
    }
 
    // Methods unique to the TOA

    public void connect( org.omg.CORBA.Object objref) 
    {
        // Store the objref and get a userkey allocated by the transient
        // object manager.
        byte[] key = servants.storeServant(objref, null);

        // Find out the repository ID for this objref.
        String id = StubAdapter.getTypeIds( objref )[0] ;

        // Create the new objref
        ObjectReferenceFactory orf = getCurrentFactory() ;
        org.omg.CORBA.Object obj = orf.make_object( id, key ) ;

        // Copy the delegate from the new objref to the argument
        org.omg.CORBA.portable.Delegate delegate = StubAdapter.getDelegate( 
            obj ) ;
        ContactInfoList ccil = ((ClientDelegate) delegate).getContactInfoList() ;
        LocalClientRequestDispatcher lcs = 
            ccil.getLocalClientRequestDispatcher() ;

        if (lcs instanceof JIDLLocalCRDImpl) {
            JIDLLocalCRDImpl jlcs = (JIDLLocalCRDImpl)lcs ;
            jlcs.setServant( objref ) ;
        } else {        
            throw new RuntimeException( 
                "TOAImpl.connect can not be called on " + lcs ) ;
        }

        StubAdapter.setDelegate( objref, delegate ) ;
    }

    public void disconnect( org.omg.CORBA.Object objref ) 
    {
        // Get the delegate, then ior, then transientKey, then delete servant
        org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate( 
            objref ) ; 
        ContactInfoList ccil = ((ClientDelegate) del).getContactInfoList() ;
        LocalClientRequestDispatcher lcs = 
            ccil.getLocalClientRequestDispatcher() ;

        if (lcs instanceof JIDLLocalCRDImpl) {
            JIDLLocalCRDImpl jlcs = (JIDLLocalCRDImpl)lcs ;
            byte[] oid = jlcs.getObjectId() ;
            servants.deleteServant(oid);
            jlcs.unexport() ;
        } else {        
            throw new RuntimeException( 
                "TOAImpl.disconnect can not be called on " + lcs ) ;
        }
    }
} 
