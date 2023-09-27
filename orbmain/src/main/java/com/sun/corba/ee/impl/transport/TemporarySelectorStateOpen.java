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

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.sun.corba.ee.spi.transport.TemporarySelectorState;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;

/**
 *
 * @author Charlie Hunt
 */

/**
 *
 * A class which models temporary Selector in an open state.
 */
@Transport
public class TemporarySelectorStateOpen implements TemporarySelectorState {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    /** Creates a new instance of TemporarySelectorStateOpen */
    public TemporarySelectorStateOpen() {
    }

    @Transport
    public int select(Selector theSelector, long theTimeout) throws IOException {
        int result;
        if (theSelector.isOpen()) {
            if (theTimeout > 0) {
                result = theSelector.select(theTimeout);
            } else {
                throw wrapper.temporarySelectorSelectTimeoutLessThanOne(theSelector, theTimeout);
            }
        } else {
            throw new TemporarySelectorClosedException("Selector " + theSelector.toString() + " is closed.");
        }

        return result;
    }

    @Transport
    public SelectionKey registerChannel(Selector theSelector, SelectableChannel theSelectableChannel, int theOps) throws IOException {

        SelectionKey key;
        if (theSelector.isOpen()) {
            key = theSelectableChannel.register(theSelector, theOps);
        } else {
            throw new TemporarySelectorClosedException("Selector " + theSelector.toString() + " is closed.");
        }
        return key;
    }

    @Transport
    public TemporarySelectorState cancelKeyAndFlushSelector(Selector theSelector, SelectionKey theSelectionKey) throws IOException {

        if (theSelectionKey != null) {
            theSelectionKey.cancel();
        }

        if (theSelector.isOpen()) {
            theSelector.selectNow();
        } else {
            throw new TemporarySelectorClosedException("Selector " + theSelector.toString() + " is closed.");
        }

        return this;
    }

    @Transport
    public TemporarySelectorState close(Selector theSelector) throws IOException {
        theSelector.close();
        return new TemporarySelectorStateClosed();
    }

    @Transport
    public TemporarySelectorState removeSelectedKey(Selector theSelector, SelectionKey theSelectionKey) throws IOException {
        if (theSelector.isOpen()) {
            theSelector.selectedKeys().remove(theSelectionKey);
        } else {
            throw new TemporarySelectorClosedException("Selector " + theSelector.toString() + " is closed.");
        }
        return this;
    }
}
