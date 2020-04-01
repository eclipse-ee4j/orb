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

import org.glassfish.corba.idl.constExpr.Expression;

import java.io.PrintWriter;
import java.util.Hashtable;

public class StringEntry extends SymtabEntry
{
  protected StringEntry ()
  {
    super ();
    String override = (String) Parser.overrideNames.get ("string");
    if (override == null)
      name ("string");
    else
      name (override);
    repositoryID (Util.emptyID);
  } // ctor

  protected StringEntry (StringEntry that)
  {
    super (that);
    _maxSize = that._maxSize;
  } // ctor

  protected StringEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    module ("");

    String override = (String) Parser.overrideNames.get ("string");
    if (override == null)
      name ("string");
    else
      name (override);
    repositoryID (Util.emptyID);
  } // ctor

  public Object clone ()
  {
    return new StringEntry (this);
  } // clone

  /** Invoke the string type generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    stringGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the primitive type generator.
      @return an object which implements the PrimitiveGen interface.
      @see PrimitiveGen */
  public Generator generator ()
  {
    return stringGen;
  } // generator

  /** The constant expression defining the maximum size of the string.
    * If it is null, then the string is unbounded.
    * @param expr the maximum size of the string to set
    */
  public void maxSize (Expression expr)
  {
    _maxSize = expr;
  } // maxSize

  /** The constant expression defining the maximum size of the string.
    * If it is null, then the string is unbounded.
    * @return the maximum size of the String
    */
  public Expression maxSize ()
  {
    return _maxSize;
  } // maxSize

  static StringGen stringGen;

  private Expression _maxSize = null;
} // class StringEntry
