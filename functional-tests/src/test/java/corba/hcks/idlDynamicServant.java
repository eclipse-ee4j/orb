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
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2000 Nov 27 (Mon) 16:58:24 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import java.util.Properties;

class idlDynamicServant
    extends
        org.omg.PortableServer.DynamicImplementation
{
    //private static String[] __ids = { "IDL:hcks/idlI:1.0" };
    private static String[] __ids = new _idlIStub()._ids();
    public String[] _all_interfaces(POA poa, byte[] oid) { return __ids; }

    private ORB orb;

    public idlDynamicServant(ORB orb) 
    {
        this.orb = orb;
    }

    public void invoke(ServerRequest r) 
    {
        idlDynInvokeHelper.invoke(orb, r);
    }    
}

// End of file.
