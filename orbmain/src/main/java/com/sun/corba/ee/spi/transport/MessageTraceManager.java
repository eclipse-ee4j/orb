/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
     */
    boolean isEnabled();

    /**
     * Called with flag=true to enable capture of messages.
     */
    void enable(boolean flag);

    /**
     * Return an array of messages (represented as byte[]) for the message(s) sent on this thread since the last call to
     * clear(). If there is a Location Forward in this invocation, the the data returned will include all requests sent
     * starting with the first request.
     */
    byte[][] getDataSent();

    /**
     * Return an array of messages (represented as byte[]) for the message(s) received since the last call to clear().
     */
    byte[][] getDataReceived();

    void recordDataSent(ByteBuffer message);
}
