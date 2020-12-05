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
