/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.corba;

import org.omg.CORBA.Any;
import org.omg.CORBA.Context;
import org.omg.CORBA.NVList;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

public final class ContextImpl extends Context {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private org.omg.CORBA.ORB _orb;

    public ContextImpl(org.omg.CORBA.ORB orb) {
        _orb = orb;
    }

    public ContextImpl(Context parent) {
        // Ignore: no wrapper available
    }

    public String context_name() {
        throw wrapper.contextNotImplemented();
    }

    public Context parent() {
        throw wrapper.contextNotImplemented();
    }

    public Context create_child(String name) {
        throw wrapper.contextNotImplemented();
    }

    public void set_one_value(String propName, Any propValue) {
        throw wrapper.contextNotImplemented();
    }

    public void set_values(NVList values) {
        throw wrapper.contextNotImplemented();
    }

    public void delete_values(String propName) {
        throw wrapper.contextNotImplemented();
    }

    public NVList get_values(String startScope, int opFlags, String propName) {
        throw wrapper.contextNotImplemented();
    }
};
