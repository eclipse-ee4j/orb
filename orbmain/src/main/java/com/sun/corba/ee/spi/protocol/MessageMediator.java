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

package com.sun.corba.ee.spi.protocol;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateReplyMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateReplyOrReplyMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfo;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA_2_3.portable.InputStream;

/**
 * @author Harold Carr
 */
public interface MessageMediator
    extends
        ResponseHandler
{
    RequestId getRequestIdFromRawBytes();
    void setReplyHeader(LocateReplyOrReplyMessage header);
    LocateReplyMessage getLocateReplyHeader();
    ReplyMessage getReplyHeader();
    void setReplyExceptionDetailMessage(String message);
    RequestMessage getRequestHeader();
    GIOPVersion getGIOPVersion();
    byte getEncodingVersion();
    int getRequestId();
    boolean isOneWay();
    String getOperationName();
    ServiceContexts getRequestServiceContexts();
    void setRequestServiceContexts(ServiceContexts sc);
    ServiceContexts getReplyServiceContexts();
    Message getDispatchHeader();
    int getThreadPoolToUse();
    boolean dispatch();
    byte getStreamFormatVersion(); // REVIST name ForRequest?
    byte getStreamFormatVersionForReply();

    // REVISIT - not sure if the final fragment and DII stuff should
    // go here.

    void sendCancelRequestIfFinalFragmentNotSent();

    void setDIIInfo(org.omg.CORBA.Request request);
    boolean isDIIRequest();
    Exception unmarshalDIIUserException(String repoId,
                                               InputStream inputStream);
    void setDIIException(Exception exception);
    void handleDIIReply(InputStream inputStream);

    boolean isSystemExceptionReply();
    boolean isUserExceptionReply();
    boolean isLocationForwardReply();
    boolean isDifferentAddrDispositionRequestedReply();
    short getAddrDispositionReply();
    IOR getForwardedIOR();
    SystemException getSystemExceptionReply();
    void cancelRequest();

    ////////////////////////////////////////////////////
    //
    // Server side
    //

    ObjectKeyCacheEntry getObjectKeyCacheEntry();
    ProtocolHandler getProtocolHandler();

    ////////////////////////////////////////////////////
    //
    // ResponseHandler
    //

    @Override
    org.omg.CORBA.portable.OutputStream createReply();
    @Override
    org.omg.CORBA.portable.OutputStream createExceptionReply();

    ////////////////////////////////////////////////////
    //
    // from core.ServerRequest
    //

    boolean executeReturnServantInResponseConstructor();

    void setExecuteReturnServantInResponseConstructor(boolean b);

    boolean executeRemoveThreadInfoInResponseConstructor();

    void setExecuteRemoveThreadInfoInResponseConstructor(boolean b);

    boolean executePIInResponseConstructor();

    void setExecutePIInResponseConstructor( boolean b );

    ORB getBroker();

    ContactInfo getContactInfo();

    Connection getConnection();

    /**
     * Used to initialize message headers.
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    void initializeMessage();

    /**
     * Used to send the message (or its last fragment).
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    void finishSendingRequest();

    CDRInputObject waitForResponse();

    void setOutputObject(CDROutputObject outputObject);

    CDROutputObject getOutputObject();

    void setInputObject(CDRInputObject inputObject);

    CDRInputObject getInputObject();
}

// End of file.

