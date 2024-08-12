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

package com.sun.corba.ee.impl.dynamicany;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;

import com.sun.corba.ee.spi.orb.ORB;
import org.omg.DynamicAny.DynStruct;

public class DynStructImpl extends DynAnyComplexImpl implements DynStruct {
    private static final long serialVersionUID = 2832306671453429704L;

    //
    // Constructors
    //
    protected DynStructImpl(ORB orb, Any any, boolean copyValue) {
        // We can be sure that typeCode is of kind tk_struct
        super(orb, any, copyValue);
        // Initialize components lazily, on demand.
        // This is an optimization in case the user is only interested in storing Anys.
    }

    protected DynStructImpl(ORB orb, TypeCode typeCode) {
        // We can be sure that typeCode is of kind tk_struct
        super(orb, typeCode);
        // For DynStruct, the operation sets the current position to -1
        // for empty exceptions and to zero for all other TypeCodes.
        // The members (if any) are (recursively) initialized to their default values.
        index = 0;
    }

    //
    // Methods differing from DynValues
    //
    public org.omg.DynamicAny.NameValuePair[] get_members() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        checkInitComponents();
        return nameValuePairs.clone();
    }

    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        checkInitComponents();
        return nameDynAnyPairs.clone();
    }
}
