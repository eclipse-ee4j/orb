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
// Created       : 2000 Oct 13 (Fri) 09:48:05 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:10:21 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;

public class ServerIORInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        IORInterceptor
{
    public final String baseMsg = ServerIORInterceptor.class.getName();
    public final String estMsg  = baseMsg + ".establish_components";

    public String name()    { return baseMsg; }
    public void   destroy() { }
    public void   establish_components(IORInfo iorInfo)
    {
        IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;
        String componentData = Common.createComponentData(estMsg, iorInfoExt);
        TaggedComponent taggedComponent =
            new TaggedComponent(Common.ListenPortsComponentID,
                                componentData.getBytes());
        iorInfo.add_ior_component(taggedComponent);
        System.out.println(estMsg + ": add_ior_component completed");
    }

    public void components_established( IORInfo iorInfo )
    {
        // NO-OP
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
        // NO-OP
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
        short state ) 
    {
        // NO-OP
    }
}

// End of file.
