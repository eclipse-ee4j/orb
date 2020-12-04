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

