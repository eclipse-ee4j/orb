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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.oa.OADestroyed;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.ForwardException;
import com.sun.corba.ee.spi.trace.Subcontract;

@Subcontract
public abstract class ServantCacheLocalCRDBase 
    extends LocalClientRequestDispatcherBase {

    private OAInvocationInfo cachedInfo ;

    protected ServantCacheLocalCRDBase( ORB orb, int scid, IOR ior )
    {
        super( orb, scid, ior ) ;
    }

    @Subcontract
    protected void cleanupAfterOADestroyed() {
        cachedInfo = null ;
    }

    @Subcontract
    protected synchronized OAInvocationInfo getCachedInfo() throws OADestroyed {
        if (!servantIsLocal) {
            throw poaWrapper.servantMustBeLocal() ;
        }

        if (cachedInfo == null) {
            updateCachedInfo() ;
        }

        return cachedInfo ;
    }

    @Subcontract
    private void updateCachedInfo() throws OADestroyed {
        // If find throws an exception, just let it propagate out
        ObjectAdapter oa = oaf.find( oaid ) ;
        cachedInfo = oa.makeInvocationInfo( objectId ) ;
        oa.enter( );

        // InvocationInfo must be pushed before calling getInvocationServant
        orb.pushInvocationInfo( cachedInfo ) ;

        try {
            oa.getInvocationServant( cachedInfo ) ;
        } catch (ForwardException freq) {
            throw poaWrapper.illegalForwardRequest( freq ) ;
        } finally {
            oa.returnServant();
            oa.exit();
            orb.popInvocationInfo() ;
        }

        return ;
    }
}

// End of File
