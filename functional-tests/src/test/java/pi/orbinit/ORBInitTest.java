/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.orbinit;

import corba.framework.*;
import java.util.*;

/**
 * Tests ORBInitializer and ORBInitInfo as per Portable Interceptors spec
 * orbos/99-12-02, Chapter 9.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class ORBInitTest extends CORBATest {
    // Set to true if at least one test fails.
    private boolean failed = false;

    protected void doTest() throws Throwable {
        System.out.println();

        printBeginTest( "[Properties Object] " );
        Controller orbd = createORBD();
        orbd.start();
        Controller client = createClient( "pi.orbinit.PropsClient" );
        client.start();
        client.waitFor();
        printEndTest( client, null );
        client.stop();
        orbd.stop();
        pause();

        /* Second time around is invalid unless flags in ClientTestInitializer
         * are cleared; so how did this ever work correctly?
         * Also note that system vs. props test is not needed here,
         * as the ORB initialization test already covers that.

        printBeginTest( "[System Properties] " );
        orbd = createORBD();
        orbd.start();
        client = createClient( "pi.orbinit.SystemClient" );
        client.start();
        client.waitFor();
        printEndTest( client, null );
        client.stop();
        orbd.stop();
        */

        System.out.print( "      Final Result: " );
        if( failed ) {
            throw new RuntimeException( "Errors detected" );
        }
    }

    private void printBeginTest( String name ) {
        System.out.print( "      " + name );
    }

    private void printEndTest( Controller client, Controller server )
        throws Throwable
    {
        if( (server != null) && server.finished() ) {
            System.out.println( "FAILED, Server crashed" );
            failed = true;
        }
        else if( client.exitValue() != Controller.SUCCESS ) {
            System.out.println( "FAILED, Client exit value = " +
                client.exitValue() );
            failed = true;
        }
        else {
            System.out.println( "PASSED" );
        }
    }

    // Pause a little to allow all processes to fully terminate.
    private void pause() {
        try {
            Thread.sleep( 2000 );
        }
        catch( InterruptedException e ) {
            // ignore.
        }
    }

}

