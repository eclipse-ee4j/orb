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

import com.sun.corba.ee.spi.trace.Transport;

/**
 *
 * @author Charlie Hunt
 */

/**
 *
 * An class which models a temporary Selector in a closed state.
 */
@Transport
public class TemporarySelectorStateClosed implements TemporarySelectorState {

    /** Creates a new instance of TemporarySelectorStateOpen */
    public TemporarySelectorStateClosed() {
    }

    public int select(Selector theSelector, long theTimeout) throws IOException {
        String selectorToString = getSelectorToString(theSelector);
        throw new TemporarySelectorClosedException("Temporary Selector " +
                                                    selectorToString +
                                                   " closed");
    }

    public SelectionKey registerChannel(Selector theSelector,
                                        SelectableChannel theSelectableChannel,
                                        int theOps) throws IOException {
        String selectorToString = getSelectorToString(theSelector);
        throw new TemporarySelectorClosedException("Temporary Selector " +
                                                    selectorToString +
                                                   " closed");
    }

    public TemporarySelectorState cancelKeyAndFlushSelector(Selector theSelector,
                              SelectionKey theSelectionKey) throws IOException {
        String selectorToString = getSelectorToString(theSelector);
        throw new TemporarySelectorClosedException("Temporary Selector " +
                                                    selectorToString +
                                                   " closed");
    }

    @Transport
    public TemporarySelectorState close(Selector theSelector) throws IOException {
        if (theSelector != null && theSelector.isOpen()) {
            theSelector.close();
        }
        return this;
    }

    public TemporarySelectorState removeSelectedKey(Selector theSelector,
                              SelectionKey theSelectionKey) throws IOException {
        String selectorToString = getSelectorToString(theSelector);
        throw new TemporarySelectorClosedException("Temporary Selector " +
                                                    selectorToString +
                                                   " closed");
    }

    private String getSelectorToString(Selector theSelector) {
        String selectorToString = "(null)";
        if (theSelector != null) {
            selectorToString = theSelector.toString();
        }
        return selectorToString;
    }
}
