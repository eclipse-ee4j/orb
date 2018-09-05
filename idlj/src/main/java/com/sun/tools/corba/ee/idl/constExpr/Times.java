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

public class Times extends BinaryExpr
{
  protected Times (com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand)
  {
    super ("*", leftOperand, rightOperand);
  } // ctor

  public Object evaluate () throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException
  {
    try
    {
      Number l = (Number)left ().evaluate ();
      Number r = (Number)right ().evaluate ();

      boolean lIsNonInteger = l instanceof Float || l instanceof Double;
      boolean rIsNonInteger = r instanceof Float || r instanceof Double;

      if (lIsNonInteger && rIsNonInteger)
        value (new Double (l.doubleValue () * r.doubleValue ()));
      else if (lIsNonInteger || rIsNonInteger)
      {
        String[] parameters = {Util.getMessage ("EvaluationException.times"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
        throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.1", parameters));
      }
      else
      {
        // Multiplication (*)
        BigInteger tmpL = (BigInteger)l,  tmpR = (BigInteger)r;
        value (tmpL.multiply (tmpR));
        //daz        value (new Long (l.longValue () * r.longValue ()));
      }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.times"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
      throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.1", parameters));
    }
    return value ();
  } // evaluate
} // class Times
