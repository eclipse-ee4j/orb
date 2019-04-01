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

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.encoding.CDRInputObject;

public interface MessageData {
    /**
     * An array of GIOP messages. The messages will satisfy:
     * <OL>
     * <LI>If there is more than one message, the first message is a request or a reply.
     * <LI>If there is more than one message, all messages after the first will be fragment messages.
     * <LI>If there is more than one message, all messages will share the same request ID (for GIOP 1.2).
     * <LI>The more fragments bit will be set on all messages except the last message.
     * </OL>
     */
    Message[] getMessages();

    /**
     * A fully initialized input stream for the message data, positioned at the first element of the body.
     */
    CDRInputObject getStream();
}
