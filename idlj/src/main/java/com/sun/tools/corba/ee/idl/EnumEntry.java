/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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
import java.util.Vector;

/**
 * This is the symbol table entry for enumerated types.
 **/
public class EnumEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected EnumEntry ()
  {
    super ();
  } // ctor

  protected EnumEntry (EnumEntry that)
  {
    super (that);
    _elements = (Vector<String>)that._elements.clone ();
  } // ctor

  protected EnumEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);

    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  @Override
  public Object clone ()
  {
    return new EnumEntry (this);
  } // clone

  /** Invoke the enumerator generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    enumGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the enumerator generator.
      @return an object which implements the EnumGen interface.
      @see com.sun.tools.corba.ee.idl.EnumGen */
  @Override
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return enumGen;
  } // generator

  /** Add an element to the list of elements.
   * @param element new element to add
   */
  public void addElement (String element)
  {
    _elements.addElement (element);
  } // addElement

  /** Each element of the vector is a String. 
   * @return a {@link Vector} or all the elements in the enum
   */
  public Vector<String> elements() {
    return _elements;
  } // elements

  static com.sun.tools.corba.ee.idl.EnumGen enumGen;
  private Vector<String>  _elements = new Vector<>();
} // class EnumEntry
