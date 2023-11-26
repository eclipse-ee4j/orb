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
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

import com.sun.corba.ee.spi.orb.ORB;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynValueBox;

public class DynValueBoxImpl extends DynValueCommonImpl implements DynValueBox {
    private static final long serialVersionUID = 670401668768259219L;
    //
    // Constructors
    //

    private DynValueBoxImpl() {
        this(null, (Any) null, false);
    }

    protected DynValueBoxImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }

    protected DynValueBoxImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }

    //
    // DynValueBox methods
    //

    public Any get_boxed_value() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitAny();
        return any;
    }

    public void set_boxed_value(org.omg.CORBA.Any boxed) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        if (!isNull && !boxed.type().equal(this.type())) {
            throw new TypeMismatch();
        }
        clearData();
        any = boxed;
        representations = REPRESENTATION_ANY;
        index = 0;
        isNull = false;
    }

    public DynAny get_boxed_value_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return components[0];
    }

    public void set_boxed_value_as_dyn_any(DynAny boxed) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        if (!isNull && !boxed.type().equal(this.type())) {
            throw new TypeMismatch();
        }
        clearData();
        components = new DynAny[] { boxed };
        representations = REPRESENTATION_COMPONENTS;
        index = 0;
        isNull = false;
    }

    @Override
    protected boolean initializeComponentsFromAny() {
        try {
            components = new DynAny[] { DynAnyUtil.createMostDerivedDynAny(any, orb, false) };
        } catch (InconsistentTypeCode ictc) {
            return false; // impossible
        }
        return true;
    }

    @Override
    protected boolean initializeComponentsFromTypeCode() {
        try {
            any = DynAnyUtil.createDefaultAnyOfType(any.type(), orb);
            components = new DynAny[] { DynAnyUtil.createMostDerivedDynAny(any, orb, false) };
        } catch (InconsistentTypeCode ictc) {
            return false; // impossible
        }
        return true;
    }

    @Override
    protected boolean initializeAnyFromComponents() {
        any = getAny(components[0]);
        return true;
    }
}
