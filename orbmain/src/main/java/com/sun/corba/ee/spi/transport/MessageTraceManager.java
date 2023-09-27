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

package com.sun.corba.ee.spi.transport;

import java.nio.ByteBuffer;

/**
 * Interface that supports capturing the GIOP messages for the last non-co-located invocation in the current thread.
 * This enables easy capture of the GIOP messages for testing purposes.
 */
public interface MessageTraceManager {
    /**
     * Discard all messages accumulated since the last call to clear.
     */
    void clear();

    /**
     * Returns true if messages are to be captured on this thread, otherwise false.
     * 
     * @return if messages are captured
     */
    boolean isEnabled();

    /**
     * Called with flag=true to enable capture of messages.
     * 
     * @param flag enable capture
     */
    void enable(boolean flag);

    /**
     * Return an array of messages (represented as byte[]) for the message(s) sent on this thread since the last call to
     * clear(). If there is a Location Forward in this invocation, the the data returned will include all requests sent
     * starting with the first request.
     * 
     * @return array of messages
     */
    byte[][] getDataSent();

    /**
     * Return an array of messages (represented as byte[]) for the message(s) received since the last call to clear().
     * 
     * @return messages received
     */
    byte[][] getDataReceived();

    void recordDataSent(ByteBuffer message);
}
