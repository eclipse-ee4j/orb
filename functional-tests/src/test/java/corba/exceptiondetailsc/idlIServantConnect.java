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
// Last Modified : 2003 Jul 28 (Mon) 09:34:33 by Harold Carr.
//

package corba.exceptiondetailsc;

import org.omg.CORBA.FREE_MEM;
import corba.hcks.U;

class idlIServantConnect
    extends 
        _idlIImplBase
{
    public static final String baseMsg = idlIServantConnect.class.getName();

    public idlIServantConnect()
    {
    }

    public void raise_system_exception(String arg1)
    {
        throw new FREE_MEM(arg1);
    }

    public void raise_user_exception(String arg1)
        throws idlException
    {
        throw new idlException(arg1);
    }

    public void raise_runtime_exception(String arg1)
    {
        throw new RuntimeException(arg1);
    }
}

// End of file.

