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
 * A class that contains information about the identity of the client, for access control and other purposes. It
 * contains a single attribute, the name of the <code>Principal</code>, encoded as a sequence of bytes.
 * <P>
 *
 * @deprecated Deprecated by CORBA 2.2.
 */

// @Deprecated
public class Principal {
    /**
     * Sets the name of this <code>Principal</code> object to the given value.
     *
     * @param value the value to be set in the <code>Principal</code>
     * @deprecated Deprecated by CORBA 2.2.
     */
    // @Deprecated
    public void name(byte[] value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Gets the name of this <code>Principal</code> object.
     *
     * @return the name of this <code>Principal</code> object
     * @deprecated Deprecated by CORBA 2.2.
     */
    // @Deprecated
    public byte[] name() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
