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

public abstract class ClientCommon 
    implements InternalProcess 
{

    // Set in run()
    com.sun.corba.ee.spi.orb.ORB orb;
    
    // Set in run()
    PrintStream out;
    
    // Set in run()
    PrintStream err;

    // Set to true if the last invocation resulted in an exception.
    boolean exceptionRaised;
    
    /**
     * Creates a com.sun.corba.ee.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    void createORB( String[] args ) {
        // create the ORB without an initializer
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass",
                   System.getProperty("org.omg.CORBA.ORBClass"));
        this.orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(args, props);
    }

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation.
     */
    abstract void resolveReferences() throws Exception;

    /**
     * Call syncWithServer on the server object
     */
    abstract String syncWithServer() throws Exception;

    /**
     * Invoke the method with the given name on the object
     */
    abstract protected void invokeMethod( String methodName ) throws Exception;

    /**
     * Wait for server to give us the name of a method to execute, and then
     * execute that method.  Repeat the process until the server tells us
     * to execute a method called "exit."
     */
    void obeyServer() throws Exception {
        out.println( "+ Obeying commands from server." );

        String methodName;
        do {
            // Re-resolve all references to eliminate any cached 
            // LOCATION_FORWARDs
            resolveReferences();

            // Synchronize with the server and get the name of the 
            // method to invoke.:
            out.println( "    - Syncing with server..." + 
                new Date().toString() );
            methodName = syncWithServer();
            out.println( "    - Synced with server at " + 
                new Date().toString() );
            
            // Execute the appropriate method on the hello object:
            out.println( "    - Executing method " + methodName + "..." );
            exceptionRaised = false;
            if( !methodName.equals( ServerCommon.EXIT_METHOD ) ) {
                try {
                    invokeMethod( methodName );
                }
                catch( IMP_LIMIT e ) {
                    exceptionRaised = true;
                    out.println( "      + Received IMP_LIMIT exception" );
                }
            }

        } while( !methodName.equals( ServerCommon.EXIT_METHOD ) );
        
        out.println( "    - Exit detected.  No longer obeying server." );
    }

}
