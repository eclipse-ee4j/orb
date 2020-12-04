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

