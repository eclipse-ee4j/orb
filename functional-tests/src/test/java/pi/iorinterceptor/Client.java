/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.iorinterceptor;

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import corba.framework.*;

import java.util.*;
import java.io.*;

public class Client 
    implements InternalProcess 
{

    // Set from run()
    private ORB orb;
    
    // Set from run()
    private PrintStream out;
    
    // Set from run()
    private PrintStream err;
    
    private CodecFactory codecFactory;

    public static void main(String args[]) {
        try {
            (new Client()).run( System.getProperties(),
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
        out.println( "Instantiating ORB" );
        out.println( "=================" );

        // Initializer classes
        String testInitializer = "pi.orbinit.ClientTestInitializer";

        // create and initialize the ORB
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        ORB orb = ORB.init(args, props);

        this.out = out;
        this.err = err;
        this.orb = orb;

        if( ServerTestInitializer.postInitFailed ) {
            throw new RuntimeException( "post_init failed" );
        }

        // Test IORInterceptor
        testIORInterceptor();

        // Test IORInfo
        testIORInfo();
    }

    /**
     * Perform IORInterceptor tests
     */
    private void testIORInterceptor() {
        out.println();
        out.println( "Testing IORInterceptor" );
        out.println( "======================" );
        out.println( "+ Nothing to test on client side, yet." );
    }

    /**
     * Perform IORInfo-related tests
     */
    private void testIORInfo() {
        out.println();
        out.println( "Testing IORInfo" );
        out.println( "===============" );
        out.println( "+ Nothing to test on client side, yet." );
    }
}
