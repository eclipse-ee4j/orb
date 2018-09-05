/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.ort;

import org.omg.CORBA.*;
import corba.framework.*;
import java.util.*;

/**
 * Tests IORInterceptor and IORInfo as per Portable Interceptors spec
 * orbos/99-12-02, Chapter 7.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class ORTTest extends CORBATest 
{
    protected void doTest() 
        throws Throwable 
    {
        Controller server = createServer( "pi.ort.Server" );
    
        server.start();

        // NOTE: This sleep is required, there are some more tests that is
        // still running even after recievig "Server is Ready" handshake.
        Thread.sleep( 80000 );
      
        server.stop();

    }
}

