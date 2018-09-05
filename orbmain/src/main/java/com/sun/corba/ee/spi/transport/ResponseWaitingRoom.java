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

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import org.omg.CORBA.SystemException;

/**
 * @author Harold Carr
 */
public interface ResponseWaitingRoom{
    public void registerWaiter(MessageMediator messageMediator);

    // REVISIT: maybe return void (or MessageMediator).
    public CDRInputObject waitForResponse(MessageMediator messageMediator);

    public void responseReceived(CDRInputObject inputObject);

    public void unregisterWaiter(MessageMediator messageMediator);

    public int numberRegistered();

    public void signalExceptionToAllWaiters(SystemException systemException);

    public MessageMediator getMessageMediator(int requestId);
}

// End of file.
