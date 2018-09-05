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
// Created       : 2000 Sep 27 (Wed) 17:37:35 by Harold Carr.
// Last Modified : 2002 Dec 04 (Wed) 21:00:16 by Harold Carr.
//

package corba.connectintercept_1_4;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.impl.orb.ORBImpl;

public class MyPIORB 
    extends
        ORBImpl 
{
    public static final String baseMsg = 
        MyPIORB.class.getName() + ".objectReferenceCreated: ";

    protected IOR objectReferenceCreated (IOR ior) 
    {
        String componentData = Common.createComponentData(baseMsg, this);

        // This test puts the information in the IOR via
        // the ServerIORInterceptor. The example here is just to
        // show how to use the old hooks to get the info.
        // You would put that info in the given IOR similar to
        // the ServerIORInterceptor code then return the augmented
        // ior.
        return ior ;
    }
}
 
// End of file.

