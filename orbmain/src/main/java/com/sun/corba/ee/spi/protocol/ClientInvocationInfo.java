/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;

import java.util.Iterator;

/**
 * @author Harold Carr
 */
public interface ClientInvocationInfo {
    public Iterator getContactInfoListIterator();

    public void setContactInfoListIterator(Iterator contactInfoListIterator);

    public boolean isRetryInvocation();

    public void setIsRetryInvocation(boolean isRetryInvocation);

    public int getEntryCount();

    public void incrementEntryCount();

    public void decrementEntryCount();

    public void setClientRequestDispatcher(ClientRequestDispatcher clientRequestDispatcher);

    public ClientRequestDispatcher getClientRequestDispatcher();

    public void setMessageMediator(MessageMediator messageMediator);

    public MessageMediator getMessageMediator();
}

// End of file.
