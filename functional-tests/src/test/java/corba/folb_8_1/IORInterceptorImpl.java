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
// Last Modified : 2005 Jun 01 (Wed) 11:07:06 by Harold Carr.
//

package corba.folb_8_1;

import java.net.InetAddress;

import org.omg.CORBA.Any;
import org.omg.IOP.TaggedComponent;

import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt ;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;

/**
 * @author Harold Carr
 */
public class IORInterceptorImpl
    extends
        org.omg.CORBA.LocalObject
    implements
        ORBInitializer,
        org.omg.PortableInterceptor.IORInterceptor
{
    private ORB orb ;

    public IORInterceptorImpl()
    {
    }

    public IORInterceptorImpl( ORB orb ) 
    {
        this.orb = orb ;
    }

    public final String baseMsg = IORInterceptorImpl.class.getName();
    public String name()    { return baseMsg; }
    public void   destroy() { }

    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) { }

    public void post_init(ORBInitInfo info)
    {
        orb = ((ORBInitInfoExt)info).getORB() ;
        try {
            info.add_ior_interceptor(new IORInterceptorImpl(orb));
        } catch (DuplicateName ex) {
            System.out.println(baseMsg + ex);
            System.exit(1);
        }
    }

    //
    // IORInterceptor
    //

    public void   establish_components(IORInfo iorInfo)
    {
        try {
            IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;

            String localAddress = InetAddress.getLocalHost().getHostAddress();

            for (int i = 0; i < Common.socketTypes.length; i++) {

                TaggedCustomSocketInfo socketInfo = 
                    new TaggedCustomSocketInfo(
                        Common.socketTypes[i], 
                        localAddress,
                        iorInfoExt.getServerPort(Common.socketTypes[i]));

                if (orb.transportDebugFlag) {
                    dprint(".establish_components:" 
                           + " " + Common.socketTypes[i]
                           + " " + localAddress
                           + " " + iorInfoExt.getServerPort(Common.socketTypes[i]));
                }

                Any any = orb.create_any();
                TaggedCustomSocketInfoHelper.insert(any, socketInfo);
                byte[] data = Common.getCodec(orb).encode(any);
                TaggedComponent tc =
                    new TaggedComponent(TAG_TAGGED_CUSTOM_SOCKET_INFO.value,
                                        data);
                iorInfo.add_ior_component(tc);
            }
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            System.exit(1);
        }
    }

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

    private void dprint(String msg)
    {
        ORBUtility.dprint("IORInterceptor", msg);
    }
}

// End of file.
