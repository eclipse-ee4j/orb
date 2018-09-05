/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package rmic;

import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;

/**
 * StressParseIIOP walks all classes in a classpath and has CompoundType.makeType()
 * parse them. The output is sent to standard out.
 *
 * @author      Bryan Atsatt
 */
public class StressParseIIOP {

    private static final int MAX_FILES = 400;
    private static final String kPathArg                = "-classpath";
    private static final String kBatchSizeArg       = "-batchsize";
    private static final String kSoloArg                = "-solo";
    private String classPath;
    private int batchSize;
    private boolean solo;
    private int totalCount;


    /**
     * Constructor
     */
    public StressParseIIOP (String classPath, int batchSize, boolean solo) {

        this.classPath = classPath;
        this.batchSize = batchSize;
        this.solo = solo;
        totalCount = 0;
    }

    public int parse () {

        int result = 0;
                
        // Collect all classes...

        Vector allClasses = ClassEnumerator.getClasses(classPath,true);
        System.out.println("Processing " + allClasses.size() + " classes:\n");
        
        // Split the list if needed...

        Vector[] classes = split(allClasses,batchSize);

        // Parse them...

        for (int i = 0; i < classes.length; i++) {
            int exitValue = parse(classes[i]);
            if (exitValue != 0) {
                result = exitValue;
            }
        }
                
        return result;
    }

    protected static Vector[] split (Vector all, int maxSize) {
        int chunks = 1;
        int count = all.size();
        if (count > maxSize) {
            float temp = ((float)count)/((float)maxSize);
            chunks = (int) temp;
            if (temp > chunks) chunks++;
        }

        Vector[] result = new Vector[chunks];

        // Do we need to split them?

        if (chunks == 1) {

            // Nope...

            result[0] = all;

        } else {

            // Yes...

            int copyIndex = 0;
            int copyCount = maxSize;

            for (int i = 0; i < chunks; i++) {
                result[i] = new Vector(copyCount);
                int thisCount = copyCount;
                while (thisCount-- > 0) {
                    result[i].addElement(all.elementAt(copyIndex++));
                }

                count -= copyCount;
                if (count < maxSize) {
                    copyCount = count;
                }
            }
        }

        return result;
    }

    protected int parse (Vector list) {

        // Create args array...

        int listSize = list.size();
        int count = listSize + 5;
        String[] args = new String[count];
        int offset = 0;
        args[offset++] = "java";
        args[offset++] = "rmic.MapType";
        args[offset++] = new Boolean(solo).toString();
        args[offset++] = Integer.toString(totalCount);
        args[offset++] = classPath;

        for (int i = 0; i < listSize; i++) {
            args[offset++] = (String) list.elementAt(i);
        }
        totalCount += listSize;
        
        // Run the command and exit...

        return execAndWaitFor(args);
    }


    /**
     * Process the arguments, construct an instance and tell it to process the files...
     */
    public static void main(String args[]) {

        // Init arguments...

        String classPath = null;
        int batchSize = MAX_FILES;
        boolean solo = false;

        // Get arguments...

        if (args == null) {
            usage();
        } else {
            for (int i = 0; i < args.length; i++) {

                String arg = args[i].toLowerCase();

                if (arg.equals(kPathArg)) classPath = args[++i];
                else if (arg.equals(kBatchSizeArg)) batchSize = Integer.parseInt(args[++i]);
                else if (arg.equals(kSoloArg)) solo = true;
                else usage();
            }
        }

        // Init classPath if needed...

        if (classPath == null) {
            classPath = ClassEnumerator.getFullClassPath();
        }
        // Construct our object...

        StressParseIIOP parser = new StressParseIIOP(classPath,batchSize,solo);

        // Tell it to do it's thing...

        int result = 0;
        
        try {
            long startTime = System.currentTimeMillis();
            result = parser.parse();
            long duration = System.currentTimeMillis() - startTime;
                        
            if (result == 0) {
                System.out.println("PASS. Completed in " + duration + " ms.");
            } else {
                System.out.println("FAIL. Completed in " + duration + " ms.");
            }
        } catch (InternalError e) {
            result = 1;
            System.err.println("Error! " + e.getMessage());
        } catch (Exception e) {
            result = 1;
            System.err.println("Error! Caught " + e.getMessage());
            e.printStackTrace(System.err);
        }
        
        System.exit(result);
    }

    /**
     * Print usage.
     */
    public static void usage () {
        PrintStream out = System.out;

        out.println();
        out.println("Usage: java rmic.StressParseIIOP [-classpath <path>]");
        out.println();
    }

    /**
     * execAndWaitFor will create a new Process and wait for the
     * process to complete before returning
     * @param command command line arguments to pass to exec.
     * @return int the result of Process.exitValue() or -1;
     * @throws Error if an unexpected exception occurs an Error is
     * thrown with the message string from the original exception.
     */
    public static int execAndWaitFor(String[] command) {

        try {

            Runtime runtime     = Runtime.getRuntime();
            Process theProcess  = runtime.exec(command);
            ProcessMonitor monitor = new ProcessMonitor(theProcess,System.out,System.err,0);
            monitor.start();
            int result = waitForCompletion(theProcess,2000);
            monitor.stop();
            return result;
        } catch (Throwable error) {
            error.printStackTrace(System.out);
            throw new Error(error.getMessage());
        }
    }


    private static int waitForCompletion( Process theProcess, int sleepTime)
        throws java.lang.InterruptedException
    {
        int result = -1;

        try {
            theProcess.waitFor();
            result = theProcess.exitValue();
        }
        catch (java.lang.IllegalThreadStateException notDone) {
            // We assume that waitFor() does not work and exitValue()
            // failed because the Process is not done. Lets Sleep
            // for a while then check for completion again.
            Thread.sleep(sleepTime,0);
            result = waitForCompletion(theProcess,sleepTime+1500);
        }

        return result;
    }
}



/**
 * ProcessMonitor provides a thread which will consume output from a
 * java.lang.Process and write it to the specified local streams.
 *
 * @version     1.0, 6/11/98
 * @author      Bryan Atsatt
 */
class ProcessMonitor extends Thread {
    Process process;
    long delay;
    PrintWriter localOut;
    PrintWriter localErr;
    String prefix = null;

    /**
     * Constructor.
     * @param theProcess The process to monitor.
     * @param out The stream to which to copy Process.getInputStream() data.
     * @param err The stream to which to copy Process.getErrorStream() data.
     * @param delay How long to wait between checking the streams for data.
     * May be zero.
     */
    ProcessMonitor (    Process theProcess,
                        OutputStream out,
                        OutputStream err,
                        long delay) {
        process = theProcess;
        this.delay = delay;
        localOut = new PrintWriter(out,true);
        localErr = new PrintWriter(err,true);
        setDaemon(true);
    }

    /**
     * Constructor.
     * @param theProcess The process to monitor.
     * @param out The stream to which to copy Process.getInputStream() data.
     * @param err The stream to which to copy Process.getErrorStream() data.
     * @param delay How long to wait between checking the streams for data.
     * May be zero.
     * @param prefix String to prepend to all copied output lines.
     */
    ProcessMonitor (    Process theProcess,
                        OutputStream out,
                        OutputStream err,
                        long delay,
                        String prefix) {
        process = theProcess;
        this.delay = delay;
        localOut = new PrintWriter(out,true);
        localErr = new PrintWriter(err,true);
        setDaemon(true);
        this.prefix = prefix;
    }

    public void run () {

        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String eolTest = "";

        try {
            while ((eolTest = in.readLine()) != null) {
                if (prefix != null) {
                    localOut.println(prefix + eolTest);
                } else {
                    localOut.println(eolTest);
                }

            }
        } catch (IOException e) {}

        try {
            while ((eolTest = err.readLine()) != null) {
                if (prefix != null) {
                    localErr.println(prefix + eolTest);
                } else {
                    localErr.println(eolTest);
                }
            }

        } catch (IOException e) {}

        if (delay > 0) {
            synchronized (this) {

                try {
                    wait(delay);
                } catch (InterruptedException e){}
            }
        }
    }
}
