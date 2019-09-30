/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.cosnaming;

// Get CORBA type

import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

// Get org.omg.CosNaming types

// Import transient naming context
import com.sun.corba.ee.spi.misc.ORBConstants;


import com.sun.corba.ee.spi.logging.NamingSystemException;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.trace.Naming;

/**
 * Class TransientNameService implements a transient name service
 * using TransientNamingContexts and TransientBindingIterators, which
 * implement the org.omg.CosNaming::NamingContext and org.omg.CosNaming::BindingIterator
 * interfaces specfied by the OMG Common Object Services Specification.
 * <p>
 * The TransientNameService creates the initial NamingContext object.
 * @see NamingContextImpl
 * @see BindingIteratorImpl
 * @see TransientNamingContext
 * @see TransientBindingIterator
 */
@Naming
public class TransientNameService
{
    private static final NamingSystemException wrapper =
        NamingSystemException.self ;

    /**
     * Constructs a new TransientNameService, and creates an initial
     * NamingContext, whose object
     * reference can be obtained by the initialNamingContext method.
     * @param orb The ORB object
     * @exception org.omg.CORBA.INITIALIZE Thrown if
     * the TransientNameService cannot initialize.
     */
    public TransientNameService(com.sun.corba.ee.spi.orb.ORB orb )
        throws org.omg.CORBA.INITIALIZE
    {
        // Default constructor uses "NameService" as the key for the Root Naming
        // Context. If default constructor is used then INS's object key for
        // Transient Name Service is "NameService"
        initialize( orb, "NameService" );
    }

    /**
     * Constructs a new TransientNameService, and creates an initial
     * NamingContext, whose object
     * reference can be obtained by the initialNamingContext method.
     * @param orb The ORB object
     * @param serviceName Stringified key used for INS Service registry
     * @exception org.omg.CORBA.INITIALIZE Thrown if
     * the TransientNameService cannot initialize.
     */
    public TransientNameService(com.sun.corba.ee.spi.orb.ORB orb,
        String serviceName ) throws org.omg.CORBA.INITIALIZE
    {
        // This constructor gives the flexibility of providing the Object Key
        // for the Root Naming Context that is registered with INS.
        initialize( orb, serviceName );
    }


    /** 
     * This method initializes Transient Name Service by associating Root 
     * context with POA and registering the root context with INS Object Keymap.
     */
    @Naming
    private void initialize( com.sun.corba.ee.spi.orb.ORB orb,
        String nameServiceName )
        throws org.omg.CORBA.INITIALIZE
    {
        try {
            POA rootPOA = (POA) orb.resolve_initial_references( 
                ORBConstants.ROOT_POA_NAME );
            rootPOA.the_POAManager().activate();

            int i = 0;
            Policy[] poaPolicy = new Policy[3];
            poaPolicy[i++] = rootPOA.create_lifespan_policy(
                LifespanPolicyValue.TRANSIENT);
            poaPolicy[i++] = rootPOA.create_id_assignment_policy(
                IdAssignmentPolicyValue.SYSTEM_ID);
            poaPolicy[i++] = rootPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.RETAIN);

            POA nsPOA = rootPOA.create_POA( "TNameService", null, poaPolicy );
            ObjectAdapter.class.cast( nsPOA ).setNameService(true);
            nsPOA.the_POAManager().activate();

            // Create an initial context
            TransientNamingContext initialContext =
                new TransientNamingContext(orb, null, nsPOA);
            byte[] rootContextId = nsPOA.activate_object( initialContext );
            initialContext.localRoot =
                nsPOA.id_to_reference( rootContextId );
            theInitialNamingContext = initialContext.localRoot;
            orb.register_initial_reference( nameServiceName, 
                theInitialNamingContext );
        } catch (org.omg.CORBA.SystemException e) {
            throw wrapper.transNsCannotCreateInitialNcSys( e ) ;
        } catch (Exception e) {
            throw wrapper.transNsCannotCreateInitialNc( e ) ;
        } 
    }


    /**
     * Return the initial NamingContext.
     * @return the object reference for the initial NamingContext.
     */
    public org.omg.CORBA.Object initialNamingContext()
    {
        return theInitialNamingContext;
    }


    // The initial naming context for this name service
    private org.omg.CORBA.Object theInitialNamingContext;
}
