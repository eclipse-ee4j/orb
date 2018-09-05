/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * This is the symbol table entry for the #pragma statement.
 **/
public class PragmaEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected PragmaEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } // ctor

  protected PragmaEntry (com.sun.tools.corba.ee.idl.SymtabEntry that)
  {
    super (that, new com.sun.tools.corba.ee.idl.IDLID());
    module (that.name ());
    name ("");
  } // ctor

  protected PragmaEntry (PragmaEntry that)
  {
    super (that);
  } // ctor

  public Object clone ()
  {
    return new PragmaEntry (this);
  } // clone

  /** Invoke the Include type generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    pragmaGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the Include type generator.
      @return an object which implements the IncludeGen interface.
      @see com.sun.tools.corba.ee.idl.IncludeGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return pragmaGen;
  } // generator

  public String data ()
  {
    return _data;
  } // data

  public void data (String newData)
  {
    _data = newData;
  } // data

  static com.sun.tools.corba.ee.idl.PragmaGen pragmaGen;

  private String _data = null;
} // class PragmaEntry
