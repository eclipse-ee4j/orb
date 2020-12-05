/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package com.sun.tools.corba.ee.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for structs.
 **/
public class StructEntry extends SymtabEntry
{
  protected StructEntry ()
  {
    super ();
  } // ctor

  protected StructEntry (StructEntry that)
  {
    super (that);
    if (!name ().equals (""))
    {
      module (module () + name ());
      name ("");
    }
    _members   = (Vector<TypedefEntry>)that._members.clone ();
    _contained = (Vector<SymtabEntry>)that._contained.clone ();
  } // ctor

    protected StructEntry(SymtabEntry that, IDLID clone) {
        super(that, clone);
        if (module().equals("")) {
            module(name());
        } else if (!name().equals("")) {
            module(module() + "/" + name());
        }
    } // ctor

  @Override
  public Object clone ()
  {
    return new StructEntry (this);
  } // clone

  /** Invoke the struct generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    structGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the struct generator.
      @return an object which implements the StructGen interface.
      @see com.sun.tools.corba.ee.idl.StructGen */
  public Generator generator ()
  {
    return structGen;
  } // generator

  /** Add a member to the member list.
   * @param member member to add to list
   */
  public void addMember(TypedefEntry member) {
    _members.addElement (member);
  } // addMember

  /** This is a vector of TypedefEntry's.  In this context, only the name,
    * type, and arrayInfo fields hold any meaning.
    * @return a {@link Vector} of the members of the stuct
    */
  public Vector<TypedefEntry> members ()
  {
    return _members;
  } // members

  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } // addContained

  /** This is a vector of SymtabEntry's.  It itemizes any types which
      this struct contains.  It is different than the member list.
      For example:
      <pre>
      struct A
      {
        long x;
        Struct B
        {
          long a;
          long b;
        } y;
      }
      </pre>
      Struct B is contained within struct A.
      The members vector will contain entries for x and y. 
    * @return a {@link Vector} of the {@link SymtabEntry} in the Struct
    */
  public Vector<SymtabEntry> contained() {
    return _contained;
  } // contained

  private Vector<TypedefEntry> _members   = new Vector<>();
  private Vector<SymtabEntry> _contained = new Vector<>();

  static com.sun.tools.corba.ee.idl.StructGen structGen;
} // class StructEntry
