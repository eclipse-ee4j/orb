/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.cosnaming;

import corba.framework.*;

public class Test extends CORBATest
{
    protected void doTest () throws Throwable
    {
        // Turn on debugging flags
        // Options.addORBDArgs( "-ORBDebug transport,subcontract,giop,orbd" ) ;
        // Options.addClientArgs( "-ORBDebug transport,subcontract,giop" ) ;

        Controller orbd = createORBD();

        orbd.start();

        Controller client = createClient("naming.cosnaming.naming_client");
        
        client.start();

        client.waitFor(60000);

        client.stop();

        orbd.stop();
    }
}
