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

package org.omg.CORBA.DynAnyPackage;

/**
 * TypeMismatch is thrown by dynamic any accessor methods when
 * type of the actual contents do not match what is trying to be
 * accessed.  
 */
// @SuppressWarnings({"serial"})
public final class TypeMismatch
    extends org.omg.CORBA.UserException {

    /**
     * Constructs a <code>TypeMismatch</code> object.
     */
    public TypeMismatch() {
        super();
    }

    /**
     * Constructs a <code>TypeMismatch</code> object.
     * @param reason  a <code>String</code> giving more information
     * regarding the exception.
     */
    public TypeMismatch(String reason) {
        super(reason);
    }
}
