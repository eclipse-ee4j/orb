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

import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.TemporarySelectorState;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

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
