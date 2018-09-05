/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.driinvocation;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.CORBATest;
import corba.framework.Controller;
import corba.framework.InternalExec;
import corba.framework.Options;
import java.util.Properties;

public class DRIInvocationTest extends CORBATest
{
    @Override
    protected Controller newClientController()
    {
        return new InternalExec();
    }

    @Override
    protected void doTest() throws Throwable
    {
        // try this one. the report dir was already set to gen/corba/rmipoacounter
        Options.setOutputDirectory((String)getArgs().get(test.Test.OUTPUT_DIRECTORY));

        Controller orbd = createORBD();

        Properties serverProps = Options.getExtraServerProperties();

        serverProps.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                                Options.getUnusedPort().toString());

        Controller server = createServer("corba.rmipoacounter.counterServer");

        orbd.start();

        server.start();

        Controller client = createClient("corba.rmipoacounter.counterClient");

        client.start();

        client.waitFor();

        client.stop();

        server.stop();

        orbd.stop();
    }
}

