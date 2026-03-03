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

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

/**
 * The Holder for <tt>Long</tt>.  For more information on 
 * Holder files, see <a href="doc-files/generatedfiles.html#holder">
 * "Generated Files: Holder Files"</a>.<P>
 * A Holder class for a <code>long</code>
 * that is used to store "out" and "inout" parameters in IDL methods.
 * If an IDL method signature has an IDL <code>long long</code> as an "out"
 * or "inout" parameter, the programmer must pass an instance of
 * <code>LongHolder</code> as the corresponding
 * parameter in the method invocation; for "inout" parameters, the programmer
 * must also fill the "in" value to be sent to the server.
 * Before the method invocation returns, the ORB will fill in the
 * value corresponding to the "out" value returned from the server.
 * <P>
 * If <code>myLongHolder</code> is an instance of <code>LongHolder</code>,
 * the value stored in its <code>value</code> field can be accessed with
 * <code>myLongHolder.value</code>.
 *
 * @version     1.14, 09/09/97
 * @since       JDK1.2
 */
public final class LongHolder implements Streamable {

    /**
     * The <code>long</code> value held by this <code>LongHolder</code>
     * object.
     */
    public long value;

    /**
     * Constructs a new <code>LongHolder</code> object with its
     * <code>value</code> field initialized to <code>0</code>.
     */
    public LongHolder() {
    }

    /**
     * Constructs a new <code>LongHolder</code> object with its
     * <code>value</code> field initialized to the given
     * <code>long</code>.
     * @param initial the <code>long</code> with which to initialize
     *                the <code>value</code> field of the newly-created
     *                <code>LongHolder</code> object
     */
    public LongHolder(long initial) {
        value = initial;
    }

    /**
     * Reads from <code>input</code> and initalizes the value in the Holder
     * with the unmarshalled data.
     *
     * @param input the InputStream containing CDR formatted data from the wire
     */
    public void _read(InputStream input) {
        value = input.read_longlong();
    }

    /**
     * Marshals to <code>output</code> the value in the Holder.
     *
     * @param output the OutputStream which will contain the CDR formatted data
     */
    public void _write(OutputStream output) {
        output.write_longlong(value);
    }

    /**
     * Returns the <code>TypeCode</code> object 
     * corresponding to the value held in the Holder.
     *
     * @return    the TypeCode of the value held in the holder
     */
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_longlong);
    }

}
