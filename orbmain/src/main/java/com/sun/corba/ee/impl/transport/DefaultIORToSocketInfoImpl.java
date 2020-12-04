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

package com.sun.corba.ee.impl.transport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS ;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

public class DefaultIORToSocketInfoImpl
    implements IORToSocketInfo
{
    public List<? extends SocketInfo> getSocketInfo(IOR ior, 
        List<? extends SocketInfo> previous) {

        // 6152681
        if (! previous.isEmpty()) {
            return previous;
        }

        SocketInfo socketInfo;
        List<SocketInfo> result = new ArrayList<SocketInfo>();

        IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
            ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
        String hostname = primary.getHost().toLowerCase();
        int    port     = primary.getPort();
        // NOTE: we could check for 0 (i.e., CSIv2) but, for a 
        // non-CSIv2-configured client ORB talking to a CSIv2 configured
        // server ORB you might end up with an empty contact info list
        // which would then report a failure which would not be as
        // instructive as leaving a ContactInfo with a 0 port in the list.
        socketInfo = createSocketInfo(hostname, port);
        result.add(socketInfo);

        Iterator iterator = iiopProfileTemplate.iteratorById(
            TAG_ALTERNATE_IIOP_ADDRESS.value);

        while (iterator.hasNext()) {
            AlternateIIOPAddressComponent alternate =
                (AlternateIIOPAddressComponent) iterator.next();
            hostname = alternate.getAddress().getHost().toLowerCase();
            port     = alternate.getAddress().getPort();
            socketInfo= createSocketInfo(hostname, port);
            result.add(socketInfo);
        }
        return result;
    }

    private SocketInfo createSocketInfo(final String hostname, final int port)
    {
        return new SocketInfo() {
            public String getType() { return SocketInfo.IIOP_CLEAR_TEXT; }
            public String getHost() { return hostname; }
            public int    getPort() { return port; }};
    }
}

// End of file.
