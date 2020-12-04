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

import java.util.*;
import java.io.*;
import org.omg.PortableInterceptor.*;

import ServerRequestInfo.*;

/**
 * Servant implementation.  
 */
class helloDelegate implements helloIF {
    private PrintStream out = null;

    // The symbol to append to SampleServerRequestInterceptor.methodOrder
    // every time a relevant method is called on this object.
    String symbol;

    // A callback back to the server so that we can cause this Servant to
    // become a client.  This is used to test the request info stack.  When
    // the Server creates this delegate, it passes a reference to an
    // object implementing ClientCallback.  This object will look up a
    // reference to this same servant and make the appropriate invocation.
    ClientCallback clientCallback;

    // See above comment for clientCallback.
    public static interface ClientCallback {
        public String sayHello();
        public void saySystemException();
    }

    public helloDelegate( PrintStream out, String symbol, 
        ClientCallback callback ) 
    {
        super();
        this.out = out;
        this.symbol = symbol;
        this.clientCallback = callback;
    }

    public String sayHello() {
        out.println( "    - helloDelegate: sayHello() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
        return "Hello, world!";
    }

    public void sayOneway() {
        out.println( "    - helloDelegate: sayOneway() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
    }
    
    public void saySystemException() {
        out.println( "    - helloDelegate: saySystemException() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
        throw new IMP_LIMIT( SampleServerRequestInterceptor.VALID_MESSAGE );
    }

    public void sayUserException() 
        throws ExampleException
    {
        out.println( "    - helloDelegate: sayUserException() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
        throw new ExampleException( "valid" );
    }
    
    // Client code calls this to synchronize with server.  This call
    // blocks until the server is ready for the next invocation.  
    // It then returns a String containing the name of the method to
    // invoke on (either "sayHello" or "saySystemException").
    // If the string "exit" is returned, the Client's
    // work is done and it may exit.
    //
    // @param exceptionRaised true if the last invocation resulted in
    //     an exception on the client side.
    public String syncWithServer( boolean exceptionRaised ) {
        out.println( "    - helloDelegate: syncWithServer() invoked" );
        // Notify the test case that the client is waiting for 
        // syncWithServer to return:
        ServerCommon.syncing = true;
        ServerCommon.exceptionRaised = exceptionRaised;
        
        // Wait for the next test case to start:
        synchronized( ServerCommon.syncObject ) {
            try {
                ServerCommon.syncObject.wait();
            }
            catch( InterruptedException e ) {
                // ignore, assume we are good to go.
            }
        }
        
        ServerCommon.syncing = false;
        
        return ServerCommon.nextMethodToInvoke;
    }

    /**
     * If n is 0, sayHello is invoked.  
     * If n is 1, saySystemException is invoked. 
     */
    public void sayInvokeAgain( int n ) {
        out.println( "    - helloDelegate: sayInvokeAgain( " + n + 
            " ) invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;

        switch( n ) {
        case INVOKE_SAY_HELLO.value:
            out.println( "    - helloDelegate: invoking sayHello..." );
            clientCallback.sayHello();
            break;
        case INVOKE_SAY_SYSTEM_EXCEPTION.value:
            out.println( 
                "    - helloDelegate: invoking saySystemException..." );
            clientCallback.saySystemException();
            break;
        }

        out.println( "    - helloDelegate: sayInvokeAgain( " + n + 
            " ) returning..." );
    }

}

