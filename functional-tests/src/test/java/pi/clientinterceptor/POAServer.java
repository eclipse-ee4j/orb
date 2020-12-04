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

package pi.clientinterceptor;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableInterceptor.*;
import corba.framework.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.interceptors.*;

import java.util.*;
import java.io.*;

import ClientRequestInterceptor.*; // hello interface

public class POAServer 
    implements InternalProcess 
{
    // Set from run()
    private PrintStream out;
    
    private static final String ROOT_POA = "RootPOA";
    
    private POA rootPOA;
    
    private com.sun.corba.ee.spi.orb.ORB orb;

    public static void main(String args[]) {
        try {
            (new POAServer()).run( System.getProperties(),
                                args, System.out, System.err, null );
        }
        catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        this.out = out;

        out.println( "Invoking ORB" );
        out.println( "============" );

        // create and initialize the ORB
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        ORB orb = ORB.init(args, props);
        this.orb = (com.sun.corba.ee.spi.orb.ORB)orb;

        // Get the root POA:
        rootPOA = null;
        out.println( "Obtaining handle to root POA and activating..." );
        try {
            rootPOA = (POA)orb.resolve_initial_references( ROOT_POA );
        }
        catch( InvalidName e ) {
            err.println( ROOT_POA + " is an invalid name." );
            throw e;
        }
        rootPOA.the_POAManager().activate();
        
        // Set up hello object and helloForward object for POA remote case:
        createAndBind( "Hello1" );
        createAndBind( "Hello1Forward" );
        
        //handshake:
        out.println("Server is ready.");
        out.flush();

        // wait for invocations from clients
        java.lang.Object sync = new java.lang.Object();
        synchronized (sync) {
            sync.wait();
        }

    }
    
    /**
     * Implementation borrowed from corba.socket.HelloServer test
     */
    public void createAndBind (String name)
        throws Exception
    {
        // create servant and register it with the ORB
        helloServant helloRef = new helloServant( out );
      
        byte[] id = rootPOA.activate_object(helloRef);
        org.omg.CORBA.Object ref = rootPOA.id_to_reference(id);
      
        // get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
        // bind the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
            
        ncRef.rebind(path, ref);
    }

}
