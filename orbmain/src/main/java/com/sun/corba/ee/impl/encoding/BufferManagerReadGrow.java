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
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

public class BufferManagerReadGrow
    implements BufferManagerRead, MarkAndResetHandler
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public void processFragment (ByteBuffer byteBuffer, FragmentMessage header)
    {
        // REVISIT - should we consider throwing an exception similar to what's
        //           done for underflow()???
    }

    public void init(Message msg) {}

    public ByteBuffer underflow(ByteBuffer byteBuffer) {
        throw wrapper.unexpectedEof() ;
    }

    @Override
    public boolean isFragmentOnUnderflow() {
        return false;
    }

    public void cancelProcessing(int requestId) {}
    
    // Mark and reset handler -------------------------

    private Object streamMemento;
    private RestorableInputStream inputStream;
    private boolean markEngaged = false;

    public MarkAndResetHandler getMarkAndResetHandler() {
        return this;
    }

    public void mark(RestorableInputStream is) {
        markEngaged = true;
        inputStream = is;
        streamMemento = inputStream.createStreamMemento();
    }

    // This will never happen
    public void fragmentationOccured(ByteBuffer byteBuffer) {}

    public void reset() {

        if (!markEngaged)
            return;

        markEngaged = false;
        inputStream.restoreInternalState(streamMemento);
        streamMemento = null;
    }

    // Nothing to close and cleanup.
    public void close(ByteBuffer byteBuffer) {}
}
