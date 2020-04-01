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

package org.glassfish.corba.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * This is the symbol table entry for forward declarations of values.
 **/
public class ForwardValueEntry extends ForwardEntry
{
  protected ForwardValueEntry ()
  {
    super ();
  } // ctor

  protected ForwardValueEntry (ForwardValueEntry that)
  {
    super (that);
  } // ctor

  protected ForwardValueEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
  } // ctor

  public Object clone ()
  {
    return new ForwardValueEntry (this);
  } // clone

  /** Invoke the forward value declaration generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    forwardValueGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the interface generator.
      @return an object which implements the ForwardValueGen interface.
      @see ValueGen */
  public Generator generator ()
  {
     return forwardValueGen;
  } // generator

  static ForwardValueGen forwardValueGen;
} // class ForwardValueEntry
