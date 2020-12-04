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

package org.omg.CORBA.portable;

import org.omg.CORBA.TypeCode;

/**
 * The base class for the Holder classess of all complex
 * IDL types. The ORB treats all generated Holders as Streamable to invoke
 * the methods for marshalling and unmarshalling.
 *
 * @version 1.11, 03/18/98
 * @since   JDK1.2
 */

public interface Streamable {
    /**
     * Reads data from <code>istream</code> and initalizes the
     * <code>value</code> field of the Holder with the unmarshalled data.
     *
     * @param     istream   the InputStream that represents the CDR data from the wire.
     */
    void _read(InputStream istream);
    /**
     * Marshals to <code>ostream</code> the value in the 
     * <code>value</code> field of the Holder.
     *
     * @param     ostream   the CDR OutputStream
     */
    void _write(OutputStream ostream);

    /**
     * Retrieves the <code>TypeCode</code> object corresponding to the value
     * in the <code>value</code> field of the Holder.
     *
     * @return    the <code>TypeCode</code> object for the value held in the holder
     */
    TypeCode _type();
}
