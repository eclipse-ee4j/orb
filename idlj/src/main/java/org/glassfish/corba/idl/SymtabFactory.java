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

/**
 * Each entry must have three ways in which it can be instantiated:
 * <ul>
 * <li>with no parameters;
 * <li>cloned from a copy of itself;
 * <li>the normal-use instantiation (usually with 2 parameters:  the container and the id of the container).
 * </ul>
 **/
public interface SymtabFactory
{
  AttributeEntry attributeEntry ();
  AttributeEntry attributeEntry (InterfaceEntry container, IDLID id);

  ConstEntry constEntry ();
  ConstEntry constEntry (SymtabEntry container, IDLID id);

  NativeEntry nativeEntry ();
  NativeEntry nativeEntry (SymtabEntry container, IDLID id);

  EnumEntry enumEntry ();
  EnumEntry enumEntry (SymtabEntry container, IDLID id);

  ExceptionEntry exceptionEntry ();
  ExceptionEntry exceptionEntry (SymtabEntry container, IDLID id);

  ForwardEntry forwardEntry ();
  ForwardEntry forwardEntry (ModuleEntry container, IDLID id);

  ForwardValueEntry forwardValueEntry ();
  ForwardValueEntry forwardValueEntry (ModuleEntry container, IDLID id);

  IncludeEntry includeEntry ();
  IncludeEntry includeEntry (SymtabEntry container);

  InterfaceEntry interfaceEntry ();
  InterfaceEntry interfaceEntry (ModuleEntry container, IDLID id);

  ValueEntry valueEntry ();
  ValueEntry valueEntry (ModuleEntry container, IDLID id);

  ValueBoxEntry valueBoxEntry ();
  ValueBoxEntry valueBoxEntry (ModuleEntry container, IDLID id);

  MethodEntry methodEntry ();
  MethodEntry methodEntry (InterfaceEntry container, IDLID id);

  ModuleEntry moduleEntry ();
  ModuleEntry moduleEntry (ModuleEntry container, IDLID id);

  ParameterEntry parameterEntry ();
  ParameterEntry parameterEntry (MethodEntry container, IDLID id);

  PragmaEntry pragmaEntry ();
  PragmaEntry pragmaEntry (SymtabEntry container);

  PrimitiveEntry primitiveEntry ();
  /** name can be, but is not limited to, the primitive idl type names:
      char, octet, short, long, etc.  The reason it is not limited to
      these is that, as an extender, you may wish to override these names.
      For instance, when generating Java code, octet translates to byte,
      so there is an entry in Compile.overrideNames:  &lt;"octet", "byte"&gt;
      and a PrimitiveEntry in the symbol table for "byte". */
  PrimitiveEntry primitiveEntry (String name);

  SequenceEntry sequenceEntry ();
  SequenceEntry sequenceEntry (SymtabEntry container, IDLID id);

  StringEntry stringEntry ();

  StructEntry structEntry ();
  StructEntry structEntry (SymtabEntry container, IDLID id);

  TypedefEntry typedefEntry ();
  TypedefEntry typedefEntry (SymtabEntry container, IDLID id);

  UnionEntry unionEntry ();
  UnionEntry unionEntry (SymtabEntry container, IDLID id);
} // interface SymtabFactory
