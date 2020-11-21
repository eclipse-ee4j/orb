/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
 * Debugging execution strategy.
 * <P>
 * Defers control to the user when methods are called.  The user
 * is responsible for communicating with the framework and giving
 * the expected responses.  This allows someone to use jdb or
 * another debugger in place of running a class in a separate
 * process under the framework.
 */
public class DebugExec extends ExternalExec
{
    /**
     * Was this process started?
     */
    private boolean started = false;

    /**
     * File containing the command line for JDB (erased at the
     * end of the test).
     */
    protected File jdbCmd = null;

    /**
     * Creates the JDB command line from the normal execution
     * command line.
     */
    private String buildJDBCmdString(String[] command)
    {
        String jdb = System.getProperty("java.home")
            + File.separator
            + ".."
            + File.separator
            + "bin"
            + File.separator
            + "jdb";

        StringBuilder cmd = new StringBuilder(jdb);

        for (int i = 1; i < command.length; i++) {
            cmd.append(' ').append(command[i]);
        }

        return cmd.toString();
    }

    /**
     * Opens and truncates the JDB command line file and writes
     * the command line to it.  This allows people to quickly run
     * JDB for this process without copy and paste.  Unfortunately,
     * they will have to set the execution permission on Solaris.
     */
    private void writeJDBCmdFile(String cmd) throws IOException
    {
        // The user directory will be test/build/[OS]
        String path = System.getProperty("user.dir")
            + File.separator
            + "jdb" + processName + ".bat";

        jdbCmd = new File(path);
        jdbCmd.delete();

        PrintWriter out
            = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jdbCmd)));

        jdbCmd.deleteOnExit();

        out.println(cmd);
        out.flush();
        out.close();
    }



    /**
     * Ask the user to start the process, providing both the JDB command line
     * and a file containing it, ready to execute.
     */
    public void start()
    {
        String[] command = buildCommand();

        System.out.println();
        printDebugBreak();
        System.out.println("Start the " + processName + " process");
        System.out.println();
        
        System.out.println("How to run the process with jdb: ");
        System.out.println();

        String jdbStr = buildJDBCmdString(command);
        System.out.println(jdbStr);
        System.out.println();

        try {
            writeJDBCmdFile(jdbStr);
            
            System.out.println("The above command was written to: "
                               + jdbCmd.getName());
            System.out.println("This file will be deleted when the test finishes");

        } catch (IOException ex) {
            System.out.println("Error:  Command could not be written to a file");
            System.out.println(ex);
        }

        System.out.println();

        waitForEnter("Press enter when you have started the process");

        started = true;
    }

    /**
     * Ask the user to stop the process.
     */
    public void stop()
    {
        if (jdbCmd != null) {
            jdbCmd.delete();
            jdbCmd = null;
        }

        if (!started || exitValue != INVALID_STATE)
            return;

        printDebugBreak();

        System.out.println("The framework wants to stop the "
                           + processName + " process");
        
        String result = promptUser("Did this process end on its own [default: No]? ");
        
        if (result == null || !result.toUpperCase().startsWith("Y")) {
            waitForEnter("Press enter when you have killed this process");
            exitValue = STOPPED;
        } else {
            exitValue();
        }
    }

    /**
     * Inform the user that the framework is waiting for this process, and
     * ask for its exit value.
     */
    public int waitFor()
    {
        if (started) {
            printDebugBreak();

            System.out.println("The framework is waiting for the "
                               + processName + " process");
        }

        return exitValue();
    }

    /**
     * Inform the user that the framework is waiting for this process, and
     * ask for its exit value.  The timeout is meaningless in this case.
     */
    public int waitFor(long timeout)
    {
        return waitFor();
    }

    /**
     * Ask the user for the exit value for this process.
     *
     *@return int Exit value
     *@exception IllegalThreadStateException  The process hasn't started
     */
    public int exitValue() throws IllegalThreadStateException
    {
        if (!started) 
            throw new IllegalThreadStateException(processName
                                                  + " was never started");

        while (exitValue == INVALID_STATE) {
            printDebugBreak();
            try {
                String result = promptUser("What was the exit value of the "
                                           + processName + " process (< 1 means SUCCESS)? ");
                if (result == null)
                    continue;
                exitValue = Integer.parseInt(result);
            } catch (NumberFormatException ex) {
                System.out.println("That is not a valid integer.  Please try again.");
            }
        }

        return exitValue;
    }

    /**
     * Is this process still running?
     *
     *@exception IllegalThreadStateException  The process hasn't started
     */
    public boolean finished() throws IllegalThreadStateException
    {
        if (!started)
            throw new IllegalThreadStateException(processName
                                                  + " was never started");

        return exitValue != INVALID_STATE;
    }
}
