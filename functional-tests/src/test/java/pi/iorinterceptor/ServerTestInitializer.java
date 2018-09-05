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

import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

/**
 * Registers the necessary IORInterceptor interceptors to test IORInterceptor.
 */
public class ServerTestInitializer 
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{

    // The PrintStream to pass to the IORInterceptor for output
    // This is set from Server.java, statically.
    static PrintStream out;

    /** The ORB to pass to the IORInterceptor */
    static ORB orb;

    /** True if post_init failed */
    public static boolean postInitFailed = false;

    /**
     * Creates a ServerTestInitializer
     */
    public ServerTestInitializer() {
    }

    /**
     * Called before all references are registered
     */
    public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info) {
    }

    /**
     * Called after all references are registered
     */
    public void post_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        IORInterceptor iorInterceptor = new SampleIORInterceptor( "test", out);
        IORInterceptor npeIORInterceptor = new NPEIORInterceptor( "npe", out);
        try {
            out.println( "    - post_init: adding Sample IOR Interceptor..." );
            info.add_ior_interceptor( iorInterceptor );
            out.println( "    - post_init: adding NPE IOR Interceptor..." );
            info.add_ior_interceptor( npeIORInterceptor );
        }
        catch( DuplicateName e ) {
            out.println( "    - post_init: received DuplicateName!" );
            postInitFailed = true;
        }

        out.println( "    - post_init: registering PolicyFactory for 100..." );
        PolicyFactory policyFactory100 = new PolicyFactoryHundred();
        info.register_policy_factory( 100, policyFactory100 );
    }

}
