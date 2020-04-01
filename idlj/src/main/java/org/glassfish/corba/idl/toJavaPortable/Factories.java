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
// -D62023<klr> Add corbaLevel=2.4

import org.glassfish.corba.idl.Arguments;

/**
 *
 **/
public class Factories extends org.glassfish.corba.idl.Factories
{
  public org.glassfish.corba.idl.GenFactory genFactory ()
  {
    return new GenFactory();
  } // genFactory

  public Arguments arguments ()
  {
    return new org.glassfish.corba.idl.toJavaPortable.Arguments();
  } // arguments

  public String[] languageKeywords ()
  {
  // These are Java keywords that are not also IDL keywords.
    return keywords;
  } // languageKeywords

  static String[] keywords =
    {"abstract",   "break",     "byte",
     "catch",      "class",     "continue",
     "do",         "else",      "extends",
     "false",      "final",     "finally",
     "for",        "goto",      "if",
     "implements", "import",    "instanceof",
     "int",        "interface", "native",
     "new",        "null",      "operator",
     "outer",      "package",   "private",
     "protected",  "public",    "return",
     "static",     "super",     "synchronized",
     "this",       "throw",     "throws",
     "transient",  "true",      "try",
     "volatile",   "while",
// Special reserved suffixes:
     "+Helper",    "+Holder",   "+Package",
// These following are not strictly keywords.  They
// are methods on java.lang.Object and, as such, must
// not have conflicts with methods defined on IDL
// interfaces.  Treat them the same as keywords.
     "clone",      "equals",       "finalize",
     "getClass",   "hashCode",     "notify",
     "notifyAll",  "toString",     "wait"};

  ///////////////
  // toJava-specific factory methods

  private org.glassfish.corba.idl.toJavaPortable.Helper _helper = null;        // <62023>
  public org.glassfish.corba.idl.toJavaPortable.Helper helper ()
  {
    if (_helper == null)
      if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
         _helper = new org.glassfish.corba.idl.toJavaPortable.Helper24();     // <d60023>
      else
         _helper = new org.glassfish.corba.idl.toJavaPortable.Helper();
    return _helper;
  } // helper

  private org.glassfish.corba.idl.toJavaPortable.ValueFactory _valueFactory = null;        // <62023>
  public org.glassfish.corba.idl.toJavaPortable.ValueFactory valueFactory ()
  {
    if (_valueFactory == null)
      if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
         _valueFactory = new org.glassfish.corba.idl.toJavaPortable.ValueFactory();     // <d60023>
      // else return null since shouldn't be used
    return _valueFactory;
  } // valueFactory

  private org.glassfish.corba.idl.toJavaPortable.DefaultFactory _defaultFactory = null;        // <62023>
  public org.glassfish.corba.idl.toJavaPortable.DefaultFactory defaultFactory ()
  {
    if (_defaultFactory == null)
      if (org.glassfish.corba.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
         _defaultFactory = new org.glassfish.corba.idl.toJavaPortable.DefaultFactory();     // <d60023>
      // else return null since shouldn't be used
    return _defaultFactory;
  } // defaultFactory

  private org.glassfish.corba.idl.toJavaPortable.Holder _holder = new org.glassfish.corba.idl.toJavaPortable.Holder();
  public org.glassfish.corba.idl.toJavaPortable.Holder holder ()
  {
    return _holder;
  } // holder

  private org.glassfish.corba.idl.toJavaPortable.Skeleton _skeleton = new org.glassfish.corba.idl.toJavaPortable.Skeleton();
  public org.glassfish.corba.idl.toJavaPortable.Skeleton skeleton ()
  {
    return _skeleton;
  } // skeleton

  private org.glassfish.corba.idl.toJavaPortable.Stub _stub = new org.glassfish.corba.idl.toJavaPortable.Stub();
  public org.glassfish.corba.idl.toJavaPortable.Stub stub ()
  {
    return _stub;
  } // stub

  // toJava-specific factory methods
  ///////////////
} // class Factories
