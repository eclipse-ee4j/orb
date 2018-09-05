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



import com.sun.corba.ee.spi.orb.ORB ;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DynAnyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.DynamicAny.DynAnyFactory
{
    private static final long serialVersionUID = 5207021167805787406L;

    //
    // Instance variables
    //

    private transient ORB orb;

    //
    // Constructors
    //

    private DynAnyFactoryImpl() {
        this.orb = null;
    }

    public DynAnyFactoryImpl(ORB orb) {
        this.orb = orb;
    }

    // Present only to get rid of FindBugs error
    private void readObject( ObjectInputStream is ) throws IOException,
        ClassNotFoundException {
        this.orb = null ;
    }
    //
    // DynAnyFactory interface methods
    //

    // Returns the most derived DynAny type based on the Anys TypeCode.
    public org.omg.DynamicAny.DynAny create_dyn_any (org.omg.CORBA.Any any)
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        return DynAnyUtil.createMostDerivedDynAny(any, orb, true);
    }

    // Returns the most derived DynAny type based on the TypeCode.
    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code (org.omg.CORBA.TypeCode type)
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        return DynAnyUtil.createMostDerivedDynAny(type, orb);
    }

    // Needed for org.omg.CORBA.Object

    private final String[] __ids = { "IDL:omg.org/DynamicAny/DynAnyFactory:1.0" };

    public String[] _ids() {
        return __ids.clone() ;
    }
}
