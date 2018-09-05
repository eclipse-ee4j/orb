/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverrequestinfo;

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

import ServerRequestInfo.*;

public abstract class POAClient 
    extends ClientCommon 
{
    // The hello object to make invocations on.
    hello helloRef;

    // Reference to hello object to be forwarded to.
    hello helloRefForward;

    // Reference to hello child 1
    hello helloChild1;

    // Reference to hello child 2
    hello helloChild2;

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

        out.println( "    - Resolving HelloChild1..." );
        helloChild1 = resolve( orb, "HelloChild1" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving HelloChild2..." );
        helloChild2 = resolve( orb, "HelloChild2" );
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
        else if( methodName.equals( "child1.sayHello" ) ) {
            helloChild1.sayHello();
        }
        else if( methodName.equals( "child2.sayHello" ) ) {
            helloChild2.sayHello();
        }
        else if( methodName.equals( "sayOneway" ) ) {
            helloRef.sayOneway();
        }
        else if( methodName.equals( "saySystemException" ) ) { 
            helloRef.saySystemException();
        }
        else if( methodName.equals( "child1.saySystemException" ) ) { 
            helloChild1.saySystemException();
        }
        else if( methodName.equals( "child2.saySystemException" ) ) { 
            helloChild2.saySystemException();
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
            catch( ExampleException e ) {
                out.println( "    - Caught ExampleException user " +
                    "exception (ok)" );
            }
        }
        else if( methodName.equals( "sayInvokeAgain.sayHello" ) ) {
            helloRef.sayInvokeAgain( INVOKE_SAY_HELLO.value );
        }
        else if( methodName.equals( "sayInvokeAgain.saySystemException" ) ) {
            helloRef.sayInvokeAgain( INVOKE_SAY_SYSTEM_EXCEPTION.value );
        }
        else {
            throw new RuntimeException( "Unknown method: '" + 
                methodName + "'" );
        }
    }
    
    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    static hello resolve(ORB orb, String name)
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

