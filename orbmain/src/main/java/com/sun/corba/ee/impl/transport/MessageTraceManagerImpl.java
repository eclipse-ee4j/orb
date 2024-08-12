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

package com.sun.corba.ee.impl.transport;

import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;

import com.sun.corba.ee.spi.transport.MessageTraceManager;

public class MessageTraceManagerImpl implements MessageTraceManager {
    // Note: this implementation does not need to be syncronized
    // because an instance of this class is only called from a single
    // thread.
    private List /* <byte[]> */ dataSent;
    private List /* <byte[]> */ dataReceived;
    private boolean enabled;
    private boolean RHRCalled; // Set to true whenever recordHeaderReceived is called.
    private byte[] header;

    public MessageTraceManagerImpl() {
        init();
        enabled = false;
    }

    public void clear() {
        init();
    }

    private void init() {
        dataSent = new ArrayList();
        dataReceived = new ArrayList();
        initHeaderRecorder();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable(boolean flag) {
        enabled = flag;
    }

    public byte[][] getDataSent() {
        return (byte[][]) dataSent.toArray(new byte[dataSent.size()][]);
    }

    public byte[][] getDataReceived() {
        return (byte[][]) dataReceived.toArray(new byte[dataReceived.size()][]);
    }

    // Methods that are used internally to record messages

    private void initHeaderRecorder() {
        RHRCalled = false;
        header = null;
    }

    /**
     * Return the contents of the byte buffer. The ByteBuffer is not modified. The result is written starting at index
     * offset in the byte[].
     * 
     * @param bb Buffer to read from
     * @param offset Offset to start from, must be non-negative
     * @return Contents of the buffer
     */
    public byte[] getBytes(ByteBuffer bb, int offset) {
        ByteBuffer view = bb.asReadOnlyBuffer();
        view.flip();
        int len = view.remaining();
        byte[] buffer = new byte[len + offset];
        view.get(buffer, offset, len);

        return buffer;
    }

    @Override
    public void recordDataSent(ByteBuffer message) {
        byte[] buffer = getBytes(message, 0);
        dataSent.add(buffer);
    }

    public void recordHeaderReceived(ByteBuffer message) {
        if (RHRCalled) {
            // Previous call was for header only: no body
            dataReceived.add(header);
            initHeaderRecorder();
        }

        RHRCalled = true;
        header = getBytes(message, 0);
    }

    public void recordBodyReceived(ByteBuffer message) {
        if (!RHRCalled)
            // This string is 12 characters long, so the ASCII
            // representation should have the same length as a
            // GIOP header.
            header = "NO HEADER!!!".getBytes();

        byte[] buffer = getBytes(message, header.length);
        System.arraycopy(header, 0, buffer, header.length, message.remaining());
        dataReceived.add(buffer);

        initHeaderRecorder();
    }
}
