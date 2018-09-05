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
// Last Modified : 2000 Nov 27 (Mon) 16:57:15 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import java.util.Properties;

class idlDeprecatedDynamicServant
    extends
        org.omg.CORBA.DynamicImplementation
{
    //private static String[] __ids = { "IDL:hcks/idlI:1.0" };
    private static String[] __ids = new _idlIStub()._ids();
    public String[] _ids()  { return __ids; }

    private ORB orb;

    public idlDeprecatedDynamicServant(ORB orb) 
    {
        this.orb = orb;
    }

    public void invoke(ServerRequest r) 
    {
        idlDynInvokeHelper.invoke(orb, r);
    }    
}

// End of file.
