/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.legacy.connection;

import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.transport.ContactInfoImpl;


/**
 * @author Harold Carr
 */
public class SocketFactoryContactInfoImpl 
    extends
        ContactInfoImpl
{
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    protected SocketInfo socketInfo;

    // XREVISIT 
    // See SocketOrChannelAcceptorImpl.createMessageMediator
    // See SocketFactoryContactInfoImpl.constructor()
    // See SocketOrChannelContactInfoImpl.constructor()
    public SocketFactoryContactInfoImpl()
    {
    }

    public SocketFactoryContactInfoImpl(
        ORB orb,
        ContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition,
        SocketInfo cookie)
    {
        super(orb, contactInfoList);
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;

        socketInfo = 
            orb.getORBData().getLegacySocketFactory()
                .getEndPointInfo(orb, effectiveTargetIOR, cookie);

        socketType = socketInfo.getType();
        hostname = socketInfo.getHost();
        port = socketInfo.getPort();
    }

    @Override
    public Connection createConnection()
    {
        Connection connection =
            new SocketFactoryConnectionImpl(
                orb, this,
                orb.getORBData().connectionSocketUseSelectThreadToWait(),
                orb.getORBData().connectionSocketUseWorkerThreadForEvent());
        return connection;
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    @Override
    public String toString()
    {
        return
            "SocketFactoryContactInfoImpl[" 
            + socketType + " "
            + hostname + " "
            + port
            + "]";
    }
}

// End of file.
