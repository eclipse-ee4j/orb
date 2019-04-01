/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA;

/**
 * Represents a <tt>DynAny</tt> object associated with an array.
 *
 * @deprecated Use the new <a href="../DynamicAny/DynArray.html">DynArray</a> instead
 */

// @Deprecated
public interface DynArray extends org.omg.CORBA.Object, org.omg.CORBA.DynAny {
    /**
     * Returns the value of all the elements of this array.
     *
     * @return the array of <code>Any</code> objects that is the value for this <code>DynArray</code> object
     * @see #set_elements
     */
    public org.omg.CORBA.Any[] get_elements();

    /**
     * Sets the value of this <code>DynArray</code> object to the given array.
     *
     * @param value the array of <code>Any</code> objects
     * @exception InvalidSeq if the sequence is bad
     * @see #get_elements
     */
    public void set_elements(org.omg.CORBA.Any[] value) throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
