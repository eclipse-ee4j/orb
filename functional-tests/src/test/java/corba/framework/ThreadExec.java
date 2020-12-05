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
 * Runs the class in a separate thread.  Currently, the class must extend the
 * ThreadProcess class, though it probably only needs to implement the
 * InternalProcess interface (as long as it starts itself in its own
 * thread).
 */
public class ThreadExec extends InternalExec
{
    public void stop()
    {
        if (process != null)
            process.stop();
    }

    public int waitFor() throws Exception
    {
        if (process == null)
            throw new IllegalThreadStateException(processName 
                                                  + " was never started");

        return process.waitFor();
    }

    public int waitFor(long timeout) throws Exception
    {
        if (process == null)
            throw new IllegalThreadStateException(processName
                                                  + " was never started");

        return process.waitFor(timeout);
    }

    public int exitValue() throws IllegalThreadStateException
    {
        if (process == null)
            throw new IllegalThreadStateException(processName
                                                  + " was never started");
        else
            return process.exitValue();
    }

    public boolean finished() throws IllegalThreadStateException
    {
        if (process == null)
            throw new IllegalThreadStateException(processName
                                                  + " was never started");

        return process.finished();
    }

    protected void activateObject(Object obj)
    {
        process = (ThreadProcess)obj;

        PrintStream output = new PrintStream(out, true);
        PrintStream errors = new PrintStream(err, true);

        try {
            process.run(environment, programArgs, output, errors, extra);
        } catch (Exception ex) {
            ex.printStackTrace(errors);
            exitValue = 1;
        }
    }

    private ThreadProcess process = null;
}
