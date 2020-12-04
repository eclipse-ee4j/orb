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

import java.io.*;

/**
 * Runs the class in the current process.  This assumes the class implements
 * the InternalProcess interface.  Also beware the natural consequences of
 * running something in the current thread and process -- it won't return
 * unless it does so on its own.  This assumes single threaded access.
 * For multi-threaded options, see ThreadExec.
 *
 */
public class InternalExec extends ControllerAdapter
{
    private long startTime ;
    private long duration ;

    public void start( ) throws Exception
    {
        startTime = System.currentTimeMillis() ;

        try {
            Loader loader = new Loader();
            loader.addPath(Options.getOutputDirectory());

            Object obj = (loader.loadClass(className)).newInstance();

            activateObject(obj);
        } finally {
            duration = System.currentTimeMillis() - startTime ;
        }
    }
   
    public long duration() {
        return duration ;
    }

    public void stop()
    {
        // Can't be stopped
    }

    public void kill()
    {
        // Can't be killed
    }

    public int waitFor() throws Exception
    {
        return exitValue;
    }

    public int waitFor(long timeout) throws Exception
    {
        return exitValue;
    }

    public int exitValue() throws IllegalThreadStateException
    {
        // Just in case a subclass wants to change finished
        if (!finished())
            throw new IllegalThreadStateException("not finished");

        return exitValue;
    }

    public boolean finished() throws IllegalThreadStateException
    {
        return true;
    }

    /**
     * Activate the given Object by casting it to the
     * InternalProcess interface, and calling its
     * run method.
     */
    protected void activateObject(Object obj)
    {
        InternalProcess process = (InternalProcess)obj;

        PrintStream output = new PrintStream(out, true);
        PrintStream errors = new PrintStream(err, true);

        try {
            process.run(environment, programArgs, output, errors, extra);
        } catch (Exception ex) {
            ex.printStackTrace(errors);
            exitValue = 1;
        }
    }
                 
    /**
     * Exit value of this process.
     */
    protected int exitValue = Controller.SUCCESS;
}
