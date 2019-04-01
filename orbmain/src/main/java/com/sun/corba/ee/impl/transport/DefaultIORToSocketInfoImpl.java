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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

public class DefaultIORToSocketInfoImpl implements IORToSocketInfo {
    public List<? extends SocketInfo> getSocketInfo(IOR ior, List<? extends SocketInfo> previous) {

        // 6152681
        if (!previous.isEmpty()) {
            return previous;
        }

        SocketInfo socketInfo;
        List<SocketInfo> result = new ArrayList<SocketInfo>();

        IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate) ior.getProfile().getTaggedProfileTemplate();
        IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress();
        String hostname = primary.getHost().toLowerCase();
        int port = primary.getPort();
        // NOTE: we could check for 0 (i.e., CSIv2) but, for a
        // non-CSIv2-configured client ORB talking to a CSIv2 configured
        // server ORB you might end up with an empty contact info list
        // which would then report a failure which would not be as
        // instructive as leaving a ContactInfo with a 0 port in the list.
        socketInfo = createSocketInfo(hostname, port);
        result.add(socketInfo);

        Iterator iterator = iiopProfileTemplate.iteratorById(TAG_ALTERNATE_IIOP_ADDRESS.value);

        while (iterator.hasNext()) {
            AlternateIIOPAddressComponent alternate = (AlternateIIOPAddressComponent) iterator.next();
            hostname = alternate.getAddress().getHost().toLowerCase();
            port = alternate.getAddress().getPort();
            socketInfo = createSocketInfo(hostname, port);
            result.add(socketInfo);
        }
        return result;
    }

    private SocketInfo createSocketInfo(final String hostname, final int port) {
        return new SocketInfo() {
            public String getType() {
                return SocketInfo.IIOP_CLEAR_TEXT;
            }

            public String getHost() {
                return hostname;
            }

            public int getPort() {
                return port;
            }
        };
    }
}

// End of file.
