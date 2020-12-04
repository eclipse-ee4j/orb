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

import java.io.OutputStream;
import java.util.Properties;
import java.util.Hashtable;

/**
 * Defines the interface for interacting with a process.  This is 
 * the base for swapping execution strategies in the framework, 
 * which can ultimately be used for intricate debugging (with a
 * little work).
 * <P>
 * Exit values are assumed to be as follows:
 * <BR>
 * If the process was stopped, it probably doesn't have a real exit value,
 * so return STOPPED.
 * <BR>
 * SUCCESS (0) should be returned on success.
 * <BR>
 * Any non-negative integer can be returned for failure, and this value
 * will be reported in the output.
 */
public interface Controller
{
    /**
     * Return value indicating the process was stopped before it
     * exited normally.
     */
    public static final int STOPPED = -1;

    /**
     * Return value indicating the process exited normally.
     */
    public static final int SUCCESS = 0;

    /**
     * Setup everything necessary to execute the given class.
     *
     *@param className    Full class name to execute
     *@param processName  Name which identifies this process
     *                    ("server", "ORBD", "client5", etc)
     *@param environment  Environment variables to provide
     *@param VMArgs       Arguments to the VM(can be ignored)
     *@param programArgs  Arguments to the class when run
     *@param out          Output stream to pipe stdout to
     *@param err          Output stream to pipe stderr to
     *@param extra        Strategy specific initialization extras
     *
     *@exception   Exception  Any fatal error that occured
     */
    void initialize(String className,
                    String processName,
                    Properties environment,
                    String VMArgs[],
                    String programArgs[],
                    OutputStream out,
                    OutputStream err,
                    Hashtable extra) throws Exception;
  
    /** Time between calls to start() and either waitFor completes, or
     * the controller is terminated by a call to stop or kill.
     * @throws IllegalStateException if the process has not been started,
     * or has not yet completed.
     */
    long duration() ;

    /**
     * Start the process(may block).
     *
     *@exception  Exception  Any fatal error that occured
     */
    void start() throws Exception;

    /**
     * Stop the process.  This may request the termination of the process in some
     * modes, which may fail.
     *
     */
    void stop();

    /**
     * Kill the process.  This will attempt to forcibly terminate the process.
     *
     */
    void kill();
    /**
     * Wait for the process to finish executing.
     *
     *@return   0  for success,  non-zero for failure
     *
     *@exception  Exception  Any fatal error that occured
     */
    int waitFor() throws Exception;

    /**
     * Wait for the process to finish executing, or throw an
     * exception if it doesn't finished before the given timeout.
     * The timeout is relative and given in milliseconds.
     *
     *@param   timeout   Number of milliseconds to wait for the
     *                   process to finish
     *@return  0 for success, non-zero for failure
     *
     *@exception  Exception  Any fatal error that occured, including
     *                       a timeout
     */
    int waitFor(long timeout) throws Exception;

    /**
     *
     * Return the exit value for the process.
     *
     *@return   Process exit value, usually non-zero for failure
     *
     *@exception  IllegalThreadStateException  Process hasn't finished yet
     */
    int exitValue () throws IllegalThreadStateException;

    /**
     * Determine if this process has finished executing.
     *
     *@return   true if it has finished, otherwise false
     *
     *@exception IllegalThreadStateException  Process was never started
     */
    boolean finished() throws IllegalThreadStateException;
    
    /**
     * Return the reference to the OutputStream to which the process's
     * stdout is piped.
     *
     *@return   OutputStream where stdout is piped
     */
    OutputStream getOutputStream();
    
    /**
     * Return the reference to the OutputStream to which the process's
     * stderr is piped.
     *
     *@return   OutputStream where stderr is piped
     */
    OutputStream getErrorStream();

    /**
     * Return the class name to be executed.
     */
    String getClassName();

    /**
     * Return the name of the process ("server", "client10", etc).
     */
    String getProcessName();
}
