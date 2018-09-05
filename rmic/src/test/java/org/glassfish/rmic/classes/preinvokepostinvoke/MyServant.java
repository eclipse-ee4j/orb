/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.preinvokepostinvoke;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class MyServant extends PortableRemoteObject implements Interface
{
    private org.omg.CORBA.ORB orb;
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

