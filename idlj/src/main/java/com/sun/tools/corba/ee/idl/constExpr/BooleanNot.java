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

import com.sun.tools.corba.ee.idl.Util;

import java.math.BigInteger;

public class BooleanNot extends UnaryExpr
{
  protected BooleanNot (com.sun.tools.corba.ee.idl.constExpr.Expression operand)
  {
    super ("!", operand);
  } // ctor

  public Object evaluate () throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException
  {
    try
    {
      Object tmp = operand ().evaluate ();
      Boolean op;
      //daz      if (tmp instanceof Number)
      //           op = new Boolean (((Number)tmp).longValue () != 0);
      //         else
      //           op = (Boolean)tmp;
      if (tmp instanceof Number)
      {
        if (tmp instanceof BigInteger)
          op = Boolean.valueOf (((BigInteger)tmp).compareTo (zero) != 0);
        else
          op = Boolean.valueOf (((Number)tmp).longValue () != 0);
      }
      else
        op = (Boolean)tmp;

      value (Boolean.valueOf (!op.booleanValue ()));
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.booleanNot"), operand ().value ().getClass ().getName ()};
      throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } // evaluate
} // class BooleanNot
