/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.Writeable ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

/** IIOPAddress represents the host and port used to establish a
 * TCP connection for an IIOP request.
 */
@ManagedData
@Description( "An IP address for the IIOP protocol" )
public interface IIOPAddress extends Writeable 
{
    @ManagedAttribute
    @Description( "The target host (name or IP address)" )
    public String getHost() ;

    @ManagedAttribute
    @Description( "The target port (0-65535)" )
    public int getPort() ;
}
