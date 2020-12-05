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
 * Class representing a process which will run a separate thread but
 * the same process as the test framework.  A subclass can extend this
 * and be used with the ThreadExec strategy.
 * <P>
 * Subclasses should construct their run() method such that they
 * exit gracefully when stopped() returns true.
 * <P>
 * A subclass must call setExitValue and then setFinished at the
 * end of execution.
 * <P>
 * Could probably transfer most of this to ThreadExec.
 */
public abstract class ThreadProcess implements InternalProcess, Runnable
{
    protected Properties environment;
    protected String args[];
    protected PrintStream out;
    protected PrintStream err;
    protected Hashtable extra;

    private boolean finished = false;
    protected int exitValue = ExternalExec.INVALID_STATE;
    private boolean stopped = false;

    // Use a separte lock object in case the subclass wants to
    // use synchronized
    private Object lockObj = new Object();

    /**
     * Saves the parameters, and starts in its own thread
     * (so override the Runnable run() method).
     */
    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra)
    {
        this.environment = environment;
        this.args = args;
        this.out = out;
        this.err = out;
        this.extra = extra;

        (new Thread(this)).start();
    }

    public void stop()
    {
        /*
          If not finished:
          Set the exit value to STOPPED, and set the stopped flag to true
          Wait until the executing thread calls setFinished (it knows to
          do so because now stopped() returns true).
        */

        synchronized(lockObj) {

            if (!finished()) {
                exitValue = Controller.STOPPED;
                
                // The thread should eventually call setFinished and
                // exit which will wake up any waiters.  (It knows
                // it must leave because now stopped() returns true.)
                stopped = true;

                try {
                    lockObj.wait();
                } catch (InterruptedException ex) {
                    // Just return from wait -- this really shouldn't
                    // happen
                }
            }
        }
    }

    /**
     * Used by subclasses to determine if they have been stopped,
     * and should exit run().
     */
    protected boolean stopped()
    {
        synchronized(lockObj) {
            return stopped;
        }
    }

    public boolean finished()
    {
        synchronized(lockObj) {
            return finished;
        }
    }

    /**
     * Used by subclasses to declare that they are done, and wake up
     * any threads that are in waitFor.
     */
    protected void setFinished()
    {
        synchronized(lockObj) {
            finished = true;
            lockObj.notifyAll();
        }
    }

    public int waitFor() throws Exception
    {
        return waitFor(0);
    }

    public int waitFor(long timeout) throws Exception
    {
        synchronized(lockObj) {
            if (!finished())
                lockObj.wait(timeout);
            return exitValue;
        }
    }

    public int exitValue() throws IllegalThreadStateException
    {
        synchronized(lockObj) {
            if (exitValue == ExternalExec.INVALID_STATE)
                throw new IllegalThreadStateException("exit value wasn't set");

            return exitValue;
        }
    }

    /**
     * Used by a subclass to set its exit value.  This should be
     * called before setFinished().  If another thread called
     * stop(),  this won't change the exit value.
     */
    protected void setExitValue(int value)
    {
        synchronized(lockObj) {
            if (!stopped())
                exitValue = value;
        }
    }
}



