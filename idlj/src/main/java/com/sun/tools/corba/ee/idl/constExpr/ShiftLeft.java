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

public class ShiftLeft extends BinaryExpr {
    protected ShiftLeft(com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand) {
        super("<<", leftOperand, rightOperand);
    } // ctor

    public Object evaluate() throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException {
        try {
            Number l = (Number) left().evaluate();
            Number r = (Number) right().evaluate();

            if (l instanceof Float || l instanceof Double || r instanceof Float || r instanceof Double) {
                String[] parameters = { Util.getMessage("EvaluationException.left"), left().value().getClass().getName(),
                        right().value().getClass().getName() };
                throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
            } else {
                // Shift left (<<)
                // daz value (new Long (l.longValue () << r.longValue ()));
                BigInteger bL = (BigInteger) coerceToTarget(l);
                BigInteger bR = (BigInteger) r;

                BigInteger ls = bL.shiftLeft(bR.intValue());

                if (type().indexOf("short") >= 0)
                    ls = ls.mod(twoPow16);
                else if (type().indexOf("long") >= 0)
                    ls = ls.mod(twoPow32);
                else if (type().indexOf("long long") >= 0)
                    ls = ls.mod(twoPow64);

                value(coerceToTarget(ls));
            }
        } catch (ClassCastException e) {
            String[] parameters = { Util.getMessage("EvaluationException.left"), left().value().getClass().getName(), right().value().getClass().getName() };
            throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
        }
        return value();
    } // evaluate
} // class ShiftLeft
