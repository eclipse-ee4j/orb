/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.adapteractivator;

import test.Test;
import corba.framework.*;
import java.util.*;
import org.omg.CORBA.*;

public class AdapterActivator extends CORBATest
{
    protected void doTest() throws Throwable
    {
        Options.addServerArgs( "-ORBServerId 123 -ORBPersistentServerPort 15000" ) ;

        Controller client = createClient("corba.adapteractivator.AdapterActivatorClient");
        Controller server = createServer("corba.adapteractivator.AdapterActivatorServer");
        Controller orbd = createORBD() ;

        orbd.start() ;
        server.start();

        client.start() ;
        client.waitFor( 60000 ) ;
        client.stop();

        server.stop();
        orbd.stop() ;
    }
}

