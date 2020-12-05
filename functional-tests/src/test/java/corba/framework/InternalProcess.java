/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
