/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
import java.rmi.*;
import javax.naming.*;
import javax.rmi.*;

import ServerRequestInterceptor.*;

/**
 * Common base class for RMI client test code
 */
public abstract class DSIRMIClient 
    extends ClientCommon
{
    // The hello object to make invocations on.
    hello helloRef;

    // Reference to hello object to be forwarded to.
    hello helloRefForward;

    // RMI initial naming context
    InitialContext initialNamingContext;

    // to be invoked from subclasses after the ORB is created.
    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        this.out = out;
        this.err = err;

        out.println( "+ Creating initial naming context..." + orb );
        Hashtable env = new Hashtable();
        env.put( "java.naming.corba.orb", orb );
        initialNamingContext = new InitialContext( env );

        // Obey the server's commands:
        obeyServer();
    }

    void resolveReferences() throws Exception {
        out.println( "    - Resolving Hello1..." );
        // Look up reference to hello object on server:
        helloRef = resolve( "Hello1" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving Hello1Forward..." );
        helloRefForward = resolve( "Hello1Forward" );
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
    }
    
    /**
     * Resolves name using RMI
     */
    hello resolve(String name)
        throws Exception
    {
        java.lang.Object obj = initialNamingContext.lookup( name );
        hello helloRef = (hello)helloHelper.narrow( (org.omg.CORBA.Object)obj);

        return helloRef;
    }
    

}


