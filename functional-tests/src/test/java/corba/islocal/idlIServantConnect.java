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
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2003 Apr 18 (Fri) 19:11:40 by Harold Carr.
//

package corba.islocal;

import corba.hcks.U;

class idlIServantConnect
    extends 
        _idlIImplBase 
{
    public static final String baseMsg = idlIServantConnect.class.getName();

    public idlIServantConnect()
    {
    }

    public String o(String arg1)
    {
        // REVISIT : IDL stubs do NOT have colocated branch yet.
        //Server.checkThread(baseMsg);
        String result = Server.filter(arg1, baseMsg);
        U.sop(result);
        return result;
    }
}

// End of file.

