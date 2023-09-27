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

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.transport.SocketInfo;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * IIOPProfileTemplate represents the parts of an IIOPProfile that are independent of the object identifier. It is a
 * container of tagged components.
 */
@ManagedData
@Description("Template for an IIOP profile")
public interface IIOPProfileTemplate extends TaggedProfileTemplate {
    /**
     * Return the GIOP version of this profile.
     * 
     * @return the GIOP version
     */
    public GIOPVersion getGIOPVersion();

    /**
     * Return the IIOP address from the IIOP profile. This is called the primary address here since other addresses may be
     * contained in components.
     * 
     * @return The host and port of the IP address for the primary endpoint of this profile
     */
    @ManagedAttribute
    @Description("The host and port of the IP address for the primary endpoint of this profile")
    public IIOPAddress getPrimaryAddress();

    /**
     * Returns the description of a socket to create to access the associated endpoint. Its host and port will match the
     * primary address
     * 
     * @return a description of a socket.
     */
    SocketInfo getPrimarySocketInfo();
}
