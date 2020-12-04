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
// Created       : 2000 Oct 13 (Fri) 09:55:19 by Harold Carr.
// Last Modified : 2002 Mar 24 (Sun) 09:23:14 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public class ServerORBInitializer
    extends
        org.omg.CORBA.LocalObject
    implements
        ORBInitializer
{
    public static final String baseMsg = ServerORBInitializer.class.getName();

    public void pre_init(ORBInitInfo orbInitInfo) { }

    public void post_init(ORBInitInfo orbInitInfo)
    {
        try {
            // These are intentionally random to test ordering.

            orbInitInfo.add_client_request_interceptor(
                new CRI());

            orbInitInfo.add_server_request_interceptor(
                new SRIOrdered("Three", 3));

            orbInitInfo.add_ior_interceptor(
                new ServerIORInterceptor());

            orbInitInfo.add_server_request_interceptor(
                new SRIOrdered("One", 1));

            orbInitInfo.add_server_request_interceptor(
                new SRI());

            orbInitInfo.add_server_request_interceptor(
                new SRIOrdered("Two", 2));

            System.out.println(baseMsg + ".post_init: add_* completed.");
        } catch (DuplicateName ex) {
            System.out.println(baseMsg + ".post_init: " + ex);
            System.exit(-1);
        }
    }
}

// End of file.
