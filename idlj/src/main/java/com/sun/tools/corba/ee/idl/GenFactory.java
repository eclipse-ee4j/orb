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
 * To extend this compiler framework to generate something other than
 * the default, this factory interface must be implemented and the name
 * of it must be set in the main method (see idl.Compile).
 * <p>
 * The implementation of each method should be quite simple.  Take
 * createAttributeGen, for instance.  If the interface AttributeGen is
 * implemented by a class called MyAttributeGen, then createAttributeGen
 * will be the following:
 * <pre>
 * public AttributeGen createAttributeGen ()
 * {
 *   return new MyAttributeGen ();
 * }
 * </pre>
 * <p>
 * If it is desired that a generator do nothing, it is not necessary to
 * implement one which does nothing; you may simply write that particular
 * create method so that it returns null.
 * <p>
 * Note that this class MUST have a public default constructor (one which
 * takes no parameters).
 **/
public interface GenFactory
{
  public AttributeGen createAttributeGen ();
  public com.sun.tools.corba.ee.idl.ConstGen createConstGen ();
  public com.sun.tools.corba.ee.idl.EnumGen createEnumGen ();
  public com.sun.tools.corba.ee.idl.ExceptionGen createExceptionGen ();
  public com.sun.tools.corba.ee.idl.ForwardGen createForwardGen ();
  public com.sun.tools.corba.ee.idl.ForwardValueGen createForwardValueGen ();
  public com.sun.tools.corba.ee.idl.IncludeGen createIncludeGen ();
  public com.sun.tools.corba.ee.idl.InterfaceGen createInterfaceGen ();
  public com.sun.tools.corba.ee.idl.ValueGen createValueGen ();
  public com.sun.tools.corba.ee.idl.ValueBoxGen createValueBoxGen ();
  public com.sun.tools.corba.ee.idl.MethodGen createMethodGen ();
  public com.sun.tools.corba.ee.idl.ModuleGen createModuleGen ();
  public com.sun.tools.corba.ee.idl.NativeGen createNativeGen ();
  public com.sun.tools.corba.ee.idl.ParameterGen createParameterGen ();
  public com.sun.tools.corba.ee.idl.PragmaGen createPragmaGen ();
  public com.sun.tools.corba.ee.idl.PrimitiveGen createPrimitiveGen ();
  public com.sun.tools.corba.ee.idl.SequenceGen createSequenceGen ();
  public com.sun.tools.corba.ee.idl.StringGen createStringGen ();
  public com.sun.tools.corba.ee.idl.StructGen createStructGen ();
  public com.sun.tools.corba.ee.idl.TypedefGen createTypedefGen ();
  public com.sun.tools.corba.ee.idl.UnionGen createUnionGen ();
} // interface GenFactory
