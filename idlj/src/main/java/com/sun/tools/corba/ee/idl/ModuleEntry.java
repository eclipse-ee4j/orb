/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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
 * This is the symbol table entry for modules.
 **/
public class ModuleEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected ModuleEntry ()
  {
    super ();
  }  // ctor

  protected ModuleEntry (ModuleEntry that)
  {
    super (that);
    _contained = (Vector<SymtabEntry>)that._contained.clone ();
  } // ctor

  protected ModuleEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);

    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  public Object clone ()
  {
    return new ModuleEntry (this);
  } // clone

  /** Invoke the module generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    moduleGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the module generator.
      @return an object which implements the ModuleGen interface.
      @see com.sun.tools.corba.ee.idl.ModuleGen */
  @Override
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return moduleGen;
  } // generator

  /** @param entry Valid entries in this vector are:  TypedefEntry, ExceptionEntry,
      StructEntry, UnionEntry, EnumEntry, ConstEntry, InterfaceEntry,
      ModuleEntry. */
  public void addContained (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    _contained.addElement (entry);
  } // addContained

  /** This is a vector of SymtabEntry's.  Valid entries in this vector are:
      TypedefEntry, ExceptionEntry, StructEntry, UnionEntry, EnumEntry,
      ConstEntry, InterfaceEntry, ModuleEntry.
    * @return a {@link Vector} of {@link SymtabEntry}
    */
  public Vector<SymtabEntry> contained ()
  {
    return _contained;
  } // contained

  private Vector<SymtabEntry> _contained = new Vector<>();

  static com.sun.tools.corba.ee.idl.ModuleGen moduleGen;
} // class ModuleEntry
