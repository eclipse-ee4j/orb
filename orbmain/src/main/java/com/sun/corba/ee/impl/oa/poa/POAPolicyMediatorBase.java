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

package com.sun.corba.ee.impl.oa.poa ;


import com.sun.corba.ee.impl.misc.ORBUtility ;
import com.sun.corba.ee.spi.extension.ServantCachingPolicy ;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.spi.logging.POASystemException;
import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.spi.orb.ORB ;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA.
 */
public abstract class POAPolicyMediatorBase implements POAPolicyMediator {
    protected static final POASystemException wrapper =
        POASystemException.self ;
    protected static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    protected POAImpl poa ;
    protected ORB orb ;

    private int sysIdCounter ;
    private Policies policies ;
    private DelegateImpl delegateImpl ;

    private int serverid ;
    private int scid ;

    protected boolean isImplicit ;
    protected boolean isUnique ;
    protected boolean isSystemId ;

    public final Policies getPolicies()
    {
        return policies ;
    }

    public final int getScid() 
    {
        return scid ;
    }

    public final int getServerId() 
    {
        return serverid ;
    }

    POAPolicyMediatorBase( Policies policies, POAImpl poa ) 
    {
        if (policies.isSingleThreaded()) {
            throw wrapper.singleThreadNotSupported();
        }

        POAManagerImpl poam = (POAManagerImpl)(poa.the_POAManager()) ;
        POAFactory poaf = poam.getFactory() ;
        delegateImpl = (DelegateImpl)(poaf.getDelegateImpl()) ;
        this.policies = policies ;
        this.poa = poa ;
        orb = poa.getORB() ;

        switch (policies.servantCachingLevel()) {
            case ServantCachingPolicy.NO_SERVANT_CACHING :
                scid = ORBConstants.TRANSIENT_SCID ;
                break ;
            case ServantCachingPolicy.FULL_SEMANTICS :
                scid = ORBConstants.SC_TRANSIENT_SCID ;
                break ;
            case ServantCachingPolicy.INFO_ONLY_SEMANTICS :
                scid = ORBConstants.IISC_TRANSIENT_SCID ;
                break ;
            case ServantCachingPolicy.MINIMAL_SEMANTICS :
                scid = ORBConstants.MINSC_TRANSIENT_SCID ;
                break ;
        }

        if ( policies.isTransient() ) {
            serverid = orb.getTransientServerId();
        } else {
            serverid = orb.getORBData().getPersistentServerId();
            scid = ORBConstants.makePersistent( scid ) ;
        }

        isImplicit = policies.isImplicitlyActivated() ;
        isUnique = policies.isUniqueIds() ;
        isSystemId = policies.isSystemAssignedIds() ;

        sysIdCounter = 0 ; 
    }
    
    public final java.lang.Object getInvocationServant( byte[] id, 
        String operation ) throws ForwardRequest
    {
        java.lang.Object result = internalGetServant( id, operation ) ;

        return result ;
    }

    // Create a delegate and stick it in the servant.
    // This delegate is needed during dispatch for the ObjectImpl._orb()
    // method to work.
    protected final void setDelegate(Servant servant, byte[] id) 
    {
        //This new servant delegate no longer needs the id for 
        // its initialization.
        servant._set_delegate(delegateImpl);
    }

    public synchronized byte[] newSystemId() throws WrongPolicy
    {
        if (!isSystemId) {
            throw new WrongPolicy();
        }

        byte[] array = new byte[8];
        ORBUtility.intToBytes(++sysIdCounter, array, 0);
        ORBUtility.intToBytes( poa.getPOAId(), array, 4);
        return array;
    }

    protected abstract  java.lang.Object internalGetServant( byte[] id, 
        String operation ) throws ForwardRequest ;
}
