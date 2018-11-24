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

import java.util.Vector;

/**
 * This class encapsulates one branch of a union. Here are some examples of what it may contain:
 * <dl>
 * <dt>case 1: short x;
 * <dd>&lt;short x, &lt;1&gt;, false&gt;
 * <dt>case 0: case 8: case 2: long x;
 * <dd>&lt;long x, &lt;0, 8, 2&gt;, false&gt;
 * <dt>default: long x;
 * <dd>&lt;long x, &lt;&gt;, true&gt;
 * <dt>case 0: case 2: default: char c;
 * <dd>&lt;char c, &lt;0, 2&gt;, true&gt;
 * </dl>
 **/
public class UnionBranch {
    /** The type definition for the branch. */
    public TypedefEntry typedef;
    /**
     * A vector of Expression's, one for each label in the order in which they appear in the IDL file. The default branch
     * has no label.
     */
    public Vector labels = new Vector();
    /** true if this is the default branch. */
    public boolean isDefault = false;
} // class UnionBranch
