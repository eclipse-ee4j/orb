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

import java.util.Iterator ;

import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.impl.transport.AcceptorImpl;
import com.sun.corba.ee.spi.trace.Transport;

/**
 * @author Harold Carr
 */
@Transport
public class SocketFactoryAcceptorImpl
    extends
        AcceptorImpl
{
    public SocketFactoryAcceptorImpl(ORB orb, int port, 
                                     String name, String type)
    {
        super(orb, port, name, type);
    }

    @Transport
    @Override
    public boolean initialize()
    {
        if (initialized) {
            return false;
        }
        try {
            serverSocket = orb.getORBData()
                .getLegacySocketFactory().createServerSocket(type, port);
            internalInitialize();
        } catch (Throwable t) {
            throw wrapper.createListenerFailed( t, "localhost", port ) ;
        }
        initialized = true;
        return true;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    @Override
    protected String toStringName()
    {
        return "SocketFactoryAcceptorImpl";
    }

    // Fix for 6331566.
    // This Acceptor must NOT contribute alternate IIOP address components
    // to the standard IIOPProfileTemplate,
    // because typically this is used for special addresses (such as SSL
    // ports) that must NOT be present in tag alternate components.
    // However, this method MUST add an IIOPProfileTemplate if one is
    // not already present.
    @Override
    public void addToIORTemplate( IORTemplate iorTemplate,
        Policies policies, String codebase ) 
    {
        Iterator iterator = iorTemplate.iteratorById(
            org.omg.IOP.TAG_INTERNET_IOP.value);

        if (!iterator.hasNext()) {
            // If this is the first call, create the IIOP profile template.
            IIOPProfileTemplate iiopProfile = makeIIOPProfileTemplate(
                policies, codebase ) ;
            iorTemplate.add(iiopProfile);
        }
    }
}

// End of file.
