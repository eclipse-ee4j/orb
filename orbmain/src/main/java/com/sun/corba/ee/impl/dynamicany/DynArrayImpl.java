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
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.portable.InputStream;
import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

import com.sun.corba.ee.spi.orb.ORB;

public class DynArrayImpl extends DynAnyCollectionImpl implements DynArray {
    private static final long serialVersionUID = -5898255222474271560L;
    //
    // Constructors
    //

    protected DynArrayImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }

    protected DynArrayImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }

    // Initializes components and anys representation
    // from the Any representation
    protected boolean initializeComponentsFromAny() {
        int length = getBound();
        TypeCode contentType = getContentType();
        InputStream input;

        try {
            input = any.create_input_stream();
        } catch (BAD_OPERATION e) {
            return false;
        }

        components = new DynAny[length];
        anys = new Any[length];

        for (int i = 0; i < length; i++) {
            // _REVISIT_ Could use read_xxx_array() methods on InputStream for efficiency
            // but only for primitive types
            anys[i] = DynAnyUtil.extractAnyFromStream(contentType, input, orb);
            try {
                // Creates the appropriate subtype without copying the Any
                components[i] = DynAnyUtil.createMostDerivedDynAny(anys[i], orb, false);
            } catch (InconsistentTypeCode itc) { // impossible
            }
        }
        return true;
    }

    // Initializes components and anys representation
    // from the internal TypeCode information with default values.
    // This is not done recursively, only one level.
    // More levels are initialized lazily, on demand.
    protected boolean initializeComponentsFromTypeCode() {
        int length = getBound();
        TypeCode contentType = getContentType();

        components = new DynAny[length];
        anys = new Any[length];

        for (int i = 0; i < length; i++) {
            createDefaultComponentAt(i, contentType);
        }
        return true;
    }

    //
    // DynArray interface methods
    //

    // Initializes the elements of the array.
    // If value does not contain the same number of elements as the array dimension,
    // the operation raises InvalidValue.
    // If one or more elements have a type that is inconsistent with the DynArrays TypeCode,
    // the operation raises TypeMismatch.
    // This operation does not change the current position.
    /*
     * public void set_elements (org.omg.CORBA.Any[] value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
     * org.omg.DynamicAny.DynAnyPackage.InvalidValue;
     */

    //
    // Utility methods
    //

    protected void checkValue(Object[] value) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (value == null || value.length != getBound()) {
            throw new InvalidValue();
        }
    }
}
