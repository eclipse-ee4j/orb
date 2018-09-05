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

/**
 * This factory constructs the default symbol table entries, namely,
 * those declared within the package com.sun.tools.corba.ee.idl.
 **/
public class DefaultSymtabFactory implements SymtabFactory
{
  public com.sun.tools.corba.ee.idl.AttributeEntry attributeEntry ()
  {
    return new com.sun.tools.corba.ee.idl.AttributeEntry();
  } // attributeEntry

  public com.sun.tools.corba.ee.idl.AttributeEntry attributeEntry (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.AttributeEntry(container, id);
  } // attributeEntry

  public com.sun.tools.corba.ee.idl.ConstEntry constEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ConstEntry();
  } // constEntry

  public com.sun.tools.corba.ee.idl.ConstEntry constEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ConstEntry(container, id);
  } // constEntry

  public com.sun.tools.corba.ee.idl.NativeEntry nativeEntry ()
  {
    return new com.sun.tools.corba.ee.idl.NativeEntry();
  } // interfaceEntry

  public com.sun.tools.corba.ee.idl.NativeEntry nativeEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.NativeEntry(container, id);
  } // interfaceEntry

  public com.sun.tools.corba.ee.idl.EnumEntry enumEntry ()
  {
    return new com.sun.tools.corba.ee.idl.EnumEntry();
  } // enumEntry

  public com.sun.tools.corba.ee.idl.EnumEntry enumEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.EnumEntry(container, id);
  } // enumEntry

  public com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ExceptionEntry();
  } // exceptionEntry

  public com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ExceptionEntry(container, id);
  } // exceptionEntry

  public com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ForwardEntry();
  } // forwardEntry

  public com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ForwardEntry(container, id);
  } // forwardEntry

  public com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ForwardValueEntry();
  } // forwardValueEntry

  public com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ForwardValueEntry(container, id);
  } // forwardValueEntry

  public com.sun.tools.corba.ee.idl.IncludeEntry includeEntry ()
  {
    return new com.sun.tools.corba.ee.idl.IncludeEntry();
  } // includeEntry

  public com.sun.tools.corba.ee.idl.IncludeEntry includeEntry (com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    return new com.sun.tools.corba.ee.idl.IncludeEntry(container);
  } // includeEntry

  public com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry ()
  {
    return new com.sun.tools.corba.ee.idl.InterfaceEntry();
  } // interfaceEntry

  public com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.InterfaceEntry(container, id);
  } // interfaceEntry

  public com.sun.tools.corba.ee.idl.ValueEntry valueEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ValueEntry();
  } // valueEntry

  public com.sun.tools.corba.ee.idl.ValueEntry valueEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ValueEntry(container, id);
  } // valueEntry

  public com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ValueBoxEntry();
  } // valueBoxEntry

  public com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ValueBoxEntry(container, id);
  } // valueBoxEntry

  public com.sun.tools.corba.ee.idl.MethodEntry methodEntry ()
  {
    return new com.sun.tools.corba.ee.idl.MethodEntry();
  } // methodEntry

  public com.sun.tools.corba.ee.idl.MethodEntry methodEntry (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.MethodEntry(container, id);
  } // methodEntry

  public com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ModuleEntry();
  } // moduleEntry

  public com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ModuleEntry(container, id);
  } // moduleEntry

  public com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ParameterEntry();
  } // parameterEntry

  public com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry (com.sun.tools.corba.ee.idl.MethodEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ParameterEntry(container, id);
  } // parameterEntry

  public com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry ()
  {
    return new com.sun.tools.corba.ee.idl.PragmaEntry();
  } // pragmaEntry

  public com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry (com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    return new com.sun.tools.corba.ee.idl.PragmaEntry(container);
  } // pragmaEntry

  public com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry ()
  {
    return new com.sun.tools.corba.ee.idl.PrimitiveEntry();
  } // primitiveEntry

  /** "name" can be, but is not limited to, the primitive idl type names:
      'char', 'octet', 'short', 'long', etc.  The reason it is not limited
      to these is that, as an extender, you may wish to override these names.
      For instance, when generating Java code, octet translates to byte, so
      there is an entry in Compile.overrideNames:  &lt;"octet", "byte"&gt; and a
      PrimitiveEntry in the symbol table for "byte". */
  public com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry (String name)
  {
    return new com.sun.tools.corba.ee.idl.PrimitiveEntry(name);
  } // primitiveEntry

  public com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry ()
  {
    return new com.sun.tools.corba.ee.idl.SequenceEntry();
  } // sequenceEntry

  public com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.SequenceEntry(container, id);
  } // sequenceEntry

  public com.sun.tools.corba.ee.idl.StringEntry stringEntry ()
  {
    return new com.sun.tools.corba.ee.idl.StringEntry();
  } // stringEntry

  public com.sun.tools.corba.ee.idl.StructEntry structEntry ()
  {
    return new com.sun.tools.corba.ee.idl.StructEntry();
  } // structEntry

  public com.sun.tools.corba.ee.idl.StructEntry structEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.StructEntry(container, id);
  } // structEntry

  public com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry ()
  {
    return new com.sun.tools.corba.ee.idl.TypedefEntry();
  } // typedefEntry

  public com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.TypedefEntry(container, id);
  } // typedefEntry

  public com.sun.tools.corba.ee.idl.UnionEntry unionEntry ()
  {
    return new com.sun.tools.corba.ee.idl.UnionEntry();
  } // unionEntry

  public com.sun.tools.corba.ee.idl.UnionEntry unionEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.UnionEntry(container, id);
  } // unionEntry

} // interface DefaultSymtabFactory
