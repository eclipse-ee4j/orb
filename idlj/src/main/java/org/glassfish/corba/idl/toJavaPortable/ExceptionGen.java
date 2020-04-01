/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
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

import org.glassfish.corba.idl.ExceptionEntry;

/**
 *
 **/
public class ExceptionGen extends StructGen implements org.glassfish.corba.idl.ExceptionGen
{
  /**
   * Public zero-argument constructor.
   **/
  public ExceptionGen ()
  {
    super (true);
  } // ctor

  /**
   *
   **/
  public void generate (Hashtable symbolTable, ExceptionEntry entry, PrintWriter stream)
  {
    super.generate (symbolTable, entry, stream);
  } // generate
} // class ExceptionGen
