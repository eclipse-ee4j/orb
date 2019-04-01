/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.org.omg.CORBA;

/**
 * com/sun/org/omg/CORBA/OperationDescription.java Generated by the IDL-to-Java compiler (portable), version "3.0" from
 * ir.idl Thursday, May 6, 1999 1:51:51 AM PDT
 */

public final class OperationDescription implements org.omg.CORBA.portable.IDLEntity {
    public String name = null;
    public String id = null;
    public String defined_in = null;
    public String version = null;
    public org.omg.CORBA.TypeCode result = null;
    public com.sun.org.omg.CORBA.OperationMode mode = null;
    public String contexts[] = null;
    public com.sun.org.omg.CORBA.ParameterDescription parameters[] = null;
    public com.sun.org.omg.CORBA.ExceptionDescription exceptions[] = null;

    public OperationDescription() {
    } // ctor

    public OperationDescription(String _name, String _id, String _defined_in, String _version, org.omg.CORBA.TypeCode _result,
            com.sun.org.omg.CORBA.OperationMode _mode, String[] _contexts, com.sun.org.omg.CORBA.ParameterDescription[] _parameters,
            com.sun.org.omg.CORBA.ExceptionDescription[] _exceptions) {
        name = _name;
        id = _id;
        defined_in = _defined_in;
        version = _version;
        result = _result;
        mode = _mode;
        contexts = _contexts;
        parameters = _parameters;
        exceptions = _exceptions;
    } // ctor

} // class OperationDescription
