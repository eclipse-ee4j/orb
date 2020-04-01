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

package org.glassfish.corba.idl.toJavaPortable;

// NOTES:

import org.glassfish.corba.idl.AttributeGen;
import org.glassfish.corba.idl.EnumGen;
import org.glassfish.corba.idl.ExceptionGen;
import org.glassfish.corba.idl.ForwardGen;
import org.glassfish.corba.idl.ForwardValueGen;
import org.glassfish.corba.idl.IncludeGen;
import org.glassfish.corba.idl.InterfaceGen;
import org.glassfish.corba.idl.MethodGen;
import org.glassfish.corba.idl.NativeGen;
import org.glassfish.corba.idl.ParameterGen;
import org.glassfish.corba.idl.PragmaGen;

/**
 *
 **/
public class GenFactory implements org.glassfish.corba.idl.GenFactory
{

  public AttributeGen createAttributeGen ()
  {
    if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new AttributeGen24();
    else
      return new org.glassfish.corba.idl.toJavaPortable.AttributeGen();
  } // createAttributeGen

  public org.glassfish.corba.idl.ConstGen createConstGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.ConstGen();
  } // createConstGen

  public NativeGen createNativeGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.NativeGen();
  } // createNativeGen

  public EnumGen createEnumGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.EnumGen();
  } // createEnumGen

  public ExceptionGen createExceptionGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.ExceptionGen();
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
    return new org.glassfish.corba.idl.toJavaPortable.InterfaceGen();
  } // createInterfaceGen

  public org.glassfish.corba.idl.ValueGen createValueGen ()
  {
    if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new org.glassfish.corba.idl.toJavaPortable.ValueGen24();
    else
      return new org.glassfish.corba.idl.toJavaPortable.ValueGen();
  } // createValueGen

  public org.glassfish.corba.idl.ValueBoxGen createValueBoxGen ()
  {
    if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new org.glassfish.corba.idl.toJavaPortable.ValueBoxGen24();
    else
      return new org.glassfish.corba.idl.toJavaPortable.ValueBoxGen();
  } // createValueBoxGen

  public MethodGen createMethodGen ()
  {
    if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new org.glassfish.corba.idl.toJavaPortable.MethodGen24();
    else
      return new org.glassfish.corba.idl.toJavaPortable.MethodGen();
  } // createMethodGen

  public org.glassfish.corba.idl.ModuleGen createModuleGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.ModuleGen();
  } // createModuleGen

  public ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  public PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  public org.glassfish.corba.idl.PrimitiveGen createPrimitiveGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.PrimitiveGen();
  } // createPrimitiveGen

  public org.glassfish.corba.idl.SequenceGen createSequenceGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.SequenceGen();
  } // createSequenceGen

  public org.glassfish.corba.idl.StringGen createStringGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.StringGen();
  } // createSequenceGen

  public org.glassfish.corba.idl.StructGen createStructGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.StructGen();
  } // createStructGen

  public org.glassfish.corba.idl.TypedefGen createTypedefGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.TypedefGen();
  } // createTypedefGen

  public org.glassfish.corba.idl.UnionGen createUnionGen ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.UnionGen();
  } // createUnionGen
} // class GenFactory
