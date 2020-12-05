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
