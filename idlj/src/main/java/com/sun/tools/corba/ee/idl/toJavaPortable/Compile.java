/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl.toJavaPortable;

/**
 * A class simply to redirect to the new location of the Compiler.
 * @deprecated use {@link org.glassfish.corba.idl.toJavaPortable.Compile}
 */
@Deprecated
public class Compile extends org.glassfish.corba.idl.toJavaPortable.Compile {

  public static void main (String[] args)
  {
    Compile compiler = new Compile();
    compiler.start (args);
  } // main
}
