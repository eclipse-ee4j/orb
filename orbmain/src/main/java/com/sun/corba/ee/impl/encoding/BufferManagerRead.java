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

package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;

public interface BufferManagerRead
{
    /**
     * Case: Called from ReaderThread on complete message or fragments.
     *       The given buffer may be entire message or a fragment.
     *
     *  The ReaderThread finds the ReadBufferManager instance either in
     *  in a fragment map (when collecting - GIOP 1.2 phase 1) or
     *  in an active server requests map (when streaming - GIOP 1.2 phase 2).
     *
     *  As a model for implementation see IIOPInputStream's
     *  constructor of the same name. There are going to be some variations.
     * @param byteBuffer buffer to read from
     * @param header header of fragment
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
     * @param byteBuffer Current buffer, to return to pool
     * @return Buffer containing new data
     * @see #isFragmentOnUnderflow()
     */
    ByteBuffer underflow(ByteBuffer byteBuffer);

    /**
     * Returns true if this buffer manager reads fragments when it underflows.
     * @return if fragments will be read.
     * @see #underflow(ByteBuffer)
     */
    boolean isFragmentOnUnderflow();

    /**
     * Called once after creating this buffer manager and before
     * it begins processing.
     * @param header message header
     */
    public void init(Message header);

    /**
     * Returns the mark/reset handler for this stream.
     * @return The mark/reset handler for this stream.
     */
    public MarkAndResetHandler getMarkAndResetHandler();

    /**
     * Signals that the processing be cancelled.
     * @param requestId ID of the request to cancel
     */
    public void cancelProcessing(int requestId);

    /**
     * Close BufferManagerRead and perform any outstanding cleanup.
     * @param byteBuffer buffer to return to the pool
     */
    public void close(ByteBuffer byteBuffer);
}
