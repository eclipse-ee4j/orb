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
import com.sun.corba.ee.spi.transport.OutboundConnectionCache;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;
import com.sun.corba.ee.spi.trace.Transport;

/**
 * @author Harold Carr
 */
@Transport
public abstract class ContactInfoBase implements ContactInfo {
    protected ORB orb;
    protected ContactInfoList contactInfoList;
    // NOTE: This may be different from same named one in CorbaContactInfoList.
    protected IOR effectiveTargetIOR;
    protected short addressingDisposition;
    protected OutboundConnectionCache connectionCache;

    public ORB getBroker() {
        return orb;
    }

    public ContactInfoList getContactInfoList() {
        return contactInfoList;
    }

    public ClientRequestDispatcher getClientRequestDispatcher() {
        int scid = getEffectiveProfile().getObjectKeyTemplate().getSubcontractId();
        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry();
        return scr.getClientRequestDispatcher(scid);
    }

    // Note: not all derived classes will use a connection cache.
    // These are convenience methods that may not be used.
    public void setConnectionCache(OutboundConnectionCache connectionCache) {
        this.connectionCache = connectionCache;
    }

    public OutboundConnectionCache getConnectionCache() {
        return connectionCache;
    }

    // Called when client making an invocation.
    @Transport
    public MessageMediator createMessageMediator(ORB broker, ContactInfo contactInfo, Connection connection, String methodName, boolean isOneWay) {
        // REVISIT: Would like version, ior, requestid, etc., decisions
        // to be in client subcontract. Cannot pass these to this
        // factory method because it breaks generic abstraction.
        // Maybe set methods on mediator called from subcontract
        // after creation?
        MessageMediator messageMediator = new MessageMediatorImpl((ORB) broker, (ContactInfo) contactInfo, connection,
                GIOPVersion.chooseRequestVersion((ORB) broker, effectiveTargetIOR), effectiveTargetIOR, ((Connection) connection).getNextRequestId(),
                getAddressingDisposition(), methodName, isOneWay);

        return messageMediator;
    }

    @Transport
    public CDROutputObject createOutputObject(MessageMediator messageMediator) {

        CDROutputObject outputObject = OutputStreamFactory.newCDROutputObject(orb, messageMediator, messageMediator.getRequestHeader(),
                messageMediator.getStreamFormatVersion());

        messageMediator.setOutputObject(outputObject);
        return outputObject;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfo
    //

    public short getAddressingDisposition() {
        return addressingDisposition;
    }

    public void setAddressingDisposition(short addressingDisposition) {
        this.addressingDisposition = addressingDisposition;
    }

    // REVISIT - remove this.
    public IOR getTargetIOR() {
        return contactInfoList.getTargetIOR();
    }

    public IOR getEffectiveTargetIOR() {
        return effectiveTargetIOR;
    }

    public IIOPProfile getEffectiveProfile() {
        return effectiveTargetIOR.getProfile();
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    public String toString() {
        return "CorbaContactInfoBase[" + "]";
    }
}

// End of file.
