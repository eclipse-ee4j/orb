/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientrequestinfo;

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
import javax.naming.*;

/**
 * Server for RMI/IIOP version of test.  Uses old _*ImplBase skeletons.
 */
public class OldRMILocalServer 
    implements Observer 
{
    // Set from run()
    private PrintStream out;
    
    private com.sun.corba.ee.spi.orb.ORB orb;

    InitialContext initialNamingContext;

    public void run( com.sun.corba.ee.spi.orb.ORB orb, java.lang.Object syncObject,
                     Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        this.out = out;
        this.orb = (com.sun.corba.ee.spi.orb.ORB)orb;

        // Inform the JNDI provider of the ORB to use and create intial
        // naming context:
        out.println( "+ Creating initial naming context..." );
        Hashtable env = new Hashtable();
        env.put( "java.naming.corba.orb", orb );
        initialNamingContext = new InitialContext( env );

        rebindObjects();

        // no handshake required here:
        //out.println("Server is ready.");
        //out.flush();

        // Notify client to wake up:
        synchronized( syncObject ) {
            syncObject.notifyAll();
        }

        // wait for invocations from clients
        java.lang.Object sync = new java.lang.Object();
        synchronized (sync) {
            sync.wait();
        }

    }

    private void rebindObjects()
        throws Exception
    {
        out.println( "+ Creating and binding hello objects..." );
        createAndBind( "Hello1" );
        createAndBind( "Hello1Forward" );
    }
    
    /**
     * Creates and binds a hello object using RMI
     */
    public void createAndBind (String name)
        throws Exception
    {
        helloOldRMIIIOP obj = new helloOldRMIIIOP( out );
        initialNamingContext.rebind( name, obj );

        // Add this server as an observer so that when resetServant is called
        // we can rebind.
        helloDelegate delegate = obj.getDelegate();
        delegate.addObserver( this );
    }

    public void update( Observable o, java.lang.Object arg ) {
        try {
            rebindObjects();
        }
        catch( Exception e ) {
            System.err.println( "rebindObjects() failed! " + e );
            throw new RuntimeException( "rebindObjects failed!" );
        }
    }

}
