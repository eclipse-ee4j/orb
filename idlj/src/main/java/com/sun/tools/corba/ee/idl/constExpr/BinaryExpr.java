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

public abstract class BinaryExpr extends Expression
{
  public BinaryExpr (String operation, Expression leftOperand, Expression rightOperand)
  {
    _op    = operation;
    _left  = leftOperand;
    _right = rightOperand;
  } // ctor

  public void op (String op) {_op = (op == null)? "": op;}
  public String op () {return _op;}

  public void       left (Expression left) {_left = left;}
  public Expression left () {return _left;}

  public void       right (Expression right) {_right = right;}
  public Expression right () {return _right;}

  private String     _op    = "";
  private Expression _left  = null;
  private Expression _right = null;
} // class BinaryExpr
