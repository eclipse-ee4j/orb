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

/**
 * This class is only used within an InterfaceEntry. If the interface is stateful, then its state vector will contain
 * one or more of these InterfaceStates.
 **/
public class InterfaceState {
    public static final int Private = 0, Protected = 1, Public = 2;

    public InterfaceState(int m, TypedefEntry e) {
        modifier = m;
        entry = e;
        if (modifier < Private || modifier > Public)
            modifier = Public;
    } // ctor

    public int modifier = Public;
    public TypedefEntry entry = null;
} // class InterfaceState
