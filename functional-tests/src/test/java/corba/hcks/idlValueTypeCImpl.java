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
// Created       : 2000 Mar 14 (Tue) 20:18:43 by Harold Carr.
// Last Modified : 2000 Nov 25 (Sat) 13:11:39 by Harold Carr.
//

package corba.hcks;

public class idlValueTypeCImpl
    extends
        idlValueTypeC
{
    public static final String baseMsg = idlValueTypeCImpl.class.getName();

    public idlValueTypeCImpl () {}

    public idlValueTypeCImpl (short a, short b, short c)
    { 
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public String toString ()
    {
        return baseMsg + " " + a + " " + b + " " + c;
    }

    public void marshal(org.omg.CORBA.DataOutputStream out)
    {
    }

    public void unmarshal(org.omg.CORBA.DataInputStream in)
    {
    }
}

// End of file.

