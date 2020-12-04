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

/** 
 * The representation of a <code>DynAny</code> object that is associated
 * with an IDL sequence.
 * @deprecated Use the new <a href="../DynamicAny/DynSequence.html">DynSequence</a> instead
 */
// @Deprecated
public interface DynSequence extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{

    /**
     * Returns the length of the sequence represented by this
     * <code>DynFixed</code> object.
     *
     * @return the length of the sequence
     */
    public int length();

    /**
     * Sets the length of the sequence represented by this
     * <code>DynFixed</code> object to the given argument.
     *
     * @param arg the length of the sequence
     */
    public void length(int arg);

    /**
     * Returns the value of every element in this sequence.
     *
     * @return an array of <code>Any</code> objects containing the values in
         *         the sequence
         * @see #set_elements
     */
    public org.omg.CORBA.Any[] get_elements();

    /**
     * Sets the values of all elements in this sequence with the given
         * array.
     *
     * @param value the array of <code>Any</code> objects to be set
     * @exception org.omg.CORBA.DynAnyPackage.InvalidSeq if the array of values is bad
     * @see #get_elements
     */
    public void set_elements(org.omg.CORBA.Any[] value)
        throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
