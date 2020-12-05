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

    
