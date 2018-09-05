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
// Created       : 2002 Jul 19 (Fri) 13:43:29 by Harold Carr.
// Last Modified : 2003 Jun 03 (Tue) 18:06:35 by Harold Carr.
//

package corba.iorintsockfact;

import java.net.InetAddress;
import java.util.Iterator;

import org.omg.CORBA.Any;
import org.omg.IOP.Codec;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.IORInfo;
// This one is only necessary when running in current development workspace.
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;

/**
 * @author Harold Carr
 */
public class IORInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.IORInterceptor
{
    private ORB orb ;

    public IORInterceptor( ORB orb ) 
    {
        this.orb = orb ;
    }

    public final String baseMsg = IORInterceptor.class.getName();
    public String name()    { return baseMsg; }
    public void   destroy() { }

    public void   establish_components(IORInfo iorInfo)
    {
        try {
            IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;
            ObjectAdapter adapter = iorInfoExt.getObjectAdapter();

            String localAddress = InetAddress.getLocalHost().getHostAddress();
            int port =
                iorInfoExt.getServerPort(ORBSocketFactory.IIOP_CLEAR_TEXT);

            InetAddress[] allAddresses =
                InetAddress.getAllByName(localAddress);

            for (int i = 0; i < allAddresses.length; i++) {
                String address = allAddresses[0].getHostAddress();

                IIOPAddress iiopAddress = 
                    IIOPFactories.makeIIOPAddress(address, port);
                AlternateIIOPAddressComponent iiopAddressComponent =
                    IIOPFactories.makeAlternateIIOPAddressComponent(iiopAddress);
                Iterator iterator = adapter.getIORTemplate().iteratorById(
                    org.omg.IOP.TAG_INTERNET_IOP.value);
                
                while (iterator.hasNext()) {
                    TaggedProfileTemplate taggedProfileTemplate =
                        (TaggedProfileTemplate) iterator.next();
                    taggedProfileTemplate.add(iiopAddressComponent);
                }
            }
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            System.exit(-1);
        }
    }

    // Thses are only necessary when running in current development workspace.
    public void components_established( IORInfo iorInfo )
    {
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
        short state ) 
    {
    }
}

// End of file.
