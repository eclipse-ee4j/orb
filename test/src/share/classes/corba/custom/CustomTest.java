/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.custom;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

// Loops through all possible fragment sizes from 32 through user defined
// max.  Currently [32, 512]
//
public class CustomTest extends CORBATest
{
    public static String[] rmicClasses = { "corba.custom.VerifierImpl"};

    protected void doTest() throws Throwable
    {
        Options.setRMICClasses(rmicClasses);
        Options.addRMICArgs("-poa -nolocalstubs -iiop -keep -g");
        boolean failed = false ;

        compileRMICFiles();
        compileJavaFiles();

        Controller orbd = createORBD();
        orbd.start();

        System.out.println();

        for (int fragmentSize = 32; fragmentSize <= 512; fragmentSize+=16) {

            System.out.print("  Fragment size " + fragmentSize + ": ");

            // Specify the fragment size property
            Properties clientProps = Options.getClientProperties();
            Properties serverProps = Options.getServerProperties();

            clientProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE,
                                    "" + fragmentSize);
            serverProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE,
                                    "" + fragmentSize);

            // Give each client and server a different name so all
            // output files are separate
            Controller server = createServer("corba.custom.Server",
                                             "server" + fragmentSize);
            Controller client = createClient("corba.custom.Client",
                                             "client" + fragmentSize);

            // Go ahead and restart both server and client each time to
            // make sure we test all fragment sizes for replies, too.
            server.start();
            client.start() ; 

            try {
                if (client.waitFor(60000) == Controller.SUCCESS) {
                    System.out.println("PASSED");
                } else {
                    String msg = "FAILED (" + client.exitValue() + ")" ;
                    System.out.println( msg ) ;
                    failed = true;
                }
            } catch (Exception e) {
                // Timed out waiting for the client
                System.out.println("HUNG");
                failed = true ;
            } finally {
                client.stop();
                server.stop();
            }
        }

        orbd.stop();

        System.out.println();

        if (failed)
            throw new Error("Failures detected" );
    }
}
