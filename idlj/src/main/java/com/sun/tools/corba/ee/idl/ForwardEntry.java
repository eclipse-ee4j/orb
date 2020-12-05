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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for forward declarations of interfaces.
 **/
public class ForwardEntry extends com.sun.tools.corba.ee.idl.SymtabEntry implements com.sun.tools.corba.ee.idl.InterfaceType
{
  protected ForwardEntry ()
  {
    super ();
  } // ctor

  protected ForwardEntry (ForwardEntry that)
  {
    super (that);
  } // ctor

  protected ForwardEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
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
    return new ForwardEntry (this);
  } // clone

  /** Invoke the forward declaration generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    forwardGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the interface generator.
      @return an object which implements the InterfaceGen interface.
      @see com.sun.tools.corba.ee.idl.InterfaceGen */
  @Override
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return forwardGen;
  } // generator

  static boolean replaceForwardDecl (InterfaceEntry interfaceEntry)
  {
    boolean result = true;
    try
    {
      ForwardEntry forwardEntry =
          (ForwardEntry) com.sun.tools.corba.ee.idl.Parser.symbolTable.get (interfaceEntry.fullName ());
      if ( forwardEntry != null )
      {
        result = (interfaceEntry.getInterfaceType () == 
            forwardEntry.getInterfaceType ());
        forwardEntry.type (interfaceEntry);

        // If this interface has been forward declared, there are probably
        // other interfaces which derive from a ForwardEntry.  Replace
        // those ForwardEntry's with this InterfaceEntry:
        interfaceEntry.forwardedDerivers = forwardEntry.derivers;
          for (Enumeration<InterfaceEntry> derivers = forwardEntry.derivers.elements(); derivers.hasMoreElements();) {
              (derivers.nextElement()).replaceForwardDecl(forwardEntry, interfaceEntry);
          }

        // Replace the entry's whose types are forward declarations:
          for (Enumeration<SymtabEntry> types = forwardEntry.types.elements(); types.hasMoreElements();) {
              (types.nextElement()).type(interfaceEntry);
          }
      }
    }
    catch (Exception exception)
    {}
    return result;
  } // replaceForwardDecl

  ///////////////
  // Implement interface InterfaceType

  @Override
  public int getInterfaceType ()
  {
    return _type;
  }

  @Override
  public void setInterfaceType (int type)
  {
    _type = type;
  }

  static com.sun.tools.corba.ee.idl.ForwardGen forwardGen;
  Vector<InterfaceEntry>            derivers   = new Vector<>(); // Vector of InterfaceEntry's.
  Vector<SymtabEntry>            types      = new Vector<>(); // Vector of the entry's whose type is a forward declaration.
  private int   _type  = com.sun.tools.corba.ee.idl.InterfaceType.NORMAL; // interface type
} // class ForwardEntry
