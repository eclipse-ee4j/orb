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

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import java.io.IOException;

/**
 * Interface which allows an implementation to use double dispatch when processing the various concrete message types
 * found in this package.
 */
public interface MessageHandler {
    //
    // REVISIT - These should not throw IOException.
    // Should be handled internally.

    /**
     * Used for message types for which we don't have concrete classes, yet, such as CloseConnection and MessageError, as
     * well as unknown types.
     * 
     * @param header Message to handle
     * @throws IOException If an IO error occurs
     */
    void handleInput(Message header) throws IOException;

    // Request
    void handleInput(RequestMessage_1_0 header) throws IOException;

    void handleInput(RequestMessage_1_1 header) throws IOException;

    void handleInput(RequestMessage_1_2 header) throws IOException;

    // Reply
    void handleInput(ReplyMessage_1_0 header) throws IOException;

    void handleInput(ReplyMessage_1_1 header) throws IOException;

    void handleInput(ReplyMessage_1_2 header) throws IOException;

    // LocateRequest
    void handleInput(LocateRequestMessage_1_0 header) throws IOException;

    void handleInput(LocateRequestMessage_1_1 header) throws IOException;

    void handleInput(LocateRequestMessage_1_2 header) throws IOException;

    // LocateReply
    void handleInput(LocateReplyMessage_1_0 header) throws IOException;

    void handleInput(LocateReplyMessage_1_1 header) throws IOException;

    void handleInput(LocateReplyMessage_1_2 header) throws IOException;

    // Fragment
    void handleInput(FragmentMessage_1_1 header) throws IOException;

    void handleInput(FragmentMessage_1_2 header) throws IOException;

    // CancelRequest
    void handleInput(CancelRequestMessage header) throws IOException;
}
