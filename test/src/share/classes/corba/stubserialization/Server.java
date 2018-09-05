/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.stubserialization;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;

import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import org.omg.PortableServer.*;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
                                                                                
import com.sun.corba.ee.spi.misc.ORBConstants ;


/**
 * This is a Server that uses Dynamic RMI IIOP Tie model. A Simple Server 
 * with 1 Servant that is associated with the RootPOA.
 */
public class Server {

    public static void main(String[] args ) {
        try {

            ORB orb = ORB.init( args, null );

            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
 
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            NameComponent nc = new NameComponent(Constants.HELLO_SERVICE, "");
            NameComponent path[] = {nc};

            POA rootPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rootPOA.the_POAManager().activate();
            
            byte[] id = Constants.HELLO_SERVICE.getBytes();
            rootPOA.activate_object_with_id(id, 
                (Servant)makeHelloServant((com.sun.corba.ee.spi.orb.ORB)orb));
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id );
                                                                                
            ncRef.rebind(path, obj);

            // wait for invocations from clients
            System.out.println("Server is ready.");
            System.out.flush();

            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) { sync.wait(); }
        } catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace( );
        }
    }

    static Tie makeHelloServant( com.sun.corba.ee.spi.orb.ORB orb ) {
        try {
            HelloServant servant = new HelloServant( orb );

            Tie tie = orb.getPresentationManager().getTie();
            tie.orb( orb ) ;
            tie.setTarget( (java.rmi.Remote)servant );
            return tie;
        } catch( Exception e ) {
            e.printStackTrace( );
            System.exit( -1 );
        }
        return null;
    }

}

