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

package org.omg.CORBA.TypeCodePackage;

/**
 * The exception <code>BadKind</code> is thrown when 
 * an inappropriate operation is invoked on a <code>TypeCode</code> object. For example,
 * invoking the method <code>discriminator_type()</code> on an instance of
 * <code>TypeCode</code> that does not represent an IDL union will cause the
 * exception <code>BadKind</code> to be thrown.
 *
 * @see org.omg.CORBA.TypeCode
 * @version 1.7, 03/18/98
 * @since   JDK1.2
 */

// @SuppressWarnings({"serial"})
public final class BadKind extends org.omg.CORBA.UserException {
    /**
     * Constructs a <code>BadKind</code> exception with no reason message.
     */
    public BadKind() {
        super();
    }

    /**
     * Constructs a <code>BadKind</code> exception with the specified 
     * reason message.
     * @param reason the String containing a reason message
     */
    public BadKind(String reason) {
        super(reason);
    }
}
