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
// Last Modified : 2000 Nov 25 (Sat) 13:11:36 by Harold Carr.
//

package corba.hcks;

public class idlValueTypeAImpl 
    extends 
        idlValueTypeA
{
    public static final String baseMsg = idlValueTypeAImpl.class.getName();

    public idlValueTypeAImpl () {}

    public idlValueTypeAImpl (short a)
    { 
        this.a = a;
    }

    public short getShortA () 
    {
        return this.a; 
    }

    public void setShortA (short a) 
    {
        this.a = a; 
    }

    public String toString ()
    {
        return baseMsg + " " + a;
    }
}

// End of file.

