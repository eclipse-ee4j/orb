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

public class Not extends UnaryExpr
{
  protected Not (com.sun.tools.corba.ee.idl.constExpr.Expression operand)
  {
    super ("~", operand);
  } // ctor

  public Object evaluate () throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException
  {
    try
    {
      Number op = (Number)operand ().evaluate ();

      if (op instanceof Float || op instanceof Double)
      {
        String[] parameters = {Util.getMessage ("EvaluationException.not"), operand ().value ().getClass ().getName ()};
        throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.2", parameters));
      }
      else
      {
        // Complement (~)
        //daz        value (new Long (~op.longValue ()));
        BigInteger b = (BigInteger)coerceToTarget((BigInteger)op);

        // Compute according to CORBA 2.1 specifications for specified type.
        if (type ().equals ("short") || type ().equals ("long") || type ().equals ("long long"))
          value (b.add (one).multiply (negOne));
        else if (type ().equals("unsigned short"))
          // "short" not CORBA compliant, but necessary for logical operations--size matters!
          value (twoPow16.subtract (one).subtract (b));
        else if (type ().equals ("unsigned long"))
          value (twoPow32.subtract (one).subtract (b));
        else if (type ().equals ("unsigned long long"))
          value (twoPow64.subtract (one).subtract (b));
        else
          value (b.not ());  // Should never execute...
      }
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.not"), operand ().value ().getClass ().getName ()};
      throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } // evaluate
} // class Not
