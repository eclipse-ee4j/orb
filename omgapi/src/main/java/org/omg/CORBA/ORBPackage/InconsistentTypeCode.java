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

package org.omg.CORBA.ORBPackage;

/**
 * InconsistentTypeCode is thrown when an attempt is made to create a
 * dynamic any with a type code that does not match the particular
 * subclass of <code>DynAny</code>.
 */
// @SuppressWarnings({"serial"})
public final class InconsistentTypeCode
    extends org.omg.CORBA.UserException {
    /**
     * Constructs an <code>InconsistentTypeCode</code> user exception
     * with no reason message.
    */
    public InconsistentTypeCode() {
        super();
    }

    /**
    * Constructs an <code>InconsistentTypeCode</code> user exception
    * with the specified reason message.
    * @param reason The String containing a reason message
    */
    public InconsistentTypeCode(String reason) {
        super(reason);
    }
}
