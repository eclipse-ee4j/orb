/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.lb ;

import java.util.Properties ;

import corba.framework.CORBATest ;
import corba.framework.Options ;
import corba.framework.Controller ;

public class TestngRunnerTest extends CORBATest
{
    protected void doTest() throws Exception
    {
        String thisPackage = this.getClass().getPackage().getName() ;
        
        Controller client = createClient(thisPackage+"."+"Client", "Client");

        client.start();
        client.waitFor(1000 * 60 * 2);
        client.stop();
    }
}
