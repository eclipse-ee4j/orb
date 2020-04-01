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

import org.glassfish.corba.idl.constExpr.DefaultExprFactory;
import org.glassfish.corba.idl.constExpr.ExprFactory;

/**
 * If the framework is being extended, this class must be extended.
 * At very least, the genFactory method must be overridden to return
 * the code generator extensions.  The remaining methods may be overridden
 * if necessary:
 * <dl>
 * <dt>symtabFactory
 * <dd>If you wish to extend the symbol table entries, this method must return the factory which constructs those extensions.  If you only want to extend a few of the symbol table entries, it may be useful to extend DefaultSymtabFactory and only override the pertinent methods.
 * <dt>exprFactory
 * <dd>If you wish to extend the expression classes, this method must return the factory which constructs those extensions.  If you only want to extend a few of the expression classes, it may be useful to extend com.sun.tools.corba.ee.idl.constExpr.DefaultSymtabFactory and only override the pertinent methods.
 * <dt>arguments
 * <dd>If you wish to add additional arguments to the base set of arguments, extend Arguments and override this method to return that class.
 * <dt>languageKeywords
 * <dd>If the language you are generating code in has keywords other than IDL keywords, these keywords should be returned by this method.  The framework will prepend any IDL identifiers it encounters which are in this list with an underscore (`_') to avoid compilation errors.  For instance, `catch' is a Java keyword.  If the generators are emitting Java code for the following IDL, emitting `catch' as is will cause compile errors, so it is changed to `_catch':
 * <br>
 * IDL:
 * <br>
 * const long catch = 22;
 * <br>
 * Possible generated code:
 * <br>
 * public static final int _catch = 22;
 * </dl>
 **/
public class Factories
{
  /** Return the implementation of the GenFactory interface.  If this
      returns null, then the compiler cannot generate anything.
    * @return {@code} null
    */
  public GenFactory genFactory ()
  {
    return null;
  } // genFactory

  /** Return the implementation of the SymtabFactory interface.  If this
      returns null, the default symbol table entries will be used.
    * @return {@link DefaultSymtabFactory}
    */
  public SymtabFactory symtabFactory ()
  {
    return new DefaultSymtabFactory();
  } // symtabFactory

  /** Return the implementation of the ExprFactory interface.  If this
      returns null, the default expressions will be used.
    * @return {@link DefaultSymtabFactory}
    */
  public ExprFactory exprFactory ()
  {
    return new DefaultExprFactory();
  } // exprFactory

  /** Return a subclass of the Arguments class.  If this returns null,
    * the default will be used.
    * @return a new instance of {@link Arguments}
    */
  public Arguments arguments ()
  {
    return new Arguments();
  } // arguments

  /** Return the list of keywords in the generated language.
      Note that these keywords may contain the following wildcards:
      <dl>
      <dt>`*'
      <dd>matches zero or more characters
      <dt>`+'
      <dd>matches one or more characters
      <dt>`.'
      <dd>matches any single character
      </dl>
    * @return an array of keywords
    */
  public String[] languageKeywords ()
  {
    return null;
  } // languageKeywords
} // interface Factories
