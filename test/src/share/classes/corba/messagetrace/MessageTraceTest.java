/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.messagetrace;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants;

public class MessageTraceTest extends CORBATest
{
    protected void doTest() throws Throwable {
        
        if (test.Test.useJavaSerialization()) {
            return;
        }

        Controller client = createClient( "corba.messagetrace.Client" ) ;

        client.start();

        // Wait for the client to finish for up to 1 minute, then
        // throw an exception.
        client.waitFor(120000);

        // Make sure all the processes are shut down.
        client.stop();
    }
}
