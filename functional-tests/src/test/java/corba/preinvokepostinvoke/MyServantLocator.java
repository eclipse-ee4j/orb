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


