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

package org.glassfish.corba.idl.constExpr;

// NOTES:

import org.glassfish.corba.idl.ConstEntry;

import java.math.BigInteger;

public class DefaultExprFactory implements ExprFactory
{
  public org.glassfish.corba.idl.constExpr.And and (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.And(left, right);
  } // and

  public org.glassfish.corba.idl.constExpr.BooleanAnd booleanAnd (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.BooleanAnd(left, right);
  } // booleanAnd

  public org.glassfish.corba.idl.constExpr.BooleanNot booleanNot (org.glassfish.corba.idl.constExpr.Expression operand)
  {
    return new org.glassfish.corba.idl.constExpr.BooleanNot(operand);
  } // booleanNot

  public org.glassfish.corba.idl.constExpr.BooleanOr booleanOr (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.BooleanOr(left, right);
  } // booleanOr

  public org.glassfish.corba.idl.constExpr.Divide divide (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Divide(left, right);
  } // divide

  public org.glassfish.corba.idl.constExpr.Equal equal (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Equal(left, right);
  } // equal

  public org.glassfish.corba.idl.constExpr.GreaterEqual greaterEqual (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.GreaterEqual(left, right);
  } // greaterEqual

  public org.glassfish.corba.idl.constExpr.GreaterThan greaterThan (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.GreaterThan(left, right);
  } // greaterThan

  public org.glassfish.corba.idl.constExpr.LessEqual lessEqual (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.LessEqual(left, right);
  } // lessEqual

  public org.glassfish.corba.idl.constExpr.LessThan lessThan (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.LessThan(left, right);
  } // lessThan

  public org.glassfish.corba.idl.constExpr.Minus minus (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Minus(left, right);
  } // minus

  public org.glassfish.corba.idl.constExpr.Modulo modulo (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Modulo(left, right);
  } // modulo

  public org.glassfish.corba.idl.constExpr.Negative negative (org.glassfish.corba.idl.constExpr.Expression operand)
  {
    return new org.glassfish.corba.idl.constExpr.Negative(operand);
  } // negative

  public org.glassfish.corba.idl.constExpr.Not not (org.glassfish.corba.idl.constExpr.Expression operand)
  {
    return new org.glassfish.corba.idl.constExpr.Not(operand);
  } // not

  public org.glassfish.corba.idl.constExpr.NotEqual notEqual (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.NotEqual(left, right);
  } // notEqual

  public org.glassfish.corba.idl.constExpr.Or or (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Or(left, right);
  } // or

  public org.glassfish.corba.idl.constExpr.Plus plus (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Plus(left, right);
  } // plus

  public org.glassfish.corba.idl.constExpr.Positive positive (org.glassfish.corba.idl.constExpr.Expression operand)
  {
    return new org.glassfish.corba.idl.constExpr.Positive(operand);
  } // positive

  public org.glassfish.corba.idl.constExpr.ShiftLeft shiftLeft (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.ShiftLeft(left, right);
  } // shiftLeft

  public org.glassfish.corba.idl.constExpr.ShiftRight shiftRight (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.ShiftRight(left, right);
  } // shiftRight

  public org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, Character charValue,
                                                              boolean isWide )
  {
    return new org.glassfish.corba.idl.constExpr.Terminal(representation, charValue, isWide );
  } // ctor

  public org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, Boolean booleanValue)
  {
    return new org.glassfish.corba.idl.constExpr.Terminal(representation, booleanValue);
  } // ctor

  // Support long long <daz>
  public org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, BigInteger bigIntegerValue)
  {
    return new org.glassfish.corba.idl.constExpr.Terminal(representation, bigIntegerValue);
  } // ctor

  //daz  public Terminal terminal (String representation, Long longValue)
  //       {
  //       return new Terminal (representation, longValue);
  //       } // ctor

  public org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, Double doubleValue)
  {
    return new org.glassfish.corba.idl.constExpr.Terminal(representation, doubleValue);
  } // ctor

  public org.glassfish.corba.idl.constExpr.Terminal terminal (String stringValue, boolean isWide )
  {
    return new org.glassfish.corba.idl.constExpr.Terminal(stringValue, isWide);
  } // ctor

  public org.glassfish.corba.idl.constExpr.Terminal terminal (ConstEntry constReference)
  {
    return new org.glassfish.corba.idl.constExpr.Terminal(constReference);
  } // ctor

  public org.glassfish.corba.idl.constExpr.Times times (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Times(left, right);
  } // times

  public org.glassfish.corba.idl.constExpr.Xor xor (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right)
  {
    return new org.glassfish.corba.idl.constExpr.Xor(left, right);
  } // xor
} // class DefaultExprFactory
