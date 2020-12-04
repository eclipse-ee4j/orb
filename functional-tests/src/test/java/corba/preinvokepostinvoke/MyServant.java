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

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;
import org.omg.CORBA.ORB ;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

class MyServant extends PortableRemoteObject implements Interface
{
    org.omg.CORBA.ORB orb;
    public MyServant( org.omg.CORBA.ORB orb) throws RemoteException
    {
        this.orb = orb;
    }

    public String o1(String arg)
    {
        System.out.println( "Interface.o1 called with " + arg );
        System.out.flush( );
        try {
            org.omg.CORBA.Object objRef =
                 orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef); 
            objRef = ncRef.resolve_str( "Instance2" );
            Interface i2 = 
                (Interface) PortableRemoteObject.narrow( objRef, Interface.class );
            i2.o2( "Invoking from Interface.o1..." );
        } catch( Throwable e ) {
            System.err.println( " Exception ..." + e );
            e.printStackTrace( );
            System.exit( 1 );
        }
        return "return value for interface.o1";
    }

    public String o2( String arg ) 
    {
        System.out.println( "Interface.o2 called with " + arg );
        System.out.flush( );
        return "return value for interface.o2";
    }
}

