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

//
// Created       : 2004 May 11 (Tue) 10:26:27 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:07:17 by Harold Carr.
//

package corba.folb_8_1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.CORBA.Any;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.TaggedComponent;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.misc.ORBUtility;

public class IORToSocketInfoImpl
    implements IORToSocketInfo
{
    public List getSocketInfo(IOR ior, List previous)
    {
        boolean debug = ior.getORB().transportDebugFlag;

        if (debug) {
            dprint(".getSocketInfo->: " + previous);
        }

        if (! previous.isEmpty()) {
            if (debug) {
                dprint(".getSocketInfo<-: returning previous: " + previous);
            }
            return previous;
        }

        SocketInfo socketInfo;
        List result = new ArrayList();

        //
        // Find and add address from profile.
        //

        IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
            ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
        String hostname = primary.getHost().toLowerCase();
        int    port     = primary.getPort();
        socketInfo = createSocketInfo("Primary", 
                                      SocketInfo.IIOP_CLEAR_TEXT,
                                      hostname, port);
        result.add(socketInfo);

        //
        // Find and add alternate iiop addresses.
        //

        Iterator iterator;
        /* DO NOT DO THIS FOR THE TEST
        iterator = iiopProfileTemplate.iteratorById(
            TAG_ALTERNATE_IIOP_ADDRESS.value);

        while (iterator.hasNext()) {
            AlternateIIOPAddressComponent alternate =
                (AlternateIIOPAddressComponent) iterator.next();
            hostname   = alternate.getAddress().getHost().toLowerCase();
            port       = alternate.getAddress().getPort();
            socketInfo = createSocketInfo("Alternate", 
                                          SocketInfo.IIOP_CLEAR_TEXT,
                                          hostname, port);
            result.add(socketInfo);
        }
        */

        //
        // Find and add custom tagged addresses.
        //

        iterator = iiopProfileTemplate.iteratorById(
            TAG_TAGGED_CUSTOM_SOCKET_INFO.value);

        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (! Common.timing) {
                System.out.println(o);
            }
            byte[] data = ((TaggedComponent)o).getIOPComponent( ior.getORB() ).
                component_data ;
            Any any = null;
            try {
                any = Common.getCodec(ior.getORB()).decode(data);
            } catch (Exception e) {
                System.out.println("Unexpected: " + e);
                System.exit(1);
            }
            TaggedCustomSocketInfo taggedSocketInfo = 
                TaggedCustomSocketInfoHelper.extract(any);
            socketInfo = createSocketInfo("custom",
                                          taggedSocketInfo.type,
                                          taggedSocketInfo.host,
                                          taggedSocketInfo.port);
            result.add(socketInfo);
        }

        // This should be sorted in the order you want requests tried
        // if failover occurs.

        if (debug) {
            dprint(".getSocketInfo<-: returning: " + result);
        }
        return result;
    }

    private SocketInfo createSocketInfo(String testMessage,
                                        final String type,
                                        final String hostname, final int port)
    {
        if (! Common.timing) {
            System.out.println(testMessage + " " + type 
                               + " " + hostname + " " + port);
        }
        return new SocketInfo() {
            public String getType() { return type; }
            public String getHost() { return hostname; }
            public int    getPort() { return port; }
            @Override
            public String toString()
            {
                return "SocketInfo[" + type + " " + hostname + " " + port +"]";
            }
       };
    }

    private void dprint(String msg)
    {
        ORBUtility.dprint("IORToSocketInfoImpl", msg);
    }   
}

// End of file.
