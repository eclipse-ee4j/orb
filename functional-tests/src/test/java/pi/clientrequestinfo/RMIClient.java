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

package pi.clientrequestinfo;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import corba.framework.*;

import java.rmi.*;

import java.util.*;
import java.io.*;
import javax.naming.*;
import javax.rmi.*;

public class RMIClient 
    extends ClientCommon
    implements InternalProcess 
{
    // Reference to hello object
    private helloIF helloRef;
    
    // Reference to hello object to be forwarded to:
    private helloIF helloRefForward;

    // Initial naming context
    InitialContext initialNamingContext;

    // Names for JNDI lookup:
    public static final String NAME1 = "hello2";
    public static final String NAME2 = "hello2Forward";

    public static void main(String args[]) {
        try {
            (new RMIClient()).run( System.getProperties(),
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
        TestInitializer.out = out;
        this.out = out;
        this.err = err;

        out.println( "===================================" );
        out.println( "Instantiating ORB for RMI/IIOP test" );
        out.println( "===================================" );

        out.println( "+ Creating ORB..." );
        createORB( args );

        // Inform the JNDI provider of the ORB to use and create intial
        // naming context:
        out.println( "+ Creating initial naming context..." );
        Hashtable env = new Hashtable();
        env.put( "java.naming.corba.orb", orb );
        initialNamingContext = new InitialContext( env );

        try {
            // Test ClientInterceptor
            testClientRequestInfo();
        } finally {
            finish() ;
        }
    }

    /**
     * Clear invocation flags of helloRef and helloRefForward
     */
    protected void clearInvoked() 
        throws Exception
    {
        helloRef.clearInvoked();
        helloRefForward.clearInvoked();
    }

    /**
     * Invoke the method with the given name on the object
     */
    protected void invokeMethod( String methodName )
        throws Exception
    {
        try {
            // Make an invocation:
            if( methodName.equals( "sayHello" ) ) {
                helloRef.sayHello();
            }
            else if( methodName.equals( "saySystemException" ) ) {
                helloRef.saySystemException();
            }
            else if( methodName.equals( "sayUserException" ) ) {
                helloRef.sayUserException();
            }
            else if( methodName.equals( "sayOneway" ) ) {
                helloRef.sayOneway();
            }
            else if( methodName.equals( "sayArguments" ) ) {
                helloRef.sayArguments( "one", 2, true );
            }
        }
        catch( RemoteException e ) {
            throw (Exception)e.detail;
        }
    }

    /**
     * Return true if the method was invoked
     */
    protected boolean wasInvoked() 
        throws Exception 
    {
        return helloRef.wasInvoked();
    }

    /**
     * Return true if the method was forwarded
     */
    protected boolean didForward() 
        throws Exception
    {
        return helloRefForward.wasInvoked();
    }

    /**
     * Perform ClientRequestInfo tests
     */
    protected void testClientRequestInfo() 
        throws Exception 
    {
        super.testClientRequestInfo();
    }

    /**
     * One-way test not applicable for RMI case.  Override it.
     */
    protected void testOneWay() throws Exception {
        out.println( "+ OneWay test not applicable for RMI.  Skipping..." );
    }

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation
     */
    protected void resolveReferences() 
        throws Exception 
    {
        out.println( "    + resolving references..." );
        out.println( "      - disabling interceptors..." );
        SampleClientRequestInterceptor.enabled = false;
        // Resolve the hello object.
        out.println( "      - " + NAME1 );
        helloRef = resolve( NAME1 );
        // The initializer will store the location the interceptors should
        // use during a normal request:
        TestInitializer.helloRef = (org.omg.CORBA.Object)helloRef;
        out.println( "      - " + NAME2 );
        helloRefForward = resolve( NAME2 );
        // The initializer will store the location the interceptors should
        // use during a forward request:
        TestInitializer.helloRefForward = 
            (org.omg.CORBA.Object)helloRefForward;
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    private helloIF resolve(String name)
        throws Exception
    {
        java.lang.Object obj = initialNamingContext.lookup( name );
        helloIF helloRef = (helloIF)PortableRemoteObject.narrow( 
            obj, helloIF.class );
        
        return helloRef;
    }
    
}



