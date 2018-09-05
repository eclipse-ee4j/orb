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

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:

import com.sun.tools.corba.ee.idl.*;

/**
 *
 **/
public class GenFactory implements com.sun.tools.corba.ee.idl.GenFactory
{

  public com.sun.tools.corba.ee.idl.AttributeGen createAttributeGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new AttributeGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.AttributeGen();
  } // createAttributeGen

  public com.sun.tools.corba.ee.idl.ConstGen createConstGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ConstGen();
  } // createConstGen

  public com.sun.tools.corba.ee.idl.NativeGen createNativeGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.NativeGen();
  } // createNativeGen

  public com.sun.tools.corba.ee.idl.EnumGen createEnumGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.EnumGen();
  } // createEnumGen

  public com.sun.tools.corba.ee.idl.ExceptionGen createExceptionGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ExceptionGen();
  } // createExceptionGen

  public ForwardGen createForwardGen ()
  {
    return null;
  } // createForwardGen

  public com.sun.tools.corba.ee.idl.ForwardValueGen createForwardValueGen ()
  {
    return null;
  } // createForwardValueGen

  public IncludeGen createIncludeGen ()
  {
    return null;
  } // createIncludeGen

  public com.sun.tools.corba.ee.idl.InterfaceGen createInterfaceGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.InterfaceGen();
  } // createInterfaceGen

  public com.sun.tools.corba.ee.idl.ValueGen createValueGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueGen();
  } // createValueGen

  public com.sun.tools.corba.ee.idl.ValueBoxGen createValueBoxGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueBoxGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueBoxGen();
  } // createValueBoxGen

  public com.sun.tools.corba.ee.idl.MethodGen createMethodGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen();
  } // createMethodGen

  public com.sun.tools.corba.ee.idl.ModuleGen createModuleGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ModuleGen();
  } // createModuleGen

  public ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  public PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  public com.sun.tools.corba.ee.idl.PrimitiveGen createPrimitiveGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.PrimitiveGen();
  } // createPrimitiveGen

  public com.sun.tools.corba.ee.idl.SequenceGen createSequenceGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.SequenceGen();
  } // createSequenceGen

  public com.sun.tools.corba.ee.idl.StringGen createStringGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.StringGen();
  } // createSequenceGen

  public com.sun.tools.corba.ee.idl.StructGen createStructGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.StructGen();
  } // createStructGen

  public com.sun.tools.corba.ee.idl.TypedefGen createTypedefGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.TypedefGen();
  } // createTypedefGen

  public com.sun.tools.corba.ee.idl.UnionGen createUnionGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.UnionGen();
  } // createUnionGen
} // class GenFactory
