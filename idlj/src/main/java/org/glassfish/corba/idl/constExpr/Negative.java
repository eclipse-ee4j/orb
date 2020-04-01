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

import org.glassfish.corba.idl.Util;

import java.math.BigInteger;

public class Negative extends UnaryExpr
{
  protected Negative (org.glassfish.corba.idl.constExpr.Expression operand)
  {
    super ("-", operand);
  } // ctor

  public Object evaluate () throws org.glassfish.corba.idl.constExpr.EvaluationException
  {
    try
    {
      Number op = (Number)operand ().evaluate ();

      if (op instanceof Float || op instanceof Double)
        value (new Double (-op.doubleValue ()));
      else
      {
        // Multiply by -1
        //daz        value (new Long (-op.longValue ()));
        BigInteger tmpOp = (BigInteger)op;
        value (tmpOp.multiply (BigInteger.valueOf (-1)));
     }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.neg"), operand ().value ().getClass ().getName ()};
      throw new org.glassfish.corba.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } // evaluate
} // class Negative
