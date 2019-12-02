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

import com.sun.tools.corba.ee.idl.constExpr.Expression;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for unions.
 **/
public class UnionEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected UnionEntry ()
  {
    super ();
  } // ctor

  protected UnionEntry (UnionEntry that)
  {
    super (that);
    if (!name ().equals (""))
    {
      module (module () + name ());
      name ("");
    }
    _branches      = (Vector<UnionBranch>)that._branches.clone ();
    _defaultBranch = that._defaultBranch;
    _contained     = that._contained;
  } // ctor

  protected UnionEntry (SymtabEntry that, IDLID clone)
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
    return new UnionEntry (this);
  } // clone

  /** Invoke the union generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    unionGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the union generator.
      @return an object which implements the UnionGen interface.
      @see com.sun.tools.corba.ee.idl.UnionGen */
  @Override
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return unionGen;
  } // generator

  void addBranch(UnionBranch branch)
  {
    _branches.addElement (branch);
  } // addBranch

  /** This is a vector of UnionBranch's.
   * @return a {@link Vector} of {@link UnionBranch}
   */
  public Vector<UnionBranch> branches ()
  {
    return _branches;
  } // branches

  /** This TypedefEntry describes the type and name for the default branch.
    * Like the entries in the branches vector, only the type and name fields
    * are pertinent.
    * @param branch the {@link TypedefEntry} for the default
    */
  public void defaultBranch (TypedefEntry branch)
  {
    _defaultBranch = branch;
  } // defaultBranch

  /** This TypedefEntry describes the type and name for the default branch.
      Like the entries in the branches vector, only the type and name fields
      are pertinent.
    * @return {@link TypedefEntry} for the default
    */
  public TypedefEntry defaultBranch ()
  {
    return _defaultBranch;
  } // defaultBranch

  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } // addContained

  /** This is a vector of {@link SymtabEntry}s.  It itemizes any types which
      this union contains.  For example:

      <pre>
      union A
      switch (long)
      {
        case 0: long x;
        case 1:
          Struct B
          {
            long a;
            long b;
          } y;
      }
      </pre>
      Struct B is contained within union A.
  * @return a Vector of {@link SymtabEntry}s contained within
  */
  public Vector<SymtabEntry> contained ()
  {
    return _contained;
  } // contained

  boolean has (Expression label)
  {
    Enumeration<UnionBranch> eBranches = _branches.elements ();
    while (eBranches.hasMoreElements ())
    {
      Enumeration eLabels = (eBranches.nextElement()).labels.elements ();
      while (eLabels.hasMoreElements ())
      {
        Expression exp = (Expression)eLabels.nextElement ();
        if (exp.equals (label) || exp.value ().equals (label.value ()))
          return true;
      }
    }
    return false;
  } // has

  boolean has (com.sun.tools.corba.ee.idl.TypedefEntry typedef)
  {
    Enumeration<UnionBranch> e = _branches.elements ();
    while (e.hasMoreElements ())
    {
      UnionBranch branch = e.nextElement();
      if (!branch.typedef.equals (typedef) && branch.typedef.name ().equals (typedef.name ())) {
        return true;
      }
    }
    return false;
  } // has

  /** A vector of UnionBranch's. */
  private Vector<UnionBranch>       _branches      = new Vector<>();
  private TypedefEntry _defaultBranch = null;
  private Vector<SymtabEntry>       _contained     = new Vector ();

  static UnionGen unionGen;
} // class UnionEntry
