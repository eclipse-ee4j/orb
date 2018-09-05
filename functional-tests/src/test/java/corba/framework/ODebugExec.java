/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework;

import java.io.*;
import java.util.*;
import test.*;

/**
 * Runs the class in a separate process using the JPDA options so that it can be
 * remotely debugged.  This of course assumes the class
 * has a static main method, etc.  Output is redirected appropriately by
 * using test.ProcessMonitor.
 */
public class ODebugExec extends ExternalExec
{
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
        super.initialize(className,
                         processName,
                         environment,
                         VMArgs,
                         programArgs,
                         System.out,
                         err,
                         extra);
    }

    protected String[] getDebugVMArgs()
    {
        String sourcepath = System.getProperty( "com.sun.corba.ee.test.sourcepath" ) ;
        String[] result = { "com.lambda.Debugger.Debugger", "sourcepath", 
            sourcepath } ;

        return result ;
    } ;

    public int waitFor(long timeout) throws Exception
    {
        // We don't want to set a timeout while debugging
        return waitFor() ;
    }

    public void start() throws Exception
    {
        System.out.println( "Starting process " + processName + " in remote debug mode" ) ;
        super.start() ;
        Object waiter = new Object() ;
        synchronized (waiter) {
            waiter.wait( 5000 ) ;
        }
    }

    public void stop()
    {
        // we don't want to stop; just tell the user and let them
        // tell us when to stop
        
        printDebugBreak();

        System.out.println("The framework wants to stop the "
                           + processName + " process");
        
        waitForEnter("Press enter to terminate the process");

        exitValue();
    }
}
