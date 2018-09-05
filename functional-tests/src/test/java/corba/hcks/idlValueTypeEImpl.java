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
// Last Modified : 2000 Nov 25 (Sat) 13:11:41 by Harold Carr.
//

package corba.hcks;

public class idlValueTypeEImpl
    extends
        idlValueTypeE
{
    public static final String baseMsg = idlValueTypeEImpl.class.getName();

    public idlValueTypeEImpl () 
    {
        intSeq     = new int[10000];
        octetArray = new byte[10000];
    }

    public String toString ()
    {
        return baseMsg + " ";
    }
}

// End of file.

