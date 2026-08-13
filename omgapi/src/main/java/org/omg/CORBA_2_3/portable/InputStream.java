/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package org.omg.CORBA_2_3.portable;

import java.io.Serializable;

/**
 * InputStream provides for the reading of all of the mapped IDL types from the stream. It extends
 * org.omg.CORBA.portable.InputStream. This class defines new methods that were added for CORBA 2.3.
 *
 * @see org.omg.CORBA.portable.InputStream
 * @author OMG
 * @version 1.17 07/27/07
 * @since JDK1.2
 */

public abstract class InputStream extends org.omg.CORBA.portable.InputStream {

    /**
     * Create a new instance of this class.
     */
    public InputStream() {
    }

    /**
     * Unmarshalls a value type from the input stream.
     *
     * @return the value type unmarshalled from the input stream
     */
    public Serializable read_value() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Unmarshalls a value type from the input stream.
     *
     * @param clz is the declared type of the value to be unmarshalled
     * @return the value unmarshalled from the input stream
     */
    public java.io.Serializable read_value(Class clz) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Unmarshalls a value type from the input stream.
     *
     * @param factory is the instance fo the helper to be used for unmarshalling the value type
     * @return the value unmarshalled from the input stream
     */
    public java.io.Serializable read_value(org.omg.CORBA.portable.BoxedValueHelper factory) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Unmarshalls a value type from the input stream.
     *
     * @param rep_id identifies the type of the value to be unmarshalled
     * @return value type unmarshalled from the input stream
     */
    public java.io.Serializable read_value(java.lang.String rep_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Unmarshalls a value type from the input stream.
     *
     * @param value is an uninitialized value which is added to the orb's indirection table before calling
     * Streamable._read() or CustomMarshal.unmarshal() to unmarshal the value.
     * @return value type unmarshalled from the input stream
     */
    public java.io.Serializable read_value(java.io.Serializable value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Unmarshal the value object or a suitable stub object.
     *
     * @return ORB runtime returns the value object or a suitable stub object.
     */
    public java.lang.Object read_abstract_interface() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Unmarshal the class object or the stub class corresponding to the passed type.
     *
     * @param clz is the Class object for the stub class which corresponds to the type that is statically expected.
     * @return ORB runtime returns the value object or a suitable stub object.
     */
    public java.lang.Object read_abstract_interface(Class clz) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
