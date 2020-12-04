/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package test;

import java.io.*;

/**
 * ProcessMonitor provides a thread which will consume output from a
 * java.lang.Process and write it to the specified local streams.
 *
 * @version     1.0, 6/11/98
 * @author      Bryan Atsatt
 *
 * Split into StreamReaders by Everett Anderson 8/1/2000.  Note that
 * the output streams will be closed at the end of the
 * process's inputs.
 */
public class ProcessMonitor {
    Process process;
    boolean run = true;
    StreamReader outReader;
    StreamReader errReader;

    /**
     * Constructor.
     * @param theProcess The process to monitor.
     * @param out The stream to which to copy Process.getInputStream() data.
     * @param err The stream to which to copy Process.getErrorStream() data.
     */
    public ProcessMonitor (Process theProcess,
                           OutputStream out,
                           OutputStream err) {

        process = theProcess;
        
        outReader = new StreamReader(out, theProcess.getInputStream());
        errReader = new StreamReader(err, theProcess.getErrorStream());
    }
    
    /**
     * Constructor.
     * @param theProcess The process to monitor.
     * @param out The stream to which to copy Process.getInputStream() data.
     * @param err The stream to which to copy Process.getErrorStream() data.
     * @param prefix String to prepend to all copied output lines.
     */
    public ProcessMonitor (Process theProcess,
                           OutputStream out,
                           OutputStream err,
                           String handshake) {
        process = theProcess;

        outReader = new StreamReader(out,
                                     process.getInputStream(),
                                     handshake,
                                     null);
        errReader = new StreamReader(err,
                                     process.getErrorStream(),
                                     null,
                                     null);
    }

    public ProcessMonitor (Process theProcess,
                           OutputStream out,
                           OutputStream err,
                           String handshake,
                           String prefix) {

        process = theProcess;

        // Always assume the handshake is on stdout
        outReader = new StreamReader(out,
                                     process.getInputStream(),
                                     handshake,
                                     prefix);
        errReader = new StreamReader(err,
                                     process.getErrorStream(),
                                     null,
                                     prefix);
    }


    public void start() {
        outReader.start();
        errReader.start();
    }

    public void waitForHandshake(long timeout) throws Exception {
        outReader.waitForHandshake(timeout);
    }

    // The process should have been killed/finished before
    // calling this.
    public void finishWriting() throws InterruptedException
    {
        outReader.join();
        errReader.join();
    }
}
