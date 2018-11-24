/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.cosnaming;

import org.omg.CORBA.Object;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.NameComponent;

/**
 * Class InternalBindingKey acts as a container for two objects, namely a org.omg.CosNaming::Binding and an CORBA object
 * reference, which are the two components associated with the binding.
 */
public class InternalBindingValue {
    public Binding theBinding;
    public String strObjectRef;
    public org.omg.CORBA.Object theObjectRef;

    // Default constructor
    public InternalBindingValue() {
    }

    // Normal constructor
    public InternalBindingValue(Binding b, String o) {
        theBinding = b;
        strObjectRef = o;
    }
}
