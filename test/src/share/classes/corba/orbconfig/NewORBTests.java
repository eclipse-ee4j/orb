/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.orbconfig;

import test.Test;
import corba.framework.*;


public class NewORBTests extends CORBATest
{
    protected void doTest() throws Throwable
    {
        // Create client controller using the given
        // class.  You can also specify names for these (for instance,
        // you may want to distinguish between many clients) by using
        // the equivalent methods that take two Strings.
        Controller client = createClient("corba.orbconfig.Client");

        client.start();

        // Wait for the client to finish for up to 2 minutes, then
        // throw an exception.
        client.waitFor(120000);

        // Make sure all the processes are shut down.
        client.stop();
    }
}

