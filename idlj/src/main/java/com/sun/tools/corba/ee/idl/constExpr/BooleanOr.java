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

import com.sun.tools.corba.ee.idl.Util;

import java.math.BigInteger;

public class BooleanOr extends BinaryExpr
{
  protected BooleanOr (com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand)
  {
    super ("||", leftOperand, rightOperand);
  } // ctor

  public Object evaluate () throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException
  {
    try
    {
      Object tmpL = left ().evaluate ();
      Object tmpR = right ().evaluate ();
      Boolean l;
      Boolean r;

      //daz   if (tmpL instanceof Number)
      //        l = new Boolean (((Number)tmpL).longValue () != 0);
      //      else
      //        l = (Boolean)tmpL;
      if (tmpL instanceof Number)
      {
        if (tmpL instanceof BigInteger)
          l = Boolean.valueOf (((BigInteger)tmpL).compareTo (zero) != 0);
        else
          l = Boolean.valueOf (((Number)tmpL).longValue () != 0);
      }
      else
        l = (Boolean)tmpL;
      //daz   if (tmpR instanceof Number)
      //        r = new Boolean (((Number)tmpR).longValue () != 0);
      //      else
      //        r = (Boolean)tmpR;
      if (tmpR instanceof Number)
      {
        if (tmpR instanceof BigInteger)
          r = Boolean.valueOf (((BigInteger)tmpR).compareTo (BigInteger.valueOf (0)) != 0);
        else
          r = Boolean.valueOf (((Number)tmpR).longValue () != 0);
      }
      else
        r = (Boolean)tmpR;
      value (Boolean.valueOf (l.booleanValue () || r.booleanValue ()));
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.booleanOr"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
      throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage ("EvaluationException.1", parameters));
    }
    return value ();
  } // evaluate
} // class BooleanOr
