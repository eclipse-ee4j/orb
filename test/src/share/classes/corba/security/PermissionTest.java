/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.security;

import test.Test;
import corba.framework.*;
import java.util.*;

public class PermissionTest extends CORBATest
{
    protected void doTest() throws Throwable
    {
        Controller client = createClient("corba.security.Client");

        client.start();

        client.waitFor(120000);

        client.stop();
    }
}

