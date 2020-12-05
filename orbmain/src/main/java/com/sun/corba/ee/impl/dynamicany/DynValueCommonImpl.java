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
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

import com.sun.corba.ee.spi.orb.ORB ;
import org.omg.DynamicAny.DynValueCommon;

abstract class DynValueCommonImpl extends DynAnyComplexImpl implements DynValueCommon
{
    private static final long serialVersionUID = -6538058649606934141L;
    //
    // Constructors
    //

    protected boolean isNull;

    private DynValueCommonImpl() {
        this(null, (Any)null, false);
        isNull = true;
    }

    protected DynValueCommonImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
        isNull = checkInitComponents();
    }

    protected DynValueCommonImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        isNull = true;
    }

    //
    // DynValueCommon methods
    //

    // Returns TRUE if this object represents a null valuetype
    public boolean is_null() {
        return isNull;
    }

    // Changes the representation to a null valuetype.
    public void set_to_null() {
        isNull = true;
        clearData();
    }

    // If this object represents a null valuetype then this operation
    // replaces it with a newly constructed value with its components 
    // initialized to default values as in DynAnyFactory::create_dyn_any_from_type_code.
    // If this object represents a non-null valuetype, then this operation has no effect. 
    public void set_to_value() {
        if (isNull) {
            isNull = false;
            // the rest is done lazily
        }
        // else: there is nothing to do
    }

    //
    // Methods differing from DynStruct
    //

    // Required to raise InvalidValue if this is a null value type.
    public org.omg.DynamicAny.NameValuePair[] get_members ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return nameValuePairs;
    }

    // Required to raise InvalidValue if this is a null value type.
    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return nameDynAnyPairs;
    }

    //
    // Overridden methods
    //

    // Overridden to change to non-null status.
    @Override
    public void set_members (org.omg.DynamicAny.NameValuePair[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        super.set_members(value);
        // If we didn't get an exception then this must be a valid non-null value
        isNull = false;
    }

    // Overridden to change to non-null status.
    @Override
    public void set_members_as_dyn_any (org.omg.DynamicAny.NameDynAnyPair[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        super.set_members_as_dyn_any(value);
        // If we didn't get an exception then this must be a valid non-null value
        isNull = false;
    }
}
