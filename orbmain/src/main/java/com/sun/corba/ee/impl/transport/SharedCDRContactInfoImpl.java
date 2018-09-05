/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.encoding.BufferManagerFactory;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;
import com.sun.corba.ee.impl.protocol.SharedCDRClientRequestDispatcherImpl;

public class SharedCDRContactInfoImpl
    extends 
        ContactInfoBase
{
    // This is only necessary for the pi.clientrequestinfo test.
    // It tests that request ids are different.
    // Rather than rewrite the test, just fake it.
    private static int requestId = 0;

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public SharedCDRContactInfoImpl(
        ORB orb,
        ContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition)
    {
        this.orb = orb;
        this.contactInfoList = contactInfoList;
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
    }

    public String getType()
    {
        throw wrapper.undefinedSocketinfoOperation() ;
    }

    public String getHost()
    {
        throw wrapper.undefinedSocketinfoOperation() ;
    }

    public int getPort()
    {
        throw wrapper.undefinedSocketinfoOperation() ;
    }

    public ClientRequestDispatcher getClientRequestDispatcher()
    {
        // REVISIT - use registry
        return new SharedCDRClientRequestDispatcherImpl();
    }

    public boolean isConnectionBased()
    {
        return false;
    }

    public boolean shouldCacheConnection()
    {
        return false;
    }

    public String getConnectionCacheType()
    {
        throw wrapper.methodShouldNotBeCalled();
    }
    
    public Connection createConnection()
    {
        throw wrapper.methodShouldNotBeCalled();
    }

    // Called when client making an invocation.    
    @Override
    public MessageMediator createMessageMediator(ORB broker,
                                                 ContactInfo contactInfo,
                                                 Connection connection,
                                                 String methodName,
                                                 boolean isOneWay)
    {
        if (connection != null) {
            throw wrapper.connectionNotNullInCreateMessageMediator( connection ) ;
        }

        MessageMediator messageMediator =
            new MessageMediatorImpl(
                (ORB) broker,
                (ContactInfo)contactInfo,
                null, // Connection;
                GIOPVersion.chooseRequestVersion( (ORB)broker,
                     effectiveTargetIOR),
                effectiveTargetIOR,
                requestId++, // Fake RequestId
                getAddressingDisposition(),
                methodName,
                isOneWay);

        return messageMediator;
    }

    public CDROutputObject createOutputObject(MessageMediator messageMediator)
    {
        MessageMediator corbaMessageMediator = (MessageMediator)
            messageMediator;
        // NOTE: GROW.
        CDROutputObject outputObject =
            OutputStreamFactory.newCDROutputObject(orb, messageMediator, 
                                corbaMessageMediator.getRequestHeader(),
                                corbaMessageMediator.getStreamFormatVersion(),
                                BufferManagerFactory.GROW);
        messageMediator.setOutputObject(outputObject);
        return outputObject;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfo
    //

    public String getMonitoringName()
    {
        throw wrapper.methodShouldNotBeCalled();
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    // NOTE: hashCode and equals are CRITICAL to IIOP failover implementation.
    // See SocketOrChannelContactInfoImpl.equals.

    // This calculation must be identical to SocketOrChannelContactInfoImpl.
    private int hashCode = 
        SocketInfo.IIOP_CLEAR_TEXT.hashCode() + "localhost".hashCode() ^ -1;

    public int hashCode()
    {
        return hashCode;
    }

    public boolean equals(Object obj)
    {
        return obj instanceof SharedCDRContactInfoImpl;
    }

    public String toString()
    {
        return
            "SharedCDRContactInfoImpl[" 
            + "]";
    }

    //////////////////////////////////////////////////
    //
    // Implementation
    //
}

// End of file.
