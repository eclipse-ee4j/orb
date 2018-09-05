/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4515953;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.*;

/**
 * A very simple test to make sure that RMIC compilation works for Interfaces
 * which has methods with IDLEntity as parameters or return values.
 */
public class RMICIDLEntityTest extends CORBATest
{
    public static final String[] javaFiles = { "Processor.java",
                                               "ProcessorImpl.java" };

    public static final String[] rmiClasses = { "hopper/h4515953/Processor.class" };


    protected void doTest() throws Throwable
    {
        Options.setJavaFiles(javaFiles);
        compileJavaFiles();

        Options.setRMICClasses( rmiClasses );
        compileRMICFiles();

    }
}
    
