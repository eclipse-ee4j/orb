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
import java.util.Vector;

/**
 * This is the symbol table entry for typedefs.
 **/
public class TypedefEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected TypedefEntry ()
  {
    super ();
  } // ctor

  protected TypedefEntry (TypedefEntry that)
  {
    super (that);
    _arrayInfo = (Vector)that._arrayInfo.clone ();
  } // ctor

  protected TypedefEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  /** This method returns a vector of Expressions, each expression
    * represents a dimension in an array.  A zero-length vector indicates
    * no array information.
    * @return a {link Vector} of {@link Expression}s
    */
  public Vector arrayInfo ()
  {
    return _arrayInfo;
  } // arrayInfo

  public void addArrayInfo (Expression e)
  {
    _arrayInfo.addElement (e);
  } // addArrayInfo

  public Object clone ()
  {
    return new TypedefEntry (this);
  } // clone

  /** Invoke the typedef generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    typedefGen.generate (symbolTable, this, stream);
  } // generate

  public boolean isReferencable()
  {
    // A typedef is referencable if its component
    // type is.
    return type().isReferencable() ;
  }

  public void isReferencable( boolean value ) 
  {
    // NO-OP: this cannot be set for a typedef.
  }

  /** Access the typedef generator.
      @return an object which implements the TypedefGen interface.
      @see com.sun.tools.corba.ee.idl.TypedefGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return typedefGen;
  } // generator

  private Vector _arrayInfo = new Vector ();

  static com.sun.tools.corba.ee.idl.TypedefGen typedefGen;
} // class TypedefEntry
