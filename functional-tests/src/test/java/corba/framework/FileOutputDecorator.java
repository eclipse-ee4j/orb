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
import java.util.*;

/**
 * Decorator around a Controller, allowing the user to simply specify
 * file names to initialize rather than creating the streams.
 * Delegates everything else.
 */
public class FileOutputDecorator implements Controller
{
    private Controller delegate;
    private boolean closed = false;
    private int emmaPort ;

    public FileOutputDecorator(Controller delegate)
    {
        this.delegate = delegate;
    }

    public long duration() {
        return delegate.duration() ;
    }

    /**
     * Setup everything necessary to execute the given class.
     *
     *@param className    Full class name to execute
     *@param processName  Name identifying this process for
     *                    output file name purposes
     *@param environment  Environment variables to provide
     *@param VMArgs       Arguments to the VM(can be ignored)
     *@param programArgs  Arguments to the class when run
     *@param outFileName  Name of file to pipe stdout to
     *@param errFileName  Name of file to pipe stderr to
     *@param extra        Strategy specific initialization extras
     *
     *@exception   Exception  Any fatal error that occured
     */
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           String outFileName,
                           String errFileName,
                           Hashtable extra,
                           int emmaPort ) throws Exception
    {
        OutputStream outstr = CORBAUtil.openFile(outFileName);
        OutputStream errstr = CORBAUtil.openFile(errFileName);
        this.emmaPort = emmaPort ;

        delegate.initialize(className,
                            processName,
                            environment,
                            VMArgs,
                            programArgs,
                            outstr,
                            errstr,
                            extra);
    }

    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
        // There is no reason to call this (it defeats the
        // purpose of this class), but must be present.
        delegate.initialize(className,
                            processName,
                            environment,
                            VMArgs,
                            programArgs,
                            out,
                            err,
                            extra);
    }

    public void start() throws Exception
    {
        delegate.start();
    }
    
    public void stop()
    {
        try {
            EmmaControl.writeCoverageData( emmaPort, Options.getEmmaFile() ) ;

            try {
                Thread.sleep( 500 ) ; // give emma time to write out the file
                                      // (This may not be required)
            } catch (InterruptedException exc) {
                // ignore this
            }

            delegate.stop();
        } finally {
            try {
                closeStreams();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
    
    public void kill()
    {
        try {

            delegate.kill();

        } finally {
            try {
                closeStreams();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
    
    public int waitFor() throws Exception
    {
        try {

            return delegate.waitFor();

        } finally {
            closeStreams();
        }
    }

    public int waitFor(long timeout) throws Exception
    {
        try {
            
            return delegate.waitFor(timeout);

        } finally {
            closeStreams();
        }
    }

    public int exitValue() throws IllegalThreadStateException
    {
        return delegate.exitValue();
    }
    
    public boolean finished() throws IllegalThreadStateException
    {
        return delegate.finished();
    }

    public OutputStream getOutputStream()
    {
        return delegate.getOutputStream();
    }

    public OutputStream getErrorStream()
    {
        return delegate.getErrorStream();
    }

    public Controller getDelegate()
    {
        return delegate;
    }

    /**
     * Flushes and closes the streams.
     */
    public void closeStreams() throws IOException
    {
        if (!closed) {

            closed = true;

            // In a recent change, the ProcessMonitor that handles
            // copying of output from a java.lang.Process now
            // closes the streams on its on when the process ends.
            // Closing them here could lead to problems.
            if (delegate instanceof corba.framework.ExternalExec)
                return;

            OutputStream out = delegate.getOutputStream();
            OutputStream err = delegate.getErrorStream();

            try {
                out.flush();
                err.flush();
            } finally {
                if (out != System.out)
                    out.close();
                if (err != System.err)
                    err.close();
            }
        }
    }

    public String getProcessName()
    {
        return delegate.getProcessName();
    }

    public String getClassName()
    {
        return delegate.getClassName();
    }
}
