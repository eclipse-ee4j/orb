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

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.UnknownException;

import com.sun.corba.ee.spi.ior.IOR ;

import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateRequestMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage;

/**
 * @author Harold Carr
 */
public abstract interface ProtocolHandler
{
    public void handleRequest(RequestMessage header, 
                              MessageMediator messageMediator);

    public void handleRequest(LocateRequestMessage header, 
                              MessageMediator messageMediator);

    public MessageMediator createResponse(
        MessageMediator messageMediator,
        ServiceContexts svc);
    public MessageMediator createUserExceptionResponse(
        MessageMediator messageMediator,
        ServiceContexts svc);
    public MessageMediator createUnknownExceptionResponse(
        MessageMediator messageMediator,
        UnknownException ex);
    public MessageMediator createSystemExceptionResponse(
        MessageMediator messageMediator,
        SystemException ex,
        ServiceContexts svc);
    public MessageMediator createLocationForward(
        MessageMediator messageMediator,
        IOR ior, 
        ServiceContexts svc);

    public void handleThrowableDuringServerDispatch( 
        MessageMediator request,
        Throwable exception,
        CompletionStatus completionStatus);

    public boolean handleRequest(MessageMediator messageMediator);

}

// End of file.
