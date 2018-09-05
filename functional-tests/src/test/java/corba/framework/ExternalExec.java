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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import test.ProcessMonitor;
import test.Test;

/**
 * Runs the class in a separate process.  This of course assumes the class
 * has a static main method, etc.  Output is redirected appropriately by
 * using test.ProcessMonitor.
 */
public class ExternalExec extends ControllerAdapter
{
    private long startTime = 0 ;
    private long duration = 0 ;

    public long duration() {
        if (startTime == 0) {
            throw new IllegalStateException("Process has not yet started");
        }

        if (duration == 0) {
            throw new IllegalStateException("Process has not yet completed");
        }

        return duration ;
    }

    public static final String HANDSHAKE_KEY = "handshake" ;
    protected String handshake = null ;
    private boolean addOrbToXbootClasspath ;
    
    public ExternalExec( boolean addOrbToXbootClasspath ) {
        this.addOrbToXbootClasspath = addOrbToXbootClasspath ;
    }
    
    public ExternalExec() {
        this( false ) ;
    }

    /**
     * Flag indicating that this process hasn't started, yet.
     */
    public static final int INVALID_STATE = -2;

    /**
     * java.lang.Process object, null when the process hasn't started
     * or was stopped.
     */
    protected Process process = null;

    /**
     * Monitor used to redirect the process output.
     */
    protected ProcessMonitor monitor;

    /**
     * Exit value of the process.  Will be INVALID_STATE, 
     * Controller.STOPPED, Controller.SUCCESS, or a positive value
     * indicating failure.
     */
    protected int exitValue = INVALID_STATE;

    @Override
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
        if (extra != null) {
            handshake = (String) extra.get(HANDSHAKE_KEY);
        }

        super.initialize(className,
                         processName,
                         environment,
                         VMArgs,
                         programArgs,
                         out,
                         err,
                         extra);

        addClasspath();
    }

    /**
     * Adds the class path to the current VMArgs.
     */
    protected void addClasspath()
    {
        String[] cp = new String[] { "-classpath", 
            Options.getClasspath() };
        VMArgs = CORBAUtil.combine(cp, VMArgs);
    
        if (addOrbToXbootClasspath) {
            String[] bcp = new String[] { "-Xbootclasspath/p:" + 
                System.getProperty( "corba.test.orb.classpath" ) } ;
            VMArgs = CORBAUtil.combine(bcp, VMArgs);
        }       
    }
   
    protected String[] getDebugVMArgs() 
    {
        return new String[0] ;
    }

    protected long getMaximumTimeout()
    {
        return Options.getMaximumTimeout() ;
    }

    /**
     * Create a String array with the complete command to execute, 
     * including the java executable path.
     *
     *@return   Complete command line
     */
    protected String[] buildCommand()
    {
        List<String> cmd = new ArrayList<String>() ; 
        // Command line array:

        // [java executable]
        // [arguments to the java executable]
        // [-D environment variables]
        // [debug arguments to the java executable]
        // [class name]
        // [arguments to the program]

        String[] debugArgs = getDebugVMArgs() ;

        // Java executable
        cmd.add( Options.getJavaExec() ) ;
        cmd.addAll(Arrays.asList(VMArgs));
      
        // -D environment variables
        Enumeration names = environment.propertyNames();
        while(names.hasMoreElements()) {
            String name = (String) names.nextElement();
            cmd.add( "-D" + name + "=" + environment.getProperty(name) ) ;
        }

        cmd.add( "-Dcorba.test.process.name=" + getProcessName() ) ;
        cmd.addAll(Arrays.asList(debugArgs));

        cmd.add( className ) ;
        cmd.addAll(Arrays.asList(programArgs));

        Test.dprint("--------");
        for(String str : cmd) {
            Test.dprint(str);
        }
        Test.dprint("--------");

        return cmd.toArray( new String[cmd.size()] ) ;
    }

    /**
     * Starts the class in a separate process, redirecting output
     * appropriately.  This method returns when the process starts.
     */
    public void start() throws Exception {
        try {
            startTime = System.currentTimeMillis() ;
            String[] cmd = buildCommand() ;

            if (Test.forkDebugLevel >= Test.DISPLAY) {
                System.out.println( 
                    "-----------------------------------------------------------------" ) ;
                System.out.println( "Current working directory: " +
                System.getProperty( "user.dir" ) ) ;
                System.out.println( "ExternalExec.start: Command to be executed:" ) ;
                for (String str : cmd) {
                    System.out.println("\t" + str);
                }
                System.out.println( 
                    "-----------------------------------------------------------------" ) ;
            }

            process = Runtime.getRuntime().exec(cmd) ;

            if (handshake == null) {
                monitor = new ProcessMonitor(process, out, err);
            } else {
                monitor = new ProcessMonitor(process, out, err, handshake, null);
            }

            monitor.start();

            if (handshake != null) {
                monitor.waitForHandshake(getMaximumTimeout());
            }
        } catch (Exception exc) {
            duration = System.currentTimeMillis() - startTime ;
            throw exc ;
        }
    }

    /**
     * Destroy the process and stop piping output.
     */
    private void terminate()
    {
        if (process != null) {
            try {
                exitValue = exitValue();
            } catch (IllegalThreadStateException badState) {
                // Happens when the process hasn't finished
                // or was never started
                process.destroy();
                exitValue = STOPPED;
            }

            process = null;

            try {
                monitor.finishWriting();
            } catch (InterruptedException e) {
            }

            duration = System.currentTimeMillis() - startTime ;
        }
    }

    public void stop()
    {
        terminate() ;
    }

    public final void kill() 
    {
        terminate() ;
    }

    public int waitFor() throws InterruptedException
    {
        try {
            exitValue = process.waitFor() ;
            return exitValue ;
        } catch (InterruptedException exc) {
            throw exc ;    
        } finally {
            duration = System.currentTimeMillis() - startTime ;
        }
    }

    public int waitFor(long timeout) throws Exception
    {
        long stop = System.currentTimeMillis() + timeout;
        
        do {
            if (finished()) {
                break;
            } else {
                Thread.sleep(100);
            }

        } while (System.currentTimeMillis() < stop);

        duration = System.currentTimeMillis() - startTime ;

        if (finished()) {
            exitValue = process.exitValue() ;
            return exitValue ;
        } else {
            throw new Exception("waitFor timed out for " + getProcessName());
        }
    }

    public int exitValue() throws IllegalThreadStateException
    {
        // Process is running or ended on its own.  In the latter
        // case, it will return the exit code.  In the former, it
        // will throw an exception.
        if (process != null) {
            exitValue = process.exitValue();
            process.destroy();
            process = null;
            return exitValue;
        } else if (exitValue == INVALID_STATE) {
            // Occurs when the process hasn't been started yet
            throw new IllegalThreadStateException("process hasn't started");
        } else {
            return exitValue;
        }
    }

    public boolean finished() throws IllegalThreadStateException
    {
        if (process != null) {
            return CORBAUtil.processFinished(process);
        }
        else if (exitValue == INVALID_STATE) {
            throw new IllegalThreadStateException(processName + " was never started");
        } else {
            return true;
        }
    }

    // Provide the debug print functions here so that they
    // can be shared by DebugExec and RDebugExec.

    /**
     * Print a line of characters to mark the start of a debug statement.
     */
    protected void printDebugBreak()
    {
        System.out.println("=====================================================");
    }

    /**
     * Print the message and wait for the user to press enter.
     *
     *@param message  Message to give the user
     */
    protected void waitForEnter(String message)
    {
        try {
            System.out.println(message);
            System.in.read();
            System.in.skip(System.in.available());
        } catch (IOException ex) {
            // Just return
        }
    }

    /**
     * Print the message and prompt the user for a string response.
     *
     *@param message  Message to give the user
     *@return String The user's response
     *
     */
    protected String promptUser(String message)
    {
        System.out.print(message);

        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);

            return br.readLine();

        } catch(IOException ex) {
            return null;
        }
    }
}
