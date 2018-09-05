/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.preinvokepostinvoke;


import org.omg.CORBA.ORB;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

public class MyServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public ORB orb;

    public MyServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
    {
        System.out.println( "MyServantLocator.preinvoke called " ); 

        TestAssert.preinvokeCalled( );
       
        try {
            return (Servant)javax.rmi.CORBA.Util.getTie(new MyServant( orb ));
        } catch( Throwable e ) {
            System.err.println( "Exception in MyServantLocator..." + e );
            e.printStackTrace( );
        }
        return null;
    }

 
    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
        System.out.println( "MyServantLocator.postinvoke called " ); 

        TestAssert.postinvokeCalled( );
    }
}


