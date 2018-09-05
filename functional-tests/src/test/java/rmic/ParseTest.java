/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package rmic;

import sun.rmi.rmic.iiop.Constants;
import sun.tools.java.ClassPath;
import java.lang.reflect.Method;

import corba.framework.TestngRunner ;

public class ParseTest extends test.Test implements Constants {
    public static ClassPath createClassPath() {
            
        String path = System.getProperty("java.class.path");

        // Use reflection to call sun.rmi.rmic.BatchEnvironment.createClassPath(path)
        // so that we can leave classes.zip at the front of the classpath for
        // the build environment. Don't ask.

        try {
            Class env = sun.rmi.rmic.BatchEnvironment.class;
            Method method = env.getMethod("createClassPath",new Class[]{java.lang.String.class});
            return (ClassPath) method.invoke(null,new Object[]{path});
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) 
                throw (ThreadDeath)e;
            throw new Error("ParseTest.createClassPath() caught "+e);
        }
    }
    
    public void run( ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( TestExecutor.class ) ;
        runner.run() ;
        if (runner.hasFailure()) 
            status = new Error( "test failed" ) ;
    }
}
