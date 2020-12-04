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

package hopper.h4486041;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.*;

public class ORBInitErrorTest
    extends 
        CORBATest
{
    protected void doTest() 
        throws
            Throwable
    {
        // The test does not use the ORBD.  However, when I did not
        // create and start it then the generated output test directory
        // was not created so the client failed when the framework was
        // creating its output files.
        Controller orbd = createORBD();
        orbd.start();


        Controller client = createClient("hopper.h4486041.Client");
        client.start();
        client.waitFor(120000);
        client.stop();
        orbd.stop();
    }
}

// End of file.

    
