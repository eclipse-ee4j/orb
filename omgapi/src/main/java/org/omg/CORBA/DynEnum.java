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
 * Represents a <tt>DynAny</tt> object associated with an IDL enum.
 *
 * @deprecated Use the new <a href="../DynamicAny/DynEnum.html">DynEnum</a> instead
 */

// @Deprecated
public interface DynEnum extends org.omg.CORBA.Object, org.omg.CORBA.DynAny {
    /**
     * Return the value of the IDL enum stored in this <code>DynEnum</code> as a string.
     *
     * @return the stringified value.
     */
    public String value_as_string();

    /**
     * Set a particular enum in this <code>DynEnum</code>.
     *
     * @param arg the string corresponding to the value.
     */
    public void value_as_string(String arg);

    /**
     * Return the value of the IDL enum as a Java int.
     *
     * @return the integer value.
     */
    public int value_as_ulong();

    /**
     * Set the value of the IDL enum.
     *
     * @param arg the int value of the enum.
     */
    public void value_as_ulong(int arg);
}
