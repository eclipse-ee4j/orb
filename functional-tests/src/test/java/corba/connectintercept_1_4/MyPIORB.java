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

