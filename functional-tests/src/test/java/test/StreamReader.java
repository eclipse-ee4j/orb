/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A thread which pipes everything from an input stream
 * to an output stream.  Note that this closes the
 * output stream and dies when the reader encounters an end of
 * stream!  Thus just end the stream it's reading from to make
 * it quit.  For instance, this should occur when a process
 * ends.
 */
public class StreamReader extends Thread
{
    /**
     * The NULL_OUTPUT_STREAM can be used to create a StreamReader
     * which reads everything from the input stream and can even
     * wait for a handshake, but doesn't write to anything.
     */
    public static final OutputStream NULL_OUTPUT_STREAM 
        = new NullOutputStream();

    private OutputStream originalStream;
    private PrintWriter out;
    private BufferedReader in;
    private String prefix = null;
    private String handshake = null;
    private int handshakeStatus = WAITING;
    private List inputReceived = new LinkedList();

    // Handshake status
    private static final int WAITING = 0;
    private static final int RECEIVED = 1;
    private static final int ERROR = 2;

    // Prevent garbage collection so someone can run Processes
    // without keeping references to their StreamReaders or
    // ProcessMonitors, and still not have the program hang.
    private static Map selfReferences = new IdentityHashMap(11);

    public StreamReader(OutputStream output,
                        InputStream input) {
        originalStream = output;
        out = new PrintWriter(output, true);
        in = new BufferedReader(new InputStreamReader(input));
        setDaemon(true);
    }


    public StreamReader(OutputStream output,
                        InputStream input,
                        String handshake) {
        this(output, input);
        this.handshake = handshake;
    }

    public StreamReader(OutputStream output,
                        InputStream input,
                        String handshake,
                        String prefix) {
        this(output, input, handshake);
        this.prefix = prefix;
    }

    private final void output(String msg) {
        if (prefix == null)
            out.println(msg);
        else
            out.println(prefix + msg);
    }

    public void run() {

        try {

            // Keep a reference to ourselves around to try to avoid garbage
            // collection or stopping of this thread when it's out of scope.
            // The thread should die either when the VM exits or the end of
            // the stream is reached.
            synchronized(selfReferences) {
                selfReferences.put(this, this);
            }

            setName("StreamReader");

            int inputReceivedThreshold = 0;
            while (true) {
                String input = null ;

                try {
                    input = in.readLine();
                    if (Test.debug) 
                        System.out.println( "Streamreader.read: " + input ) ;

                    // readLine should return null at the end of the
                    // stream
                    if (input == null)
                        break;
                    if (++inputReceivedThreshold > 10000) {
                       inputReceived.clear();
                    }
        
                } catch (java.io.IOException exc) {
                    // We also can get errors due to the InputStream being
                    // closed.  Simply treat these as termination.  This
                    // seems to happen on JDK 1.4.1 only?
                    break ;
                }

                // For seeing what when wrong if no handshake.
                inputReceived.add(input);

                output(input);

                if (handshake != null && 
                    handshakeStatus == WAITING && 
                    handshake.equals(input)) {

                    signalHandshakeReceived();
                }
                    
            }

            // Process/input stream ended before the handshake
            if (handshake != null && handshakeStatus == WAITING)
                signalBadHandshake();

        } finally {
            synchronized(selfReferences) {
                selfReferences.remove(this);
            }
            out.flush();
            if (originalStream != System.out &&
                originalStream != System.err)
                out.close();
        }
    }
    
    private synchronized void signalBadHandshake() {
        if (Test.debug)
            System.out.println( "Streamreader.signalBadHandshake called" ) ;
        handshakeStatus = ERROR;
        this.notifyAll();
    }

    private synchronized void signalHandshakeReceived() {
        if (Test.debug)
            System.out.println( "Streamreader.signalHandshakeReceived called" ) ;
        handshakeStatus = RECEIVED;
        this.notifyAll();
    }

    public synchronized void waitForHandshake() 
        throws InterruptedException, Exception {

        waitForHandshake(0);
    }

    public synchronized void waitForHandshake(long timeout) 
        throws InterruptedException, Exception {

        if (handshake != null) {
            if (handshakeStatus == WAITING)
                this.wait(timeout);

            if (handshakeStatus == ERROR)
                throw new Exception("Terminated before reading handshake ("
                                    + handshake + ')' + '\n'
                                    + formatInputReceived());

            if (handshakeStatus != RECEIVED)
                throw new Exception("Timed out waiting for handshake ("
                                    + handshake + ")" + "\n"
                                    + formatInputReceived());
        }
    }

    private String formatInputReceived()
    {
        boolean headerWritten = false;
        StringBuilder sb = new StringBuilder();
        Iterator i = inputReceived.iterator();
        while (i.hasNext()) {
            if (! headerWritten) {
                sb.append("Tail of input received so far:\n");
                headerWritten = true;
            }
            String line = (String) i.next();
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static final class NullOutputStream extends java.io.OutputStream
    {
        public final void close() {}
        public final void flush() {}
        public final void write(byte[] b) {}
        public final void write(byte[] b, int offset, int len) {}
        public final void write(int b) {}
    }
}


