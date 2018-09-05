/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import org.omg.CORBA.portable.ServantObject ;

import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.spi.oa.OADestroyed;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcherFactory ;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher ;

import com.sun.corba.ee.spi.ior.IOR;

public class InfoOnlyServantCacheLocalCRDImpl extends ServantCacheLocalCRDBase
{
    public InfoOnlyServantCacheLocalCRDImpl( ORB orb, int scid, IOR ior ) 
    {
        super( (com.sun.corba.ee.spi.orb.ORB)orb, scid, ior ) ;
    }

    public ServantObject internalPreinvoke( org.omg.CORBA.Object self,
        String operation, Class expectedType ) throws OADestroyed
    {
        OAInvocationInfo cachedInfo = getCachedInfo() ;
        if (!checkForCompatibleServant( cachedInfo, expectedType ))
            return null ;

        // Note that info is shared across multiple threads
        // using the same subcontract, each of which may
        // have its own operation.  Therefore we need to copy it.
        OAInvocationInfo info =  new OAInvocationInfo(cachedInfo, operation) ;
        orb.pushInvocationInfo( info ) ;

        return info ;
    }

    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servantobj) 
    {
        orb.popInvocationInfo() ;
    }
}
