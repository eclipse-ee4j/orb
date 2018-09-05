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

//
// Bare implementation of CORBA Object.
//
public class CORBAObjectImpl extends org.omg.CORBA_2_3.portable.ObjectImpl {
    public String[] _ids() {
        String[] typeids = new String[1];
        typeids[0] = "IDL:omg.org/CORBA/Object:1.0";
        return typeids;
    }
}
