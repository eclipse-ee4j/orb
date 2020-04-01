/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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
import java.util.Vector;

/**
 * This is the symbol table entry for modules.
 **/
public class ModuleEntry extends SymtabEntry
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

  protected ModuleEntry (SymtabEntry that, IDLID clone)
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
      @see SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    moduleGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the module generator.
      @return an object which implements the ModuleGen interface.
      @see ModuleGen */
  @Override
  public Generator generator ()
  {
    return moduleGen;
  } // generator

  /** @param entry Valid entries in this vector are:  TypedefEntry, ExceptionEntry,
      StructEntry, UnionEntry, EnumEntry, ConstEntry, InterfaceEntry,
      ModuleEntry. */
  public void addContained (SymtabEntry entry)
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

  static ModuleGen moduleGen;
} // class ModuleEntry
