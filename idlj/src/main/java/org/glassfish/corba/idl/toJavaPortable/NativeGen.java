/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.idl.toJavaPortable;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

import org.glassfish.corba.idl.NativeEntry;

/**
 *
 **/
public class NativeGen implements org.glassfish.corba.idl.NativeGen
{
  /**
   * Public zero-argument constructor.
   **/
  public NativeGen ()
  {
  } // ctor

  /**
   * Generate Java code for an IDL constant.  A constant is written to
   * a new class only when it is not a member of an interface; otherwise
   * it written to the interface class in which it resides.
   **/
  public void generate (Hashtable symbolTable, NativeEntry c, PrintWriter s)
  {
        // noop, do not generate anything
  } // generate

} // class NativeGen

