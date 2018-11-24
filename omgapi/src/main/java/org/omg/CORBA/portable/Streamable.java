/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.portable;

import org.omg.CORBA.TypeCode;

/**
 * The base class for the Holder classess of all complex IDL types. The ORB treats all generated Holders as Streamable
 * to invoke the methods for marshalling and unmarshalling.
 *
 * @version 1.11, 03/18/98
 * @since JDK1.2
 */

public interface Streamable {
    /**
     * Reads data from <code>istream</code> and initalizes the <code>value</code> field of the Holder with the unmarshalled
     * data.
     *
     * @param istream the InputStream that represents the CDR data from the wire.
     */
    void _read(InputStream istream);

    /**
     * Marshals to <code>ostream</code> the value in the <code>value</code> field of the Holder.
     *
     * @param ostream the CDR OutputStream
     */
    void _write(OutputStream ostream);

    /**
     * Retrieves the <code>TypeCode</code> object corresponding to the value in the <code>value</code> field of the Holder.
     *
     * @return the <code>TypeCode</code> object for the value held in the holder
     */
    TypeCode _type();
}
