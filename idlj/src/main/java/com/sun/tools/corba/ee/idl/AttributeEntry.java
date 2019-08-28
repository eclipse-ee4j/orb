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
 * This is the symbol table entry for attributes.  An attribute is simply
 * two methods with no exceptions or contexts:  a get method and, if not
 * readOnly, a set method.
 **/
public class AttributeEntry extends MethodEntry
{
  protected AttributeEntry ()
  {
    super ();
  } // ctor

  protected AttributeEntry (AttributeEntry that)
  {
    super (that);
    _readOnly  = that._readOnly;
  } // ctor

  protected AttributeEntry (com.sun.tools.corba.ee.idl.InterfaceEntry that, com.sun.tools.corba.ee.idl.IDLID clone)
  {
    super (that, clone);
  } // ctor

  @Override
  public Object clone ()
  {
    return new AttributeEntry (this);
  } // clone

  /** Invoke the attribute generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    attributeGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the attribute generator.
      @return an object which implements the AttributeGen interface.
      @see com.sun.tools.corba.ee.idl.AttributeGen */
  @Override
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return attributeGen;
  } // generator

  /** 
   * If true, only a get method will be generated. 
   * @return if the attribute is readonly
   */
  public boolean readOnly ()
  {
    return _readOnly;
  } // readOnly

  /** 
   * Sets the readOnly parameter
   * @param readOnly If true, only a get method will be generated.
   */
  public void readOnly (boolean readOnly)
  {
    _readOnly = readOnly;
  } // readOnly

  static com.sun.tools.corba.ee.idl.AttributeGen attributeGen;

  public boolean      _readOnly = false;
} // class AttributeEntry
