/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2002 Jul 19 (Fri) 14:56:53 by Harold Carr.
// Last Modified : 2004 May 12 (Wed) 11:52:02 by Harold Carr.
//

package corba.iorintsockfact;

import java.util.Iterator;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.legacy.connection.DefaultSocketFactory;
import com.sun.corba.ee.impl.legacy.connection.EndPointInfoImpl;

/**
 * @author Harold Carr
 */
public class SocketFactory
    extends
        DefaultSocketFactory
    implements
        ORBSocketFactory
{
    public SocketFactory()
    {
    }

    //
    // Client side.
    //

    @Override
    public SocketInfo getEndPointInfo(ORB orb, 
                                        IOR ior,
                                        SocketInfo socketInfo)
    {
        // NOTE: this only uses the first IIOP profile.
        // If there are multiple profiles a different API would be used
        // inside a loop.
        IIOPProfileTemplate iptemp =
            (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;

        Iterator iterator =
            iptemp.iteratorById(org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value);

        // NOTE: this only uses the first address.
        // If one were to try another address after failure you would
        // need a loop and a hashtable, hashed by IOR to keep a loop pointer.
        // Note: IOR hashing is not particularly efficient.  However, the
        // CorbaContactInfoList version of this solves the problem.
        while (iterator.hasNext()) {
            Client.foundAlternateIIOPAddressComponent = true; // For test.

            AlternateIIOPAddressComponent iiopAddressComponent =
                (AlternateIIOPAddressComponent) iterator.next();
            return new EndPointInfoImpl(
                ORBSocketFactory.IIOP_CLEAR_TEXT,
                iiopAddressComponent.getAddress().getPort(),
                iiopAddressComponent.getAddress().getHost());
        }

        // No alternate addresses.  Just use profile address.
        Client.foundAlternateIIOPAddressComponent = false; // For test.

        IIOPProfileTemplate temp = 
            (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress primary = temp.getPrimaryAddress() ;
        String host = primary.getHost().toLowerCase();
        int    port = primary.getPort();
        return new EndPointInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT,
                                    primary.getPort(),
                                    primary.getHost().toLowerCase());
    }
}

// End of file.
