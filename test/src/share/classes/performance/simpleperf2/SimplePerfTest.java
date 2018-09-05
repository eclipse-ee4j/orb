/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package performance.simpleperf2;

import test.Test;
import corba.framework.*;
import java.util.*;

public class SimplePerfTest extends CORBATest
{
    protected Controller newClientController()
    {
        return new InternalExec();
    }

    protected void doTest() throws Throwable
    {
        Options.setOutputDirectory((String)getArgs().get(test.Test.OUTPUT_DIRECTORY));
        Options.addServerArg("-debug");

        // Controller orbd = createORBD();
        Controller client = createClient("performance.simpleperf2.counterClient");

        // orbd.start();

        client.start();

        client.waitFor();

        client.stop();
        // orbd.stop();
    }
}

