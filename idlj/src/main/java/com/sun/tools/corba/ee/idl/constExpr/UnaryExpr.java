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

package com.sun.tools.corba.ee.idl.constExpr;

// NOTES:

public abstract class UnaryExpr extends Expression
{
  public UnaryExpr (String operation, Expression unaryOperand)
  {
    _op      = operation;
    _operand = unaryOperand;
  } // ctor

  public void   op (String op) {_op = (op == null)? "": op;}
  public String op () {return _op;}

  public void       operand (Expression operand) {_operand = operand;}
  public Expression operand () {return _operand;}

  private String     _op      = "";
  private Expression _operand = null;
} // class UnaryExpr
