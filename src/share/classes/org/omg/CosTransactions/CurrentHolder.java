/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CosTransactions;

/** CurrentHolder is the holder for Current objects.
 *  Current is a pseudo object, hence it will never be transmitted
 *  over the wire. 
 */
public final class CurrentHolder implements org.omg.CORBA.portable.Streamable
{
    //  instance variable 
    public org.omg.CosTransactions.Current value;
    //  constructors 
    public CurrentHolder() {
        this(null);
    }
    public CurrentHolder(org.omg.CosTransactions.Current __arg) {
        value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.omg.CosTransactions.CurrentHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.omg.CosTransactions.CurrentHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.CosTransactions.CurrentHelper.type();
    }
}
