/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;

public interface BufferManagerRead
{
    /**
     * Case: Called from ReaderThread on complete message or fragments.
     *       The given buf may be entire message or a fragment.
     *
     *  The ReaderThread finds the ReadBufferManager instance either in
     *  in a fragment map (when collecting - GIOP 1.2 phase 1) or
     *  in an active server requests map (when streaming - GIOP 1.2 phase 2).
     *
     *  As a model for implementation see IIOPInputStream's
     *  constructor of the same name. There are going to be some variations.
     *
     */

    public void processFragment ( ByteBuffer byteBuffer,
        FragmentMessage header);


    /**
     * Case: called from CDRInputStream constructor before unmarshaling.
     *
     * Does:
     *
     *  this.bufQ.get()
     *
     *  If streaming then sync on bufQ and wait if empty.
     */


    /**
     * Invoked when we run out of data to read. Obtains more data from the stream.
     */
    ByteBuffer underflow(ByteBuffer byteBuffer);

    /**
     * Returns true if this buffer manager reads fragments when it underflows.
     */
    boolean isFragmentOnUnderflow();

    /**
     * Called once after creating this buffer manager and before
     * it begins processing.
     */
    public void init(Message header);

    /**
     * Returns the mark/reset handler for this stream.
     */
    public MarkAndResetHandler getMarkAndResetHandler();

    /*
     * Signals that the processing be cancelled.
     */
    public void cancelProcessing(int requestId);

    /*
     * Close BufferManagerRead and perform any oustanding cleanup.
     */
    public void close(ByteBuffer byteBuffer);
}
