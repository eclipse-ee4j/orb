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
import java.util.Enumeration;
import java.util.Hashtable;

import org.glassfish.corba.idl.ModuleEntry;
import org.glassfish.corba.idl.SymtabEntry;

/**
 *
 **/
public class ModuleGen implements org.glassfish.corba.idl.ModuleGen
{
  /**
   * Public zero-argument constructor.
   **/
  public ModuleGen ()
  {
  } // ctor

  /**
   * Generate Java code for all members of an IDL module.
   **/
  public void generate (Hashtable symbolTable, ModuleEntry entry, PrintWriter stream)
  {
    // Generate the package directory
    String name = Util.containerFullName(entry) ;
    Util.mkdir(name);

    // Generate all of the contained types
    Enumeration e = entry.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry element = (SymtabEntry)e.nextElement ();
      if (element.emit ())
        element.generate (symbolTable, stream);
    }
  } // generate
} // class ModuleGen
