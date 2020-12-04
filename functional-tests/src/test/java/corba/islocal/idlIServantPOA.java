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
// Last Modified : 2003 May 19 (Mon) 16:08:39 by Harold Carr.
//

package corba.islocal;

import corba.hcks.U;

class idlIServantPOA
    extends 
        idlIPOA
{
    public static final String baseMsg = idlIServantPOA.class.getName();

    public idlIServantPOA()
    {
    }

    public String o(String arg1)
    {
        // REVISIT : IDL stubs do NOT have colocated branch yet.
        // NEW: but now we have a SharedCDR subcontract.
        Server.checkThread(baseMsg);
        String result = Server.filter(arg1, baseMsg);
        U.sop("SERVER: " + result);
        return result;
    }
}

// End of file.

