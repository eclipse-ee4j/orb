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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.protocol.ClientInvocationInfo;
import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import java.util.Iterator;

/**
 * @author Harold Carr
 */
public class InvocationInfo implements ClientInvocationInfo {
    // REVISIT - these needs to be an interface-based impl.

    private boolean isRetryInvocation;
    private int entryCount;
    private Iterator contactInfoListIterator;
    private ClientRequestDispatcher clientRequestDispatcher;
    private MessageMediator messageMediator;

    public InvocationInfo() {
        isRetryInvocation = false;
        entryCount = 0;
    }

    public Iterator getContactInfoListIterator() {
        return contactInfoListIterator;
    }

    public void setContactInfoListIterator(Iterator contactInfoListIterator) {
        this.contactInfoListIterator = contactInfoListIterator;
    }

    public boolean isRetryInvocation() {
        return isRetryInvocation;
    }

    public void setIsRetryInvocation(boolean isRetryInvocation) {
        this.isRetryInvocation = isRetryInvocation;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void incrementEntryCount() {
        entryCount++;
    }

    public void decrementEntryCount() {
        entryCount--;
    }

    public void setClientRequestDispatcher(ClientRequestDispatcher clientRequestDispatcher) {
        this.clientRequestDispatcher = clientRequestDispatcher;
    }

    public ClientRequestDispatcher getClientRequestDispatcher() {
        return clientRequestDispatcher;
    }

    public void setMessageMediator(MessageMediator messageMediator) {
        this.messageMediator = messageMediator;
    }

    public MessageMediator getMessageMediator() {
        return messageMediator;
    }
}

// End of file.
