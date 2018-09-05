/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poacallback;


import org.omg.CORBA.ORB;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;


public class idlI2ServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public ORB orb;

    public idlI2ServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
    {
        System.out.println( "idlI2ServantLocator.preinvoke called for " +
            operation );
        System.out.flush( );
        try { 
            org.omg.CORBA.Object obj = 
                orb.resolve_initial_references( "NameService" );
            org.omg.CosNaming.NamingContextExt nctx = 
                org.omg.CosNaming.NamingContextExtHelper.narrow( obj );
            obj = nctx.resolve_str( "idlI1" );
            idlI1 i1 = idlI1Helper.narrow( obj );
            i1.o2( "Call From idlI2ServantLocator" );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        return new idlI2Servant( );
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
    }
}


