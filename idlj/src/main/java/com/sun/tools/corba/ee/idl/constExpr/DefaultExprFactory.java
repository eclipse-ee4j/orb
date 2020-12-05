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

package com.sun.tools.corba.ee.idl.constExpr;

// NOTES:

import com.sun.tools.corba.ee.idl.ConstEntry;

import java.math.BigInteger;

public class DefaultExprFactory implements ExprFactory
{
  public com.sun.tools.corba.ee.idl.constExpr.And and (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.And(left, right);
  } // and

  public com.sun.tools.corba.ee.idl.constExpr.BooleanAnd booleanAnd (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.BooleanAnd(left, right);
  } // booleanAnd

  public com.sun.tools.corba.ee.idl.constExpr.BooleanNot booleanNot (com.sun.tools.corba.ee.idl.constExpr.Expression operand)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.BooleanNot(operand);
  } // booleanNot

  public com.sun.tools.corba.ee.idl.constExpr.BooleanOr booleanOr (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.BooleanOr(left, right);
  } // booleanOr

  public com.sun.tools.corba.ee.idl.constExpr.Divide divide (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Divide(left, right);
  } // divide

  public com.sun.tools.corba.ee.idl.constExpr.Equal equal (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Equal(left, right);
  } // equal

  public com.sun.tools.corba.ee.idl.constExpr.GreaterEqual greaterEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.GreaterEqual(left, right);
  } // greaterEqual

  public com.sun.tools.corba.ee.idl.constExpr.GreaterThan greaterThan (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.GreaterThan(left, right);
  } // greaterThan

  public com.sun.tools.corba.ee.idl.constExpr.LessEqual lessEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.LessEqual(left, right);
  } // lessEqual

  public com.sun.tools.corba.ee.idl.constExpr.LessThan lessThan (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.LessThan(left, right);
  } // lessThan

  public com.sun.tools.corba.ee.idl.constExpr.Minus minus (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Minus(left, right);
  } // minus

  public com.sun.tools.corba.ee.idl.constExpr.Modulo modulo (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Modulo(left, right);
  } // modulo

  public com.sun.tools.corba.ee.idl.constExpr.Negative negative (com.sun.tools.corba.ee.idl.constExpr.Expression operand)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Negative(operand);
  } // negative

  public com.sun.tools.corba.ee.idl.constExpr.Not not (com.sun.tools.corba.ee.idl.constExpr.Expression operand)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Not(operand);
  } // not

  public com.sun.tools.corba.ee.idl.constExpr.NotEqual notEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.NotEqual(left, right);
  } // notEqual

  public com.sun.tools.corba.ee.idl.constExpr.Or or (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Or(left, right);
  } // or

  public com.sun.tools.corba.ee.idl.constExpr.Plus plus (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Plus(left, right);
  } // plus

  public com.sun.tools.corba.ee.idl.constExpr.Positive positive (com.sun.tools.corba.ee.idl.constExpr.Expression operand)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Positive(operand);
  } // positive

  public com.sun.tools.corba.ee.idl.constExpr.ShiftLeft shiftLeft (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.ShiftLeft(left, right);
  } // shiftLeft

  public com.sun.tools.corba.ee.idl.constExpr.ShiftRight shiftRight (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.ShiftRight(left, right);
  } // shiftRight

  public com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Character charValue,
    boolean isWide )
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Terminal(representation, charValue, isWide );
  } // ctor

  public com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Boolean booleanValue)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Terminal(representation, booleanValue);
  } // ctor

  // Support long long <daz>
  public com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, BigInteger bigIntegerValue)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Terminal(representation, bigIntegerValue);
  } // ctor

  //daz  public Terminal terminal (String representation, Long longValue)
  //       {
  //       return new Terminal (representation, longValue);
  //       } // ctor

  public com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Double doubleValue)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Terminal(representation, doubleValue);
  } // ctor

  public com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String stringValue, boolean isWide )
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Terminal(stringValue, isWide);
  } // ctor

  public com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (ConstEntry constReference)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Terminal(constReference);
  } // ctor

  public com.sun.tools.corba.ee.idl.constExpr.Times times (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Times(left, right);
  } // times

  public com.sun.tools.corba.ee.idl.constExpr.Xor xor (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right)
  {
    return new com.sun.tools.corba.ee.idl.constExpr.Xor(left, right);
  } // xor
} // class DefaultExprFactory
