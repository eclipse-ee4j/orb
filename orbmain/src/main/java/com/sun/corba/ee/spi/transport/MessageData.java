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

package com.sun.corba.ee.spi.transport;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.encoding.CDRInputObject;

public interface MessageData {
    /**
     * An array of GIOP messages. The messages will satisfy:
     * <OL>
     * <LI>If there is more than one message, the first message is a request or a reply.</LI>
     * <LI>If there is more than one message, all messages after the first will be fragment messages.</LI>
     * <LI>If there is more than one message, all messages will share the same request ID (for GIOP 1.2).</LI>
     * <LI>The more fragments bit will be set on all messages except the last message.</LI>
     * </OL>
     * 
     * @return GIOP messages
     */
    Message[] getMessages();

    /**
     * A fully initialized input stream for the message data, positioned at the first element of the body.
     * 
     * @return stream of data
     */
    CDRInputObject getStream();
}
