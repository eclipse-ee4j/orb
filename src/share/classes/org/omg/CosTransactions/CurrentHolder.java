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
