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

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:
import com.sun.tools.corba.ee.idl.ForwardGen;
import com.sun.tools.corba.ee.idl.IncludeGen;
import com.sun.tools.corba.ee.idl.ParameterGen;
import com.sun.tools.corba.ee.idl.PragmaGen;

/**
 *
 **/
public class GenFactory implements com.sun.tools.corba.ee.idl.GenFactory
{

  @Override
  public com.sun.tools.corba.ee.idl.AttributeGen createAttributeGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new AttributeGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.AttributeGen();
  } // createAttributeGen

  @Override
  public com.sun.tools.corba.ee.idl.ConstGen createConstGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ConstGen();
  } // createConstGen

  @Override
  public com.sun.tools.corba.ee.idl.NativeGen createNativeGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.NativeGen();
  } // createNativeGen

  @Override
  public com.sun.tools.corba.ee.idl.EnumGen createEnumGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.EnumGen();
  } // createEnumGen

  @Override
  public com.sun.tools.corba.ee.idl.ExceptionGen createExceptionGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ExceptionGen();
  } // createExceptionGen

  @Override
  public ForwardGen createForwardGen ()
  {
    return null;
  } // createForwardGen

  @Override
  public com.sun.tools.corba.ee.idl.ForwardValueGen createForwardValueGen ()
  {
    return null;
  } // createForwardValueGen

  @Override
  public IncludeGen createIncludeGen ()
  {
    return null;
  } // createIncludeGen

  @Override
  public com.sun.tools.corba.ee.idl.InterfaceGen createInterfaceGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.InterfaceGen();
  } // createInterfaceGen

  @Override
  public com.sun.tools.corba.ee.idl.ValueGen createValueGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueGen();
  } // createValueGen

  @Override
  public com.sun.tools.corba.ee.idl.ValueBoxGen createValueBoxGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueBoxGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueBoxGen();
  } // createValueBoxGen

  @Override
  public com.sun.tools.corba.ee.idl.MethodGen createMethodGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen();
  } // createMethodGen

  @Override
  public com.sun.tools.corba.ee.idl.ModuleGen createModuleGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ModuleGen();
  } // createModuleGen

  @Override
  public ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  @Override
  public PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  @Override
  public com.sun.tools.corba.ee.idl.PrimitiveGen createPrimitiveGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.PrimitiveGen();
  } // createPrimitiveGen

  @Override
  public com.sun.tools.corba.ee.idl.SequenceGen createSequenceGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.SequenceGen();
  } // createSequenceGen

  @Override
  public com.sun.tools.corba.ee.idl.StringGen createStringGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.StringGen();
  } // createSequenceGen

  @Override
  public com.sun.tools.corba.ee.idl.StructGen createStructGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.StructGen();
  } // createStructGen

  @Override
  public com.sun.tools.corba.ee.idl.TypedefGen createTypedefGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.TypedefGen();
  } // createTypedefGen

  @Override
  public com.sun.tools.corba.ee.idl.UnionGen createUnionGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.UnionGen();
  } // createUnionGen
} // class GenFactory
