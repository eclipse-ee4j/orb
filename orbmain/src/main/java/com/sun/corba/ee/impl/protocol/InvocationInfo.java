/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import java.util.Iterator;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.protocol.ClientInvocationInfo;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;

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
