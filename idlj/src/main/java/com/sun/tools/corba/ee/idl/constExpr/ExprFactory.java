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

public interface ExprFactory
{
  And and (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.BooleanAnd booleanAnd (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.BooleanNot booleanNot (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.BooleanOr booleanOr (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Divide divide (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Equal equal (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.GreaterEqual greaterEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.GreaterThan greaterThan (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.LessEqual lessEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.LessThan lessThan (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Minus minus (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Modulo modulo (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Negative negative (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.Not not (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.NotEqual notEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Or or (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Plus plus (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Positive positive (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.ShiftLeft shiftLeft (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.ShiftRight shiftRight (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Character charValue,
                         boolean isWide );
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Boolean booleanValue);
  //daz  Terminal     terminal (String representation, Long longValue);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Double doubleValue);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, BigInteger bigIntegerValue);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String stringValue, boolean isWide );
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (ConstEntry constReference);
  com.sun.tools.corba.ee.idl.constExpr.Times times (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Xor xor (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
} // interface ExprFactory
