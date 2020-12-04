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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for interfaces.
 **/
public class InterfaceEntry extends com.sun.tools.corba.ee.idl.SymtabEntry implements InterfaceType
{

  protected InterfaceEntry ()
  {
    super ();
  } // ctor

  protected InterfaceEntry (InterfaceEntry that)
  {
    super (that);
    _derivedFromNames = (Vector<String>)that._derivedFromNames.clone ();
    _derivedFrom      = (Vector<SymtabEntry>)that._derivedFrom.clone ();
    _methods          = (Vector<MethodEntry>)that._methods.clone ();
    _allMethods       = (Vector<MethodEntry>)that._allMethods.clone ();
    forwardedDerivers = (Vector<InterfaceEntry>)that.forwardedDerivers.clone ();
    _contained        = (Vector<SymtabEntry>)that._contained.clone ();
    _interfaceType    = that._interfaceType;
  } // ctor

  protected InterfaceEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, com.sun.tools.corba.ee.idl.IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  public boolean isAbstract() 
  {
      return _interfaceType == ABSTRACT ;
  }

  public boolean isLocal() 
  {
      return _interfaceType == LOCAL ;
  }

  public boolean isLocalServant() 
  {
      return _interfaceType == LOCALSERVANT ;
  }

  public boolean isLocalSignature() 
  {
      return _interfaceType == LOCAL_SIGNATURE_ONLY ;
  }

  @Override
  public Object clone ()
  {
    return new InterfaceEntry (this);
  } // clone

  /** Invoke the interface generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  @Override
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    interfaceGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the interface generator.
      @return an object which implements the InterfaceGen interface.
      @see com.sun.tools.corba.ee.idl.InterfaceGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return interfaceGen;
  } // generator

  /** Add an InterfaceEntry to the list of interfaces which this interface
      is derivedFrom.  During parsing, the parameter to this method COULD
      be a ForwardEntry, but when parsing is complete, calling derivedFrom
      will return a vector which only contains InterfaceEntry's.
    * @param derivedFrom a {@link ForwardEntry} or {@link InterfaceEntry}
    */
  public void addDerivedFrom (com.sun.tools.corba.ee.idl.SymtabEntry derivedFrom)
  {
    _derivedFrom.addElement (derivedFrom);
  } // addDerivedFrom

  /** This method returns a vector of InterfaceEntry's.
   * @return a {@link Vector} of interfaces which this interface is derived from
   * @see #addDerivedFromName(java.lang.String) 
   */
  public Vector<SymtabEntry> derivedFrom ()
  {
    return _derivedFrom;
  } // derivedFrom

  /** Add to the list of derivedFrom names. */
  public void addDerivedFromName (String name)
  {
    _derivedFromNames.addElement (name);
  } // addDerivedFromName

  /** This method returns a vector of Strings, each of which is a fully
      qualified name of an interface. This vector corresponds to the
      derivedFrom vector.  The first element of this vector is the name
      of the first element of the derivedFrom vector, etc. 
    * @return {@link Vector} of {@link String}s
    */
  public Vector<String> derivedFromNames ()
  {
    return _derivedFromNames;
  } // derivedFromNames

  /** Add a method/attribute to the list of methods.
   * @param method method or attribute to add
   */
  public void addMethod (com.sun.tools.corba.ee.idl.MethodEntry method)
  {
    _methods.addElement (method);
  } // addMethod

  /** This is a vector of MethodEntry's.  These are the methods and
    * attributes contained within this Interface.
    * @return {@link Vector} of {@link MethodEntry}
    */
  public Vector<MethodEntry> methods ()
  {
    return _methods;
  } // methods

  /** Add a symbol table entry to this interface's contained vector.
   * @param entry new {@link SymtabEntry} to add
   */
  public void addContained (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    _contained.addElement (entry);
  } // addContained

  /** This is a vector of SymtabEntry's.  Valid entries in this vector are:
      AttributeEntry, ConstEntry, EnumEntry, ExceptionEntry, MethodEntry,
      StructEntry, NativeEntry, TypedefEntry, UnionEntry.  
      Note that the methods vector is a subset of this vector.
    * @return {@link Vector} of {@link SymtabEntry}
    * @see #methods()
    */
  public Vector<SymtabEntry> contained () {
    return _contained;
  }

  void methodsAddElement (com.sun.tools.corba.ee.idl.MethodEntry method, com.sun.tools.corba.ee.idl.Scanner scanner)
  {
    if (verifyMethod (method, scanner, false))
    {
      addMethod (method);
      _allMethods.addElement (method);

      // Add this method to the 'allMethods' list of any interfaces
      // which may have inherited this one when it was a forward
      // reference.
      addToForwardedAllMethods (method, scanner);
    }
  } // methodsAddElement

  void addToForwardedAllMethods (com.sun.tools.corba.ee.idl.MethodEntry method, com.sun.tools.corba.ee.idl.Scanner scanner)
  {
    Enumeration<InterfaceEntry> e = forwardedDerivers.elements ();
    while (e.hasMoreElements ()) {
      InterfaceEntry derived = e.nextElement();
      if (derived.verifyMethod (method, scanner, true)) {
        derived._allMethods.addElement (method);
      }
    }
  } // addToForwardedAllMethods

  // Make sure a method by this name doesn't exist in this class or
  // in this class's parents
    private boolean verifyMethod(MethodEntry method, Scanner scanner, boolean clash) {
        boolean unique = true;
        String lcName = method.name().toLowerCase();
        Enumeration<MethodEntry> e = _allMethods.elements();
        while (e.hasMoreElements()) {
            MethodEntry emethod = e.nextElement();

            // Make sure the method doesn't exist either in its
            // original name or in all lower case.  In IDL, identifiers
            // which differ only in case are collisions.
            String lceName = emethod.name().toLowerCase();
            if (method != emethod && lcName.equals(lceName)) {
                if (clash) {
                    ParseException.methodClash(scanner, fullName(), method.name());
                } else {
                    ParseException.alreadyDeclared(scanner, method.name());
                }
                unique = false;
                break;
            }
        }
        return unique;
    } // verifyMethod

  void derivedFromAddElement (SymtabEntry e, Scanner scanner)
  {
    addDerivedFrom (e);
    addDerivedFromName (e.fullName ());
    addParentType( e, scanner );
  } // derivedFromAddElement

  void addParentType (SymtabEntry e, Scanner scanner)
  {
    if (e instanceof ForwardEntry)
      addToDerivers ((ForwardEntry)e);
    else
    { // e instanceof InterfaceEntry
      InterfaceEntry derivedFrom = (InterfaceEntry)e;

        // Compare all of the parent's methods to the methods on this
        // interface, looking for name clashes:
        for (Enumeration<MethodEntry> enumeration = derivedFrom._allMethods.elements(); enumeration.hasMoreElements();) {
            MethodEntry method = enumeration.nextElement();
            if (verifyMethod(method, scanner, true)) {
                _allMethods.addElement(method);
            }

            // Add this method to the 'allMethods' list of any interfaces
            // which may have inherited this one when it was a forward
            // reference:
            addToForwardedAllMethods(method, scanner);
        }

      // If any of the parent's parents are forward entries, make
      // sure this interface gets added to their derivers list so
      // that when the forward entry is defined, the 'allMethods'
      // list of this interface can be updated.
      lookForForwardEntrys (scanner, derivedFrom);
    }
  }  // addParentType

    private void lookForForwardEntrys(Scanner scanner, InterfaceEntry entry) {
        Enumeration<SymtabEntry> parents = entry.derivedFrom().elements();
        while (parents.hasMoreElements()) {
            SymtabEntry parent = parents.nextElement();
            if (parent instanceof ForwardEntry) {
                addToDerivers((ForwardEntry) parent);
            } else if (parent == entry) {
                ParseException.selfInherit(scanner, entry.fullName());
            } else // it must be an InterfaceEntry
            {
                lookForForwardEntrys(scanner, (InterfaceEntry) parent);
            }
        }
    } // lookForForwardEntrys

  public boolean replaceForwardDecl (ForwardEntry oldEntry, InterfaceEntry newEntry)
  {
    int index = _derivedFrom.indexOf( oldEntry );
    if ( index >= 0 )
      _derivedFrom.setElementAt( newEntry, index );
    return (index >= 0);
  } // replaceForwardDecl

    private void addToDerivers(com.sun.tools.corba.ee.idl.ForwardEntry forward) {
        // Add this interface to the derivers list on the forward entry
        // so that when the forward entry is defined, the 'allMethods'
        // list of this interface can be updated.
        forward.derivers.addElement(this);
        Enumeration<InterfaceEntry> e = forwardedDerivers.elements();
        while (e.hasMoreElements()) {
            forward.derivers.addElement(e.nextElement());
        }
    } // addToDerivers

  /** This method returns a vector of the elements in the state block.
      If it is null, this is not a stateful interface.  If it is non-null,
      but of zero length, then it is still stateful; it has no state
      entries itself, but it has an ancestor which does.
    * @return Vector of {@link InterfaceState}
    */
  public Vector<InterfaceState> state ()
  {
    return _state;
  } // state

  public void initState ()
  {
    _state = new Vector<>();
  } // initState

    public void addStateElement(InterfaceState state, Scanner scanner) {
        if (_state == null) {
            _state = new Vector<>();
        }
        String name = state.entry.name();
        for (Enumeration<InterfaceState> e = _state.elements(); e.hasMoreElements();) {
            if (name.equals((e.nextElement()).entry.name())) {
                ParseException.duplicateState(scanner, name);
            }
        }
        _state.addElement(state);
    } // state

  @Override
  public int getInterfaceType ()
  {
    return _interfaceType;
  }

  @Override
  public void setInterfaceType (int type)
  {
    _interfaceType = type;
  }

  /** Get the allMethods vector.
   * @return Vector of all methods in the interface
   * @see MethodEntry
   */
  public Vector<MethodEntry> allMethods ()
  {
    return _allMethods;
  }

  private Vector<String>  _derivedFromNames = new Vector<>();
  private Vector<SymtabEntry>  _derivedFrom      = new Vector<>();
  private Vector<MethodEntry>  _methods          = new Vector<>();
          Vector<MethodEntry>  _allMethods       = new Vector();
          Vector<InterfaceEntry>  forwardedDerivers = new Vector<>();
  private Vector<SymtabEntry>  _contained        = new Vector<>();
  private Vector<InterfaceState>  _state            = null;
  private int _interfaceType         = NORMAL;

  static InterfaceGen interfaceGen;
} // class InterfaceEntry
