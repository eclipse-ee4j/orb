/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.codec;

import corba.framework.*;
import java.util.*;

/**
 * Tests Codec and CodecFactory as per Portable Interceptors spec
 * orbos/99-12-02, Chapter 10.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class CodecTest extends CORBATest
{
    protected void doTest() 
        throws Throwable
    {
        Controller orbd = createORBD();

        orbd.start();

        Controller client = createClient( "pi.codec.Client" );
    
        client.start();

        client.waitFor();

        client.stop();

        orbd.stop();
    }
}

