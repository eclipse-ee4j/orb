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

public interface ExprFactory
{
  And and (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.BooleanAnd booleanAnd (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.BooleanNot booleanNot (org.glassfish.corba.idl.constExpr.Expression operand);
  org.glassfish.corba.idl.constExpr.BooleanOr booleanOr (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Divide divide (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Equal equal (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.GreaterEqual greaterEqual (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.GreaterThan greaterThan (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.LessEqual lessEqual (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.LessThan lessThan (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Minus minus (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Modulo modulo (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Negative negative (org.glassfish.corba.idl.constExpr.Expression operand);
  org.glassfish.corba.idl.constExpr.Not not (org.glassfish.corba.idl.constExpr.Expression operand);
  org.glassfish.corba.idl.constExpr.NotEqual notEqual (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Or or (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Plus plus (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Positive positive (org.glassfish.corba.idl.constExpr.Expression operand);
  org.glassfish.corba.idl.constExpr.ShiftLeft shiftLeft (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.ShiftRight shiftRight (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, Character charValue,
                                                       boolean isWide );
  org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, Boolean booleanValue);
  //daz  Terminal     terminal (String representation, Long longValue);
  org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, Double doubleValue);
  org.glassfish.corba.idl.constExpr.Terminal terminal (String representation, BigInteger bigIntegerValue);
  org.glassfish.corba.idl.constExpr.Terminal terminal (String stringValue, boolean isWide );
  org.glassfish.corba.idl.constExpr.Terminal terminal (ConstEntry constReference);
  org.glassfish.corba.idl.constExpr.Times times (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
  org.glassfish.corba.idl.constExpr.Xor xor (org.glassfish.corba.idl.constExpr.Expression left, org.glassfish.corba.idl.constExpr.Expression right);
} // interface ExprFactory
