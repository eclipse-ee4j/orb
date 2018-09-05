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

import java.io.PrintStream;
import java.util.Properties;
import java.util.Hashtable;

/**
 * Interface representing a process which is run in the same
 * process as the test framework.  A class can implement this
 * and be used with the InternalExec strategy.
 */
public interface InternalProcess
{
    /**
     * Start the process.
     *
     *@param environment  Environment settings (org.omg.ORBClass, etc)
     *@param args         Command line arguments
     *@param out          Standard output stream to use
     *@param err          Standard error stream to use
     *@param extra        Extra options, often used to provide
     *                    a Controller for another process that
     *                    this one needs to manipulate
     *
     *@exception  Exception  Any error that occurs when running
     */
    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception;
}
