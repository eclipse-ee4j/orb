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
 * Provides the <code>TypeCode</code> operations <code>member_name()</code>,
 * <code>member_type()</code>, and <code>member_label</code>.
 * These methods
 * raise <code>Bounds</code> when the index parameter is greater than or equal
 * to the number of members constituting the type.
 *
 * @version 1.7, 03/18/98
 * @since   JDK1.2
 */

// @SuppressWarnings({"serial"})
public final class Bounds extends org.omg.CORBA.UserException {

    /**
     * Constructs a <code>Bounds</code> exception with no reason message.
     */
    public Bounds() {
        super();
    }

    /**
     * Constructs a <code>Bounds</code> exception with the specified 
     * reason message.
     * @param reason the String containing a reason message
     */
    public Bounds(String reason) {
        super(reason);
    }
}
