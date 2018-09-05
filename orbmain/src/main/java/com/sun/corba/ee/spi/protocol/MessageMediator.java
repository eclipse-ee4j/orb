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

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Connection;
import java.nio.ByteBuffer;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateReplyMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateReplyOrReplyMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage;

/**
 * @author Harold Carr
 */
public abstract interface MessageMediator
    extends
        ResponseHandler
{
    RequestId getRequestIdFromRawBytes();
    public void setReplyHeader(LocateReplyOrReplyMessage header);
    public LocateReplyMessage getLocateReplyHeader();
    public ReplyMessage getReplyHeader();
    public void setReplyExceptionDetailMessage(String message);
    public RequestMessage getRequestHeader();
    public GIOPVersion getGIOPVersion();
    public byte getEncodingVersion();
    public int getRequestId();
    public boolean isOneWay();
    public String getOperationName();
    public ServiceContexts getRequestServiceContexts();
    public void setRequestServiceContexts(ServiceContexts sc);
    public ServiceContexts getReplyServiceContexts();
    public Message getDispatchHeader();
    public int getThreadPoolToUse();
    public boolean dispatch();
    public byte getStreamFormatVersion(); // REVIST name ForRequest?
    public byte getStreamFormatVersionForReply();

    // REVISIT - not sure if the final fragment and DII stuff should
    // go here.

    public void sendCancelRequestIfFinalFragmentNotSent();

    public void setDIIInfo(org.omg.CORBA.Request request);
    public boolean isDIIRequest();
    public Exception unmarshalDIIUserException(String repoId,
                                               InputStream inputStream);
    public void setDIIException(Exception exception);
    public void handleDIIReply(InputStream inputStream);

    public boolean isSystemExceptionReply();
    public boolean isUserExceptionReply();
    public boolean isLocationForwardReply();
    public boolean isDifferentAddrDispositionRequestedReply();
    public short getAddrDispositionReply();
    public IOR getForwardedIOR();
    public SystemException getSystemExceptionReply();
    public void cancelRequest();

    ////////////////////////////////////////////////////
    //
    // Server side
    //

    public ObjectKeyCacheEntry getObjectKeyCacheEntry();
    public ProtocolHandler getProtocolHandler();

    ////////////////////////////////////////////////////
    //
    // ResponseHandler
    //

    public org.omg.CORBA.portable.OutputStream createReply();
    public org.omg.CORBA.portable.OutputStream createExceptionReply();

    ////////////////////////////////////////////////////
    //
    // from core.ServerRequest
    //

    public boolean executeReturnServantInResponseConstructor();

    public void setExecuteReturnServantInResponseConstructor(boolean b);

    public boolean executeRemoveThreadInfoInResponseConstructor();

    public void setExecuteRemoveThreadInfoInResponseConstructor(boolean b);

    public boolean executePIInResponseConstructor();

    public void setExecutePIInResponseConstructor( boolean b );

    public ORB getBroker();

    public ContactInfo getContactInfo();

    public Connection getConnection();

    /**
     * Used to initialize message headers.
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    public void initializeMessage();

    /**
     * Used to send the message (or its last fragment).
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    public void finishSendingRequest();

    public CDRInputObject waitForResponse();

    public void setOutputObject(CDROutputObject outputObject);

    public CDROutputObject getOutputObject();

    public void setInputObject(CDRInputObject inputObject);

    public CDRInputObject getInputObject();
}

// End of file.

