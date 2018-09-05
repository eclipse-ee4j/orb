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


import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.NoServant ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;


import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.impl.oa.NullServantImpl ;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA.
 */
public class POAPolicyMediatorImpl_NR_USM extends POAPolicyMediatorBase {
    // XXX How do we protect locator from multi-threaded access?
    private ServantLocator locator ;

    POAPolicyMediatorImpl_NR_USM( Policies policies, POAImpl poa ) 
    {
        super( policies, poa ) ;

        // assert !policies.retainServants() && policies.useServantManager()
        if (policies.retainServants()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }

        if (!policies.useServantManager()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }

        locator = null ;
    }
    
    protected java.lang.Object internalGetServant( byte[] id, 
        String operation ) throws ForwardRequest
    { 
        if (locator == null) {
            throw wrapper.poaNoServantManager();
        }
    
        CookieHolder cookieHolder = orb.peekInvocationInfo().getCookieHolder() ;

        java.lang.Object servant = locator.preinvoke(id, poa, operation,
            cookieHolder);

        if (servant == null) {
            servant = new NullServantImpl(omgWrapper.nullServantReturned());
        } else {
            setDelegate((Servant) servant, id);
        }


        return servant;
    }

    public void returnServant() 
    {
        OAInvocationInfo info = orb.peekInvocationInfo();

        // 6878245: added info == null check.
        if (locator == null || info == null) {
            return;
        }

        locator.postinvoke(info.id(), (POA)(info.oa()),
            info.getOperation(), info.getCookieHolder().value,
            (Servant)(info.getServantContainer()) );
    }

    public void etherealizeAll() 
    {   
        // NO-OP
    }

    public void clearAOM() 
    {
        // NO-OP
    }

    public ServantManager getServantManager() throws WrongPolicy
    {
        return locator ;
    }

    public void setServantManager( ServantManager servantManager ) throws WrongPolicy
    {
        if (locator != null) {
            throw wrapper.servantManagerAlreadySet();
        }

        if (servantManager instanceof ServantLocator) {
            locator = (ServantLocator) servantManager;
        } else {
            throw wrapper.servantManagerBadType();
        }
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy 
    {
        throw new WrongPolicy();
    }

    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
        throw new WrongPolicy();
    }

    public final void activateObject(byte[] id, Servant servant) 
        throws WrongPolicy, ServantAlreadyActive, ObjectAlreadyActive
    {
        throw new WrongPolicy();
    }

    public Servant deactivateObject( byte[] id ) throws ObjectNotActive, WrongPolicy 
    {
        throw new WrongPolicy();
    }

    public byte[] servantToId( Servant servant ) throws ServantNotActive, WrongPolicy
    {   
        throw new WrongPolicy();
    }

    public Servant idToServant( byte[] id ) 
        throws WrongPolicy, ObjectNotActive
    {
        throw new WrongPolicy();
    }
}
