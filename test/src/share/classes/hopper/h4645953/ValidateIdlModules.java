/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4645953;

import test.Test;
import corba.framework.*;

/**
 * A test to check that the IDL with module names starting with 'service' is
 * compiled correctly.
 */

public class ValidateIdlModules extends CORBATest
{
    public static final String[] idlFiles = { "service.idl" };

    public static final String[] javaFiles = { "pi/serviceexample/*.java" };

    protected void doTest() throws Throwable 
    {
        Options.addIDLCompilerArgs("-fall" );
        Options.setIDLFiles( idlFiles );
        Options.setJavaFiles( javaFiles );

        compileIDLFiles( );
        compileJavaFiles( );
    }
}
