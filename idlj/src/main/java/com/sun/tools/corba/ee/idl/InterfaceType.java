/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

// NOTES:

public interface InterfaceType {
    public static final int NORMAL = 0;
    public static final int ABSTRACT = 1;

    // LOCAL generates code according to the Local interfaces
    // Here helper and holder classes are the only ones generated
    public static final int LOCAL = 2;

    // intermediate solution to enable people to extend ServantLocatorPOA
    // and ServantActivatorPOA for interoperability. This is until the
    // POA is declared Local officially by OMG
    public static final int LOCALSERVANT = 3;

    // generate only signature interfaces, no helper/holders are generated
    public static final int LOCAL_SIGNATURE_ONLY = 4;

    public int getInterfaceType();

    public void setInterfaceType(int type);
} // interface InterfaceType
