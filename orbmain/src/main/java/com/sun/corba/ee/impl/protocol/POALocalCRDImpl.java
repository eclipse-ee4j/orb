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
import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Subcontract;

import org.omg.CORBA.portable.ServantObject;

@Subcontract
public class POALocalCRDImpl extends LocalClientRequestDispatcherBase {

    public POALocalCRDImpl( ORB orb, int scid, IOR ior) {
        super( orb, scid, ior );
    }

    @Subcontract
    private OAInvocationInfo servantEnter( ObjectAdapter oa ) throws OADestroyed {
        oa.enter() ;

        OAInvocationInfo info = oa.makeInvocationInfo( objectId ) ;
        orb.pushInvocationInfo( info ) ;

        return info ;
    }

    @Subcontract
    private void servantExit( ObjectAdapter oa ) {
        try {
            oa.returnServant();
        } finally {
            oa.exit() ;
            orb.popInvocationInfo() ; 
        }
    }

    // Look up the servant for this request and return it in a 
    // ServantObject.  Note that servant_postinvoke is always called
    // by the stub UNLESS this method returns null.  However, in all
    // cases we must be sure that ObjectAdapter.getServant and
    // ObjectAdapter.returnServant calls are paired, as required for
    // Portable Interceptors and Servant Locators in the POA.
    // Thus, this method must call returnServant if it returns null.
    @Subcontract
    @Override
    public ServantObject internalPreinvoke( org.omg.CORBA.Object self,
        String operation, Class expectedType) throws OADestroyed {

        ObjectAdapter oa = null ;

        oa = oaf.find( oaid ) ;

        OAInvocationInfo info = servantEnter( oa ) ;
        info.setOperation( operation ) ;

        try {
            oa.getInvocationServant( info );
            if (!checkForCompatibleServant( info, expectedType )) {
                servantExit( oa ) ;
                return null ;
            }

            return info ;
        } catch (Error err) {
            // Cleanup after this call, then throw to allow
            // outer try to handle the exception appropriately.
            servantExit( oa ) ;
            throw err ;
        } catch (RuntimeException re) {
            // Cleanup after this call, then throw to allow
            // outer try to handle the exception appropriately.
            servantExit( oa ) ;
            throw re ;
        }
    }

    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servantobj) 
    {
        ObjectAdapter oa = orb.peekInvocationInfo().oa() ; 
        servantExit( oa ) ;     
    }
}

// End of file.
