/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.logging.IORSystemException ;

/**
 * @author Ken Cavanaugh
 */
public final class IIOPAddressImpl extends IIOPAddressBase
{
    private static final IORSystemException wrapper =
        IORSystemException.self ;

    private String host;
    private int port;
    
    public IIOPAddressImpl( String host, int port ) 
    {
        if ((port < 0) || (port > 65535)) {
            throw wrapper.badIiopAddressPort(port);
        }

        this.host = host ;
        this.port = port ;
    }

    public IIOPAddressImpl( InputStream is )
    {
        host = is.read_string() ;
        short thePort = is.read_short() ;
        port = shortToInt( thePort ) ;
    }

    public String getHost()
    {
        return host ;
    }

    public int getPort()
    {
        return port ;
    }
}
