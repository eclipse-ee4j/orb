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
// Created       : 2003 Apr 15 (Tue) 16:28:44 by Harold Carr.
// Last Modified : 2003 Apr 15 (Tue) 16:31:40 by Harold Carr.
//

package corba.orbconfigappserv;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;

public class ORBInitializerImpl
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.ORBInitializer
{
    public void pre_init(ORBInitInfo info)
    {
        System.out.println("ORBInitializerImpl.pre_init");
    }

    public void post_init(ORBInitInfo info)
    {
        System.out.println("ORBInitializerImpl.post_init");
    }
}

// End of file.
