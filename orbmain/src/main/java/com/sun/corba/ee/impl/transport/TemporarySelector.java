/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.sun.corba.ee.spi.transport.TemporarySelectorState;

/**
 *
 * Encapsulates a temporary Selector and temporary Selector state
 * @author Charlie Hunt
 */
public class TemporarySelector {
    
    private TemporarySelectorState itsState;
    private Selector itsSelector;

    /** Creates a new instance of TemporarySelector
     * @param theSelectableChannel channel to select
     * @throws java.io.IOException If an I/O error occurs
     */
    public TemporarySelector(SelectableChannel theSelectableChannel) throws IOException {
        itsSelector = theSelectableChannel.provider().openSelector();
        itsState = new TemporarySelectorStateOpen();
    }
    
    /**
     * NOTE: There is a potential for a situation, (albeit very remote), that
     *       some other thread may be initiating an explicit "close" of a 
     *       Connection (if someone overrides the implementation of
     *       SocketOrChannelConnectionImpl and an explicit call to "close"
     *       the Connection), that call to close the Connection may also
     *       attempt to close a TemporarySelector.  If that TemporarySelector
     *       is currently in the select(long theTimeout), then the closing
     *       of that TemporarySelector will not occur until the 
     *       select(long theTimeout) method exits, (i.e. maximum blocking wait
     *       time for the close will be theTimeout milliseconds which by
     *       default is 2000 milliseconds).<p>
     *       This artifact occurs as a result of the TemporarySelector's
     *       select() and close() operations being atomic operations.
     *       However, this potential issue does not exist in the current
     *       implementation of SocketOrChannelConnectionImpl. It may arise
     *       if someone chooses to extend the implementation of the
     *       SocketOrChannelConnectionImpl and make explicit calls to
     *       close the Connection. An example of this potential scenario
     *       can be found in the "no connection cache" plug-in implementation.
     *       To avoid this potential scenario, the "no connection
     *       cache" plug-in disables the read optimization to always
     *       enter a blocking read.
     *       @see com.sun.corba.ee.impl.plugin.hwlb.NoConnectionCacheImpl
     *       NoConnectionCacheImpl to see how the 'always enter blocking
     *       read' optimization is disabled.
     * @param theTimeout If positive, block for up to theTimeout milliseconds, more or less, while waiting for a SelectableChannel to become ready; 
     *  must be greater than 0 in value
     * @return The number of keys, possibly zero, whose ready-operation sets was updated.
     * @throws java.io.IOException If an I/O error occurs
     */
    synchronized public int select(long theTimeout) throws IOException {
        return itsState.select(itsSelector, theTimeout);
    }
    
    synchronized public SelectionKey registerChannel(SelectableChannel theSelectableChannel, int theOps) throws IOException {
        return itsState.registerChannel(itsSelector, theSelectableChannel, theOps);
    }
 
    /**
     * NOTE: There is a potential for a situation, (albiet very remote), that
     *       some other thread may be in this TemporarySelector's select()
     *       method while another thread is trying to call this "close" method 
     *       as a result of an explicit close of a Connection (if someone 
     *       overrides the implementation of SocketOrChannelConnectionImpl 
     *       and makes an explicit call to "close" the Connection), that call 
     *       to close the Connection may also attempt to call this close method.
     *       If that other thread is currently in this TemporarySelector's 
     *       select(long theTimeout) method, then the call to this close method
     *       will block until the select(long theTimeout) method exits, (i.e. 
     *       maximum blocking wait time for this close will be theTimeout 
     *       milliseconds which by default is 2000 milliseconds).
     *       This artifact occurs as a result of the TemporarySelector's
     *       select() and close() operations being atomic operations.
     *       However, this potential issue does not exist in the current
     *       implementation of SocketOrChannelConnectionImpl. It may arise
     *       if someone chooses to extend the implementation of the
     *       SocketOrChannelConnectionImpl and make explicit calls to
     *       close the Connection. An example of this potential scenario
     *       exists in the "no connection cache" plug-in.  To avoid this 
     *       scenario, the "no connection cache" plug-in disables the read
     *       optimization to always enter a blocking read.
     *       See com.sun.corba.ee.impl.plugin.hwlb.NoConnectionCacheImpl.java
     *       to see how the 'always enter blocking read' optimization is
     *       disabled.
     * @throws java.io.IOException If an I/O error occurs
     */
    synchronized public void close() throws IOException {
        itsState = itsState.close(itsSelector);
    }
    
    synchronized public void removeSelectedKey(SelectionKey theSelectionKey) throws IOException {
        itsState = itsState.removeSelectedKey(itsSelector, theSelectionKey);
    }

    synchronized public void cancelAndFlushSelector(SelectionKey theSelectionKey) throws IOException {
        itsState = itsState.cancelKeyAndFlushSelector(itsSelector, theSelectionKey);
    }

}
