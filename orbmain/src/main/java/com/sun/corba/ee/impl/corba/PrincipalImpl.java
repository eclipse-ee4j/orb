/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.corba;


@SuppressWarnings({"deprecation"})
public class PrincipalImpl extends org.omg.CORBA.Principal
{
    private byte[] value;

    public void name(byte[] value)
    {
        this.value = value.clone();
    }

    public byte[] name()
    {
        return value.clone() ;
    }
}
