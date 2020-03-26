/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.transport.SocketInfo;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * IIOPProfileTemplate represents the parts of an IIOPProfile that are independent
 * of the object identifier.  It is a container of tagged components.
 */
@ManagedData
@Description( "Template for an IIOP profile" )
public interface IIOPProfileTemplate extends TaggedProfileTemplate
{
    /** Return the GIOP version of this profile.
     * 
     * @return the GIOP version
    */
    public GIOPVersion getGIOPVersion() ;

    /** Return the IIOP address from the IIOP profile.  This is called the 
    * primary address here since other addresses may be contained in 
    * components.
    * 
    * @return The host and port of the IP address for the primary endpoint of this profile
    */
    @ManagedAttribute
    @Description( "The host and port of the IP address for the primary endpoint of this profile" )
    public IIOPAddress getPrimaryAddress()  ;

    /**
     * Returns the description of a socket to create to access the associated endpoint. Its host and port
     * will match the primary address
     * @return a description of a socket.
     */
    SocketInfo getPrimarySocketInfo();
}
