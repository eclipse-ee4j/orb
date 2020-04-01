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

public class Plus extends BinaryExpr
{
  protected Plus (org.glassfish.corba.idl.constExpr.Expression leftOperand, org.glassfish.corba.idl.constExpr.Expression rightOperand)
  {
    super ("+", leftOperand, rightOperand);
  } // ctor

  public Object evaluate () throws org.glassfish.corba.idl.constExpr.EvaluationException
  {
    try
    {
      Number l = (Number)left ().evaluate ();
      Number r = (Number)right ().evaluate ();

      boolean lIsNonInteger = l instanceof Float || l instanceof Double;
      boolean rIsNonInteger = r instanceof Float || r instanceof Double;

      if (lIsNonInteger && rIsNonInteger)
        value (new Double (l.doubleValue () + r.doubleValue ()));
      else if (lIsNonInteger || rIsNonInteger)
      {
        String[] parameters = {Util.getMessage ("EvaluationException.plus"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
        throw new org.glassfish.corba.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.1", parameters));
      }
      else
      {
        // Addition (+)
        BigInteger tmpL = (BigInteger)l,  tmpR = (BigInteger)r;
        value (tmpL.add (tmpR));
        //daz        value (new Long (l.longValue () + r.longValue ()));
      }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.plus"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
      throw new org.glassfish.corba.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.1", parameters));
    }
    return value ();
  } // evaluate
} // class Plus
