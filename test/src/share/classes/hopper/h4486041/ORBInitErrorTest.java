/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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

    
