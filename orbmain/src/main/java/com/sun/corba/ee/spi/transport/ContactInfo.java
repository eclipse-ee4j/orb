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

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;

/**
 * @author Harold Carr
 */
public abstract interface ContactInfo extends SocketInfo
{
    public ContactInfoList getContactInfoList() ;
    public IOR getTargetIOR();
    public IOR getEffectiveTargetIOR();
    public IIOPProfile getEffectiveProfile(); // REVISIT - type
    public void setAddressingDisposition(short addressingDisposition);
    public short getAddressingDisposition();
    public String getMonitoringName();

    public ORB getBroker();

    public ClientRequestDispatcher getClientRequestDispatcher();

    /**
     * Used to determine if a CorbaConnection
     * will be present in an invocation.
     *
     * For example, it may be
     * <code>false</code> in the case of shared-memory
     * <code>Input/OutputObjects</code>.
     *
     * @return <code>true</code> if a CorbaConnection
     * will be used for an invocation.
     */
    public boolean isConnectionBased();

    /**
     * Used to determine if the CorbaConnection
     * used for a request should be cached.
     *
     * If <code>true</code> then the ORB will attempt to reuse an existing
     * CorbaConnection. If
     * one is not found it will create a new one and cache it for future use.
     *
     *
     * @return <code>true</code> if a CorbaConnection
     * created by this <code>ContactInfo</code> should be cached.
     */
    public boolean shouldCacheConnection();

    public String getConnectionCacheType();

    public void setConnectionCache(OutboundConnectionCache connectionCache);

    public OutboundConnectionCache getConnectionCache();

    public Connection createConnection();

    public MessageMediator createMessageMediator(ORB broker,
        ContactInfo contactInfo, Connection connection,
        String methodName, boolean isOneWay);

    public CDROutputObject createOutputObject(MessageMediator messageMediator);

    /**
     * Used to lookup artifacts associated with this <code>ContactInfo</code>.
     *
     * @return the hash value.
     */
    public int hashCode();
}

// End of file.
