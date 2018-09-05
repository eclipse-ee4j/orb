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

import com.sun.tools.corba.ee.idl.constExpr.Expression;

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * This is the symbol table entry for constants.
 **/
public class ConstEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected ConstEntry ()
  {
    super ();
  } // ctor

  protected ConstEntry (ConstEntry that)
  {
    super (that);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
    _value = that._value;
  } // ctor

  /** This is a shallow copy constructor. */
  protected ConstEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  /** This is a shallow copy clone. */
  public Object clone ()
  {
    return new ConstEntry (this);
  } // clone

  /** Invoke the constant generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    constGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the constant generator.
      @return an object which implements the ConstGen interface.
      @see com.sun.tools.corba.ee.idl.ConstGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return constGen;
  } // generator

  public Expression value ()
  {
    return _value;
  } // value

  public void value (Expression newValue)
  {
    _value = newValue;
  } // value

  static com.sun.tools.corba.ee.idl.ConstGen constGen;
  private Expression _value = null;
} // class ConstEntry
