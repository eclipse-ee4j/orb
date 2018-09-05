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
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.PortableInterceptor.*;
import com.sun.corba.ee.impl.interceptors.*;
import corba.framework.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.*;
import java.io.*;

import ServerRequestInterceptor.*;
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

public class RMIRemoteServer 
    extends RMIServer
{
    public static void main(String args[]) {
        try {
            (new RMIRemoteServer()).run( System.getProperties(),
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

        out.println( "=====================================" );
        out.println( "Instantiating ORB for RMI Remote test" );
        out.println( "=====================================" );

        out.println( "+ Creating ORB..." );
        createORB( args, new Properties() );

        super.run( environment, args, out, err, extra );
    }

    void handshake() {
        out.println( "Server is ready." );
        out.flush();
    }

    void waitForClients() {
        // wait for invocations from clients
        java.lang.Object sync = new java.lang.Object();
        synchronized( sync ) {
            try {
                sync.wait();
            }
            catch( InterruptedException e ) {
            }
        }
    }
}

