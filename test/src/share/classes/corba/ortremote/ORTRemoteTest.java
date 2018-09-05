/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.ortremote;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;

public class ORTRemoteTest extends CORBATest
{
    protected void doTest() throws Throwable {
        
        if (test.Test.useJavaSerialization()) {
            return;
        }

        // try this one. the report dir was already set to gen/corba/rmipoacounter
        Options.setOutputDirectory((String)getArgs().get(test.Test.OUTPUT_DIRECTORY));
        Options.addServerArg("-debug");

        Controller client = createClient("corba.ortremote.Test");

        client.start();
        client.waitFor(120000) ;
        client.stop();
    }
}

