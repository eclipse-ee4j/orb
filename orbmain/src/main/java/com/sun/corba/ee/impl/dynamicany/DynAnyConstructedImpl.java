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

import com.sun.corba.ee.impl.corba.TypeCodeImpl; // needed for recursive type codes
import com.sun.corba.ee.spi.orb.ORB;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.OutputStream;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

abstract class DynAnyConstructedImpl extends DynAnyImpl {
    private static final long serialVersionUID = -5868871025693895861L;

    protected static final byte REPRESENTATION_NONE = 0;
    protected static final byte REPRESENTATION_TYPECODE = 1;
    protected static final byte REPRESENTATION_ANY = 2;
    protected static final byte REPRESENTATION_COMPONENTS = 4;

    protected static final byte RECURSIVE_UNDEF = -1;
    protected static final byte RECURSIVE_NO = 0;
    protected static final byte RECURSIVE_YES = 1;

    protected static final DynAny[] emptyComponents = new DynAny[0];
    //
    // Instance variables
    //

    // Constructed DynAnys maintain an ordered collection of component DynAnys.
    DynAny[] components = emptyComponents;
    byte representations = REPRESENTATION_NONE;
    byte isRecursive = RECURSIVE_UNDEF;

    //
    // Constructors
    //
    protected DynAnyConstructedImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
        // System.out.println(this + " constructed with any " + any);
        if (this.any != null) {
            representations = REPRESENTATION_ANY;
        }
        // set the current position to 0 if any has components, otherwise to -1.
        index = 0;
    }

    protected DynAnyConstructedImpl(ORB orb, TypeCode typeCode) {
        // assertion: typeCode has been checked to be valid for this particular subclass.
        // note: We don't copy TypeCodes since they are considered immutable.
        super(orb, typeCode);
        if (typeCode != null) {
            representations = REPRESENTATION_TYPECODE;
        }
        // set the current position to 0 if any has components, otherwise to -1.
        index = NO_INDEX;

        // _REVISIT_ Would need REPRESENTATION_TYPECODE for lazy initialization
        // if ( ! isRecursive()) {
        // initializeComponentsFromTypeCode();
        // }
    }

    protected boolean isRecursive() {
        if (isRecursive == RECURSIVE_UNDEF) {
            TypeCode typeCode = any.type();
            if (typeCode instanceof TypeCodeImpl) {
                if (((TypeCodeImpl) typeCode).is_recursive()) {
                    isRecursive = RECURSIVE_YES;
                } else {
                    isRecursive = RECURSIVE_NO;
                }
            } else {
                // No way to find out unless the TypeCode spec changes.
                isRecursive = RECURSIVE_NO;
            }
        }
        return (isRecursive == RECURSIVE_YES);
    }

    //
    // DynAny traversal methods
    //

    @Override
    public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            return null;
        }
        return (checkInitComponents() ? components[index] : null);
    }

    @Override
    public int component_count() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        return (checkInitComponents() ? components.length : 0);
    }

    @Override
    public boolean next() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (!checkInitComponents()) {
            return false;
        }
        index++;
        if (index >= 0 && index < components.length) {
            return true;
        } else {
            index = NO_INDEX;
            return false;
        }
    }

    @Override
    public boolean seek(int newIndex) {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (newIndex < 0) {
            this.index = NO_INDEX;
            return false;
        }
        if (!checkInitComponents()) {
            return false;
        }
        if (newIndex < components.length) {
            index = newIndex;
            return true;
        }
        return false;
    }

    @Override
    public void rewind() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        this.seek(0);
    }

    //
    // Utility methods
    //

    @Override
    protected void clearData() {
        super.clearData();
        // _REVISIT_ What about status?
        components = emptyComponents;
        index = NO_INDEX;
        representations = REPRESENTATION_NONE;
    }

    @Override
    protected void writeAny(OutputStream out) {
        // If all we got is TypeCode representation (no value)
        // then we don't want to force creating a default value
        // System.out.println(this + " checkInitAny before writeAny");
        checkInitAny();
        super.writeAny(out);
    }

    // Makes sure that the components representation is initialized
    protected boolean checkInitComponents() {
        if ((representations & REPRESENTATION_COMPONENTS) == 0) {
            if ((representations & REPRESENTATION_ANY) != 0) {
                if (initializeComponentsFromAny()) {
                    representations |= REPRESENTATION_COMPONENTS;
                } else {
                    return false;
                }
            } else if ((representations & REPRESENTATION_TYPECODE) != 0) {
                if (initializeComponentsFromTypeCode()) {
                    representations |= REPRESENTATION_COMPONENTS;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    // Makes sure that the Any representation is initialized
    protected void checkInitAny() {
        if ((representations & REPRESENTATION_ANY) == 0) {
            // System.out.println(this + " checkInitAny: reps does not have REPRESENTATION_ANY");
            if ((representations & REPRESENTATION_COMPONENTS) != 0) {
                // System.out.println(this + " checkInitAny: reps has REPRESENTATION_COMPONENTS");
                if (initializeAnyFromComponents()) {
                    representations |= REPRESENTATION_ANY;
                }
            } else if ((representations & REPRESENTATION_TYPECODE) != 0) {
                // System.out.println(this + " checkInitAny: reps has REPRESENTATION_TYPECODE");
                if (representations == REPRESENTATION_TYPECODE && isRecursive()) {
                    return;
                }
                if (initializeComponentsFromTypeCode()) {
                    representations |= REPRESENTATION_COMPONENTS;
                }
                if (initializeAnyFromComponents()) {
                    representations |= REPRESENTATION_ANY;
                }
            }
        } else {
            // System.out.println(this + " checkInitAny: reps != REPRESENTATION_ANY");
        }
        return;
    }

    protected abstract boolean initializeComponentsFromAny();

    protected abstract boolean initializeComponentsFromTypeCode();

    // Collapses the whole DynAny hierarchys values into one single streamed Any
    protected boolean initializeAnyFromComponents() {
        // System.out.println(this + " initializeAnyFromComponents");
        OutputStream out = any.create_output_stream();
        for (DynAny component : components) {
            if (component instanceof DynAnyImpl) {
                ((DynAnyImpl) component).writeAny(out);
            } else {
                // Not our implementation. Nothing we can do to prevent copying.
                component.to_any().write_value(out);
            }
        }
        any.read_value(out.create_input_stream(), any.type());
        return true;
    }

    //
    // DynAny interface methods
    //

    @Override
    public void assign(org.omg.DynamicAny.DynAny dyn_any) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        clearData();
        super.assign(dyn_any);
        representations = REPRESENTATION_ANY;
        index = 0;
    }

    @Override
    public void from_any(org.omg.CORBA.Any value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        clearData();
        super.from_any(value);
        representations = REPRESENTATION_ANY;
        index = 0;
    }

    // Spec: Returns a copy of the internal Any
    @Override
    public org.omg.CORBA.Any to_any() {
        // System.out.println(this + " to_any ");
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        checkInitAny();
        // Anys value may still be uninitialized if DynAny was initialized by TypeCode only
        return DynAnyUtil.copy(any, orb);
    }

    @Override
    public boolean equal(org.omg.DynamicAny.DynAny dyn_any) {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (dyn_any == this) {
            return true;
        }
        // This changes the current position of dyn_any.
        // Make sure that our position isn't changed.
        if (!any.type().equal(dyn_any.type()) || !checkInitComponents()) {
            return false;
        }
        DynAny currentComponent = null;
        try {
            // Remember the current position to restore it later
            currentComponent = dyn_any.current_component();
            for (int i = 0; i < components.length; i++) {
                if (!dyn_any.seek(i)) {
                    return false;
                }
                // System.out.println(this + " comparing component " + i + "=" + components[i] +
                // " of type " + components[i].type().kind().value());
                if (!components[i].equal(dyn_any.current_component())) {
                    // System.out.println("Not equal component " + i);
                    return false;
                }
            }
        } catch (TypeMismatch tm) {
            // impossible, we checked the type codes already
        } finally {
            // Restore the current position of the other DynAny
            DynAnyUtil.set_current_component(dyn_any, currentComponent);
        }
        return true;
    }

    @Override
    public void destroy() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (status == STATUS_DESTROYABLE) {
            status = STATUS_DESTROYED;
            for (DynAny component : components) {
                if (component instanceof DynAnyImpl) {
                    ((DynAnyImpl) component).setStatus(STATUS_DESTROYABLE);
                }
                component.destroy();
            }
        }
    }

    @Override
    public org.omg.DynamicAny.DynAny copy() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        checkInitAny();
        try {
            return DynAnyUtil.createMostDerivedDynAny(any, orb, true);
        } catch (InconsistentTypeCode ictc) {
            return null; // impossible
        }
    }

    // getter / setter methods

    @Override
    public void insert_boolean(boolean value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_boolean(value);
    }

    @Override
    public void insert_octet(byte value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_octet(value);
    }

    @Override
    public void insert_char(char value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_char(value);
    }

    @Override
    public void insert_short(short value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_short(value);
    }

    @Override
    public void insert_ushort(short value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_ushort(value);
    }

    @Override
    public void insert_long(int value) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_long(value);
    }

    @Override
    public void insert_ulong(int value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_ulong(value);
    }

    @Override
    public void insert_float(float value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_float(value);
    }

    @Override
    public void insert_double(double value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_double(value);
    }

    @Override
    public void insert_string(String value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_string(value);
    }

    @Override
    public void insert_reference(org.omg.CORBA.Object value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_reference(value);
    }

    @Override
    public void insert_typecode(org.omg.CORBA.TypeCode value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_typecode(value);
    }

    @Override
    public void insert_longlong(long value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_longlong(value);
    }

    @Override
    public void insert_ulonglong(long value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_ulonglong(value);
    }

    @Override
    public void insert_wchar(char value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_wchar(value);
    }

    @Override
    public void insert_wstring(String value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_wstring(value);
    }

    @Override
    public void insert_any(org.omg.CORBA.Any value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_any(value);
    }

    @Override
    public void insert_dyn_any(org.omg.DynamicAny.DynAny value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_dyn_any(value);
    }

    @Override
    public void insert_val(java.io.Serializable value)
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        currentComponent.insert_val(value);
    }

    @Override
    public java.io.Serializable get_val()
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_val();
    }

    @Override
    public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_boolean();
    }

    @Override
    public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_octet();
    }

    @Override
    public char get_char() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_char();
    }

    @Override
    public short get_short() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_short();
    }

    @Override
    public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_ushort();
    }

    @Override
    public int get_long() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_long();
    }

    @Override
    public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_ulong();
    }

    @Override
    public float get_float() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_float();
    }

    @Override
    public double get_double() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_double();
    }

    @Override
    public String get_string() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_string();
    }

    @Override
    public org.omg.CORBA.Object get_reference()
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_reference();
    }

    @Override
    public org.omg.CORBA.TypeCode get_typecode()
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_typecode();
    }

    @Override
    public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_longlong();
    }

    @Override
    public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_ulonglong();
    }

    @Override
    public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_wchar();
    }

    @Override
    public String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_wstring();
    }

    @Override
    public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_any();
    }

    @Override
    public org.omg.DynamicAny.DynAny get_dyn_any()
            throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed();
        }
        if (index == NO_INDEX) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent)) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return currentComponent.get_dyn_any();
    }
}
