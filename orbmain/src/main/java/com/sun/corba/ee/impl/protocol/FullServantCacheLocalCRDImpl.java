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

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.oa.OADestroyed;
import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.trace.Subcontract;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.portable.ServantObject ;

@Subcontract
public class FullServantCacheLocalCRDImpl extends ServantCacheLocalCRDBase
{
    public FullServantCacheLocalCRDImpl( ORB orb, int scid, IOR ior ) 
    {
        super( orb, scid, ior ) ;
    }

    @Subcontract
    @Override
    public ServantObject internalPreinvoke( org.omg.CORBA.Object self,
        String operation, Class expectedType ) throws OADestroyed {

        OAInvocationInfo cachedInfo = getCachedInfo() ;
        if (!checkForCompatibleServant( cachedInfo, expectedType )) {
            return null;
        }

        // Note that info is shared across multiple threads
        // using the same subcontract, each of which may
        // have its own operation.  Therefore we need to clone it.
        OAInvocationInfo newInfo = new OAInvocationInfo( cachedInfo, operation ) ;
        newInfo.oa().enter() ;
        orb.pushInvocationInfo( newInfo ) ;
        return newInfo ;
    }

    @Subcontract
    public void servant_postinvoke(org.omg.CORBA.Object self,
        ServantObject servantobj) {
        try {
            OAInvocationInfo cachedInfo = getCachedInfo() ;
            cachedInfo.oa().exit() ;
        } catch (OADestroyed oades) {
            caughtOADestroyed() ;
            // ignore this: if I can't get the OA, I don't
            // need to call exit on it.
        } finally {
            orb.popInvocationInfo() ;
        }
    }

    @InfoMethod
    private void caughtOADestroyed() { }
}
