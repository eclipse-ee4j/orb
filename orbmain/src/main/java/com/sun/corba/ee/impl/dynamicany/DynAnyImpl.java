/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.dynamicany;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.OutputStream;

import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

abstract class DynAnyImpl extends org.omg.CORBA.LocalObject implements DynAny
{
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static final long serialVersionUID = 7435214669604617358L;

    protected static final int NO_INDEX = -1;
    // A DynAny is destroyable if it is the root of a DynAny hierarchy.
    protected static final byte STATUS_DESTROYABLE = 0;
    // A DynAny is undestroyable if it is a node in a DynAny hierarchy other than the root.
    protected static final byte STATUS_UNDESTROYABLE = 1;
    // A DynAny is destroyed if its root has been destroyed.
    protected static final byte STATUS_DESTROYED = 2;

    //
    // Instance variables
    //

    protected ORB orb = null;

    // An Any is used internally to implement the basic DynAny.
    // It stores the DynAnys TypeCode.
    // For primitive types it is the only representation.
    // For complex types it is the streamed representation.
    protected Any any = null;
    // Destroyable is the default status for free standing DynAnys.
    protected byte status = STATUS_DESTROYABLE;
    protected int index = NO_INDEX;

    //
    // Constructors
    //

    protected DynAnyImpl() {
    }

    protected DynAnyImpl(ORB orb, Any any, boolean copyValue) {
        this.orb = orb;

        if (copyValue) {
            this.any = DynAnyUtil.copy(any, orb);
        } else {
            this.any = any;
        }
        // set the current position to 0 if any has components, otherwise to -1.
        index = NO_INDEX;
    }

    protected DynAnyImpl(ORB orb, TypeCode typeCode) {
        this.orb = orb;

        this.any = DynAnyUtil.createDefaultAnyOfType(typeCode, orb);
    }

    protected DynAnyFactory factory() {
        try {
            return (DynAnyFactory)orb.resolve_initial_references( 
                ORBConstants.DYN_ANY_FACTORY_NAME );
        } catch (InvalidName in) {
            throw new RuntimeException("Unable to find DynAnyFactory");
        }
    }

    protected Any getAny() {
        return any;
    }

    // Uses getAny() if this is our implementation, otherwise uses to_any()
    // which copies the Any.
    protected Any getAny(DynAny dynAny) {
        if (dynAny instanceof DynAnyImpl) {
            return ((DynAnyImpl)dynAny).getAny();
        } else {
            return dynAny.to_any();
        }
    }

    protected void writeAny(OutputStream out) {
        //System.out.println(this + " writeAny of type " + type().kind().value());
        any.write_value(out);
    }

    protected void setStatus(byte newStatus) {
        status = newStatus;
    }

    protected void clearData() {
        // This clears the data part of the Any while keeping the TypeCode info.
        any.type(any.type());
    }

    //
    // DynAny interface methods
    //

    public org.omg.CORBA.TypeCode type() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return any.type();
    }

    // Makes a copy of the Any value inside the parameter
    public void assign (org.omg.DynamicAny.DynAny dyn_any)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ((any != null) && (! any.type().equal(dyn_any.type()))) {
            throw new TypeMismatch();
        }
        any = dyn_any.to_any();
    }

    // Makes a copy of the Any parameter
    public void from_any (org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ((any != null) && (! any.type().equal(value.type()))) {
            throw new TypeMismatch();
        }
        // If the passed Any does not contain a legal value
        // (such as a null string), the operation raises InvalidValue.
        Any tempAny = null;
        try {
            tempAny = DynAnyUtil.copy(value, orb);
        } catch (Exception e) {
            throw new InvalidValue();
        }
        if ( ! DynAnyUtil.isInitialized(tempAny)) {
            throw new InvalidValue();
        }
        any = tempAny;
   }

    public abstract org.omg.CORBA.Any to_any();
    public abstract boolean equal (org.omg.DynamicAny.DynAny dyn_any);
    public abstract void destroy();
    public abstract org.omg.DynamicAny.DynAny copy();

    // Needed for org.omg.CORBA.Object

    private final String[] __ids = { "IDL:omg.org/DynamicAny/DynAny:1.0" };

    public String[] _ids() {
        return __ids.clone();
    }
}
