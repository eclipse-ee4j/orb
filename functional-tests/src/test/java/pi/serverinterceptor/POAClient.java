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

package pi.serverinterceptor;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import corba.framework.*;

import java.util.*;
import java.io.*;

import ServerRequestInterceptor.*;

public abstract class POAClient 
    extends ClientCommon 
{
    // The hello object to make invocations on.
    hello helloRef;

    // Reference to hello object to be forwarded to.
    hello helloRefForward;

    // To be invoked after the orb is created by subclasses.
    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        this.out = out;
        this.err = err;

        // Obey the server's commands:
        obeyServer();
    }

    void resolveReferences() throws Exception {
        out.println( "    - Resolving Hello1..." );
        // Look up reference to hello object on server:
        helloRef = resolve( orb, "Hello1" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving Hello1Forward..." );
        helloRefForward = resolve( orb, "Hello1Forward" );
        out.println( "    - Resolved." );
    }

    String syncWithServer() throws Exception {
        return helloRef.syncWithServer( exceptionRaised );
    }

    /**
     * Invoke the method with the given name on the object
     */
    protected void invokeMethod( String methodName ) 
        throws Exception 
    {
        if( methodName.equals( "sayHello" ) ) {
            helloRef.sayHello();
        }
        else if( methodName.equals( "sayOneway" ) ) {
            helloRef.sayOneway();
        }
        else if( methodName.equals( "saySystemException" ) ) { 
            helloRef.saySystemException();
        }
        else if( methodName.equals( "sayUserException" ) ) { 
            try {
                helloRef.sayUserException();
                out.println( "    - Did not catch ForwardRequest user " +
                    "exception (error)" );
                throw new RuntimeException( 
                    "Did not catch ForwardRequest user exception " +
                    "on sayUserException" );
            }
            catch( ForwardRequest e ) {
                out.println( "    - Caught ForwardRequest user " +
                    "exception (ok)" );
            }
        }
        else if( methodName.startsWith( "sayHello2" ) ) {
            // special method.  Resolve helloRef2 and call sayHello.
            String ior = methodName.substring( "sayHello2".length() + 1 );
            out.println( "    - Resolving IOR " + ior );
            org.omg.CORBA.Object obj = orb.string_to_object( ior );
            out.println( "    - Narrowing..." );
            hello helloRef2 = helloHelper.narrow( obj );
            out.println( "    - Invoking sayHello..." );
            helloRef2.sayHello();
            out.println( "    - Invoked." );
        }
        else if( methodName.equals( "_is_a" ) ) {
            out.println( "    - Invoking _is_a..." );
            out.println( "    - Result: " + 
                helloRef._is_a( "IDL:ServerRequestInterceptor/goodbye:1.0" ) );
            out.println( "    - Invoked." );
        }
        else if( methodName.equals( "_get_interface_def" ) ) {
            out.println( "    - Invoking _get_interface_def..." );
            try {
                helloRef._get_interface_def();
                out.println( "    - Invoked." );
            }
            catch( NO_IMPLEMENT e ) {
                out.println( "    - Invoked.  Received NO_IMPLEMENT (ok)." );
            }
        }
        else if( methodName.equals( "_non_existent" ) ) {
            out.println( "    - Invoking _non_existent..." );
            helloRef._non_existent();
            out.println( "    - Invoked." );
        }
    }
    
    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    hello resolve(ORB orb, String name)
        throws Exception
    {
        // Get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        
        // resolve the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        hello helloRef = helloHelper.narrow(ncRef.resolve(path));
        
        return helloRef;
    }
    

}

