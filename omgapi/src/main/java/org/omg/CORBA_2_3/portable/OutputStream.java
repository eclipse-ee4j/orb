/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA_2_3.portable;

/**
 * OutputStream provides interface for writing of all of the mapped IDL type to the stream. It extends
 * org.omg.CORBA.portable.OutputStream, and defines new methods defined by CORBA 2.3.
 *
 * @see org.omg.CORBA.portable.OutputStream
 * @author OMG
 * @version 1.19 07/27/07
 * @since JDK1.2
 */

public abstract class OutputStream extends org.omg.CORBA.portable.OutputStream {

    /**
     * Marshals a value type to the output stream.
     *
     * @param value is the acutal value to write
     */
    public void write_value(java.io.Serializable value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Marshals a value type to the output stream.
     *
     * @param value is the acutal value to write
     * @param clz is the declared type of the value to be marshaled
     */
    public void write_value(java.io.Serializable value, java.lang.Class clz) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Marshals a value type to the output stream.
     *
     * @param value is the acutal value to write
     * @param repository_id identifies the type of the value type to be marshaled
     */
    public void write_value(java.io.Serializable value, String repository_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Marshals a value type to the output stream.
     *
     * @param value is the acutal value to write
     * @param factory is the instance of the helper to be used for marshaling the boxed value
     */
    public void write_value(java.io.Serializable value, org.omg.CORBA.portable.BoxedValueHelper factory) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Marshals a value object or a stub object.
     *
     * @param obj the actual value object to marshal or the stub to be marshalled
     */
    public void write_abstract_interface(java.lang.Object obj) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
