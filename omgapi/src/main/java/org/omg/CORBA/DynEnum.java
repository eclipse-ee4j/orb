/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package org.omg.CORBA;

/** Represents a <tt>DynAny</tt> object  associated
 *  with an IDL enum.
 * @deprecated Use the new <a href="../DynamicAny/DynEnum.html">DynEnum</a> instead
 */

// @Deprecated
public interface DynEnum extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{
    /**
     * Return the value of the IDL enum stored in this
     * <code>DynEnum</code> as a string.
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
