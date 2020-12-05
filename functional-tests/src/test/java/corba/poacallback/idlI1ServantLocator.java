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

package corba.poacallback;


import org.omg.CORBA.ORB;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

public class idlI1ServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public ORB orb;

    public idlI1ServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
    {
        System.out.println( "idlI1ServantLocator.preinvoke called for " + 
            operation );
        System.out.flush( );
         
        if( operation.equals("o1") ) {
            try {
                org.omg.CORBA.Object obj = 
                    orb.resolve_initial_references( "NameService" );
                org.omg.CosNaming.NamingContextExt nctx = 
                    org.omg.CosNaming.NamingContextExtHelper.narrow( obj );
                obj = nctx.resolve_str( "idlI2" );
                idlI2 i2 = idlI2Helper.narrow( obj );
                i2.o( "Call From idlI1ServantLocator" );
            } catch ( Exception e ) {
                e.printStackTrace( );
            } 
        } 

        return new idlI1Servant( );
    }

 
    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
    }
}


