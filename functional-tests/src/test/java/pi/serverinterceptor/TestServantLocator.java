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

package pi.serverinterceptor;

import java.io.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.CORBA.*;

/**
 * Test Servant locator that throws a ForwardRequest.
 */
public class TestServantLocator
    extends org.omg.CORBA.LocalObject
    implements ServantLocator
{
    // The PrintStream to pass to the ServerRequestInterceptor for output
    // This is set from Server.java, statically.
    PrintStream out;

    /** The ORB to pass to the ServerRequestInterceptor */
    ORB orb;

    // Where to forward the caller on a ForwardRequest
    org.omg.CORBA.Object helloRefForward;

    // We will only throw a ForwardRequest the first time.
    boolean firstTime = true;

    /**
     * Creates the servant locator.
     */
    public TestServantLocator( PrintStream out, ORB orb, 
                               org.omg.CORBA.Object helloRefForward ) 
    {
        this.out = out;
        this.orb = orb;
        this.helloRefForward = helloRefForward;
        this.firstTime = true;
    } 

    public Servant preinvoke(byte[] oid, POA adapter, String operation,
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
        out.println( "    - TestServantLocator.preinvoke called." );
        if( firstTime ) {
            firstTime = false;
            out.println( "    - First time - raising ForwardRequest." );
            throw new org.omg.PortableServer.ForwardRequest( helloRefForward );
        }

        return new helloServant( out, "[Hello2]" );
    }

    public void postinvoke(byte[] oid, POA adapter, String operation,
                           java.lang.Object cookie, Servant servant)
    {
        out.println( "    - TestServantLocator.postinvoke called." );
    }

    void resetFirstTime() {
        this.firstTime = true;
    }

}
