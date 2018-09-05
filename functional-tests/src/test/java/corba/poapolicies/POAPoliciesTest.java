/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poapolicies;

import java.util.*;
import test.Test;
import corba.framework.*;

public class POAPoliciesTest extends CORBATest
{
    private void testWithFactory(String poaFactory) throws Throwable
    {
        Test.dprint("Using POA Factory: " 
                    + (poaFactory == null ? "(Default)" : poaFactory));

        if (poaFactory != null) {
            Properties serverProps = Options.getServerProperties();
            serverProps.setProperty("POAFactory", poaFactory);
        }
        
        Controller server = createServer("corba.poapolicies.HelloServer");

        Controller client = createClient("corba.poapolicies.HelloClient", poaFactory );

        server.start();

        client.start();

        client.waitFor();

        client.stop();

        server.stop();
    }

    protected void doTest() throws Throwable
    {
        String prefix = "corba.poapolicies.";
        testWithFactory(prefix + "FactoryForRetainAndUseActiveMapOnly");
        testWithFactory(prefix + "FactoryForRetainAndUseServantManager");
    }
}

    
