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

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import com.sun.corba.ee.spi.threadpool.Work;

/**
 * @author Harold Carr
 *
 * This should only be registered with ONE selector.
 */
public interface EventHandler {
    public void setUseSelectThreadToWait(boolean x);

    public boolean shouldUseSelectThreadToWait();

    public SelectableChannel getChannel();

    public int getInterestOps();

    public void setSelectionKey(SelectionKey selectionKey);

    public SelectionKey getSelectionKey();

    public void handleEvent();

    // NOTE: if there is more than one interest op this does not
    // allow discrimination between different ops and how threading
    // is handled.
    public void setUseWorkerThreadForEvent(boolean x);

    public boolean shouldUseWorkerThreadForEvent();

    public void setWork(Work work);

    public Work getWork();

    // REVISIT: need base class with two derived.
    public Acceptor getAcceptor();

    public Connection getConnection();

}

// End of file.
