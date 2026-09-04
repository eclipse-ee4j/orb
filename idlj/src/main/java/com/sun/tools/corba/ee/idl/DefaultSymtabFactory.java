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

/**
 * This factory constructs the default symbol table entries, namely,
 * those declared within the package com.sun.tools.corba.ee.idl.
 **/
public class DefaultSymtabFactory implements SymtabFactory
{
  @Override
  public com.sun.tools.corba.ee.idl.AttributeEntry attributeEntry ()
  {
    return new com.sun.tools.corba.ee.idl.AttributeEntry();
  } // attributeEntry

  @Override
  public com.sun.tools.corba.ee.idl.AttributeEntry attributeEntry (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.AttributeEntry(container, id);
  } // attributeEntry

  @Override
  public com.sun.tools.corba.ee.idl.ConstEntry constEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ConstEntry();
  } // constEntry

  @Override
  public com.sun.tools.corba.ee.idl.ConstEntry constEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ConstEntry(container, id);
  } // constEntry

  @Override
  public com.sun.tools.corba.ee.idl.NativeEntry nativeEntry ()
  {
    return new com.sun.tools.corba.ee.idl.NativeEntry();
  } // interfaceEntry

  @Override
  public com.sun.tools.corba.ee.idl.NativeEntry nativeEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.NativeEntry(container, id);
  } // interfaceEntry

  @Override
  public com.sun.tools.corba.ee.idl.EnumEntry enumEntry ()
  {
    return new com.sun.tools.corba.ee.idl.EnumEntry();
  } // enumEntry

  @Override
  public com.sun.tools.corba.ee.idl.EnumEntry enumEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.EnumEntry(container, id);
  } // enumEntry

  @Override
  public com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ExceptionEntry();
  } // exceptionEntry

  @Override
  public com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ExceptionEntry(container, id);
  } // exceptionEntry

  @Override
  public com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ForwardEntry();
  } // forwardEntry

  @Override
  public com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ForwardEntry(container, id);
  } // forwardEntry

  @Override
  public com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ForwardValueEntry();
  } // forwardValueEntry

  @Override
  public com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ForwardValueEntry(container, id);
  } // forwardValueEntry

  @Override
  public com.sun.tools.corba.ee.idl.IncludeEntry includeEntry ()
  {
    return new com.sun.tools.corba.ee.idl.IncludeEntry();
  } // includeEntry

  @Override
  public com.sun.tools.corba.ee.idl.IncludeEntry includeEntry (com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    return new com.sun.tools.corba.ee.idl.IncludeEntry(container);
  } // includeEntry

  @Override
  public com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry ()
  {
    return new com.sun.tools.corba.ee.idl.InterfaceEntry();
  } // interfaceEntry

  @Override
  public com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.InterfaceEntry(container, id);
  } // interfaceEntry

  @Override
  public com.sun.tools.corba.ee.idl.ValueEntry valueEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ValueEntry();
  } // valueEntry

  @Override
  public com.sun.tools.corba.ee.idl.ValueEntry valueEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ValueEntry(container, id);
  } // valueEntry

  @Override
  public com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ValueBoxEntry();
  } // valueBoxEntry

  @Override
  public com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ValueBoxEntry(container, id);
  } // valueBoxEntry

  @Override
  public com.sun.tools.corba.ee.idl.MethodEntry methodEntry ()
  {
    return new com.sun.tools.corba.ee.idl.MethodEntry();
  } // methodEntry

  @Override
  public com.sun.tools.corba.ee.idl.MethodEntry methodEntry (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.MethodEntry(container, id);
  } // methodEntry

  @Override
  public com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ModuleEntry();
  } // moduleEntry

  @Override
  public com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ModuleEntry(container, id);
  } // moduleEntry

  @Override
  public com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry ()
  {
    return new com.sun.tools.corba.ee.idl.ParameterEntry();
  } // parameterEntry

  @Override
  public com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry (com.sun.tools.corba.ee.idl.MethodEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.ParameterEntry(container, id);
  } // parameterEntry

  @Override
  public com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry ()
  {
    return new com.sun.tools.corba.ee.idl.PragmaEntry();
  } // pragmaEntry

  @Override
  public com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry (com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    return new com.sun.tools.corba.ee.idl.PragmaEntry(container);
  } // pragmaEntry

  @Override
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
  @Override
  public com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry (String name)
  {
    return new com.sun.tools.corba.ee.idl.PrimitiveEntry(name);
  } // primitiveEntry

  @Override
  public com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry ()
  {
    return new com.sun.tools.corba.ee.idl.SequenceEntry();
  } // sequenceEntry

  @Override
  public com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.SequenceEntry(container, id);
  } // sequenceEntry

  @Override
  public com.sun.tools.corba.ee.idl.StringEntry stringEntry ()
  {
    return new com.sun.tools.corba.ee.idl.StringEntry();
  } // stringEntry

  @Override
  public com.sun.tools.corba.ee.idl.StructEntry structEntry ()
  {
    return new com.sun.tools.corba.ee.idl.StructEntry();
  } // structEntry

  @Override
  public com.sun.tools.corba.ee.idl.StructEntry structEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.StructEntry(container, id);
  } // structEntry

  @Override
  public com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry ()
  {
    return new com.sun.tools.corba.ee.idl.TypedefEntry();
  } // typedefEntry

  @Override
  public com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.TypedefEntry(container, id);
  } // typedefEntry

  @Override
  public com.sun.tools.corba.ee.idl.UnionEntry unionEntry ()
  {
    return new com.sun.tools.corba.ee.idl.UnionEntry();
  } // unionEntry

  @Override
  public com.sun.tools.corba.ee.idl.UnionEntry unionEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id)
  {
    return new com.sun.tools.corba.ee.idl.UnionEntry(container, id);
  } // unionEntry

} // interface DefaultSymtabFactory
