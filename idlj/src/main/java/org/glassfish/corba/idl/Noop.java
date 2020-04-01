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

import java.io.PrintWriter;
import java.util.Hashtable;

public class Noop implements
      AttributeGen, ConstGen, EnumGen, ExceptionGen, ForwardGen,
      ForwardValueGen, IncludeGen, InterfaceGen, ValueGen, ValueBoxGen,
      MethodGen, ModuleGen, NativeGen, ParameterGen, PragmaGen,
      PrimitiveGen, SequenceGen, StringGen, StructGen, TypedefGen,
      UnionGen, GenFactory
{
  public void generate (Hashtable symbolTable, AttributeEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ConstEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, EnumEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ExceptionEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ForwardEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ForwardValueEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, IncludeEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, InterfaceEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ValueEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ValueBoxEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, MethodEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ModuleEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, ParameterEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, PragmaEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, PrimitiveEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, SequenceEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, StringEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, StructEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, TypedefEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, UnionEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, NativeEntry entry, PrintWriter stream)
  {
  } // generate

  // For GenFactory
  public AttributeGen createAttributeGen ()
  {
    return null;
  } // createAttributeGen

  public ConstGen createConstGen ()
  {
    return null;
  } // createConstGen

  public EnumGen createEnumGen ()
  {
    return null;
  } // createEnumGen

  public ExceptionGen createExceptionGen ()
  {
    return null;
  } // createExceptionGen

  public ForwardGen createForwardGen ()
  {
    return null;
  } // createForwardGen

  public ForwardValueGen createForwardValueGen ()
  {
    return null;
  } // createForwardValueGen

  public IncludeGen createIncludeGen ()
  {
    return null;
  } // createIncludeGen

  public InterfaceGen createInterfaceGen ()
  {
    return null;
  } // createInterfaceGen

  public ValueGen createValueGen ()
  {
    return null;
  } // createValueGen

  public ValueBoxGen createValueBoxGen ()
  {
    return null;
  } // createValueBoxGen

  public MethodGen createMethodGen ()
  {
    return null;
  } // createMethodGen

  public ModuleGen createModuleGen ()
  {
    return null;
  } // createModuleGen

  public NativeGen createNativeGen ()
  {
    return null;
  } // createNativeGen

  public ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  public PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  public PrimitiveGen createPrimitiveGen ()
  {
    return null;
  } // createPrimitiveGen

  public SequenceGen createSequenceGen ()
  {
    return null;
  } // createSequenceGen

  public StringGen createStringGen ()
  {
    return null;
  } // createStringGen

  public StructGen createStructGen ()
  {
    return null;
  } // createStructGen

  public TypedefGen createTypedefGen ()
  {
    return null;
  } // createTypedefGen

  public UnionGen createUnionGen ()
  {
    return null;
  } // createUnionGen
} // class Noop
