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

public class BooleanAnd extends BinaryExpr {
    protected BooleanAnd(com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand) {
        super("&&", leftOperand, rightOperand);
    } // ctor

    public Object evaluate() throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException {
        try {
            Object tmpL = left().evaluate();
            Object tmpR = right().evaluate();
            Boolean l;
            Boolean r;

            // daz if (tmpL instanceof Number)
            // l = new Boolean (((Number)tmpL).longValue () != 0);
            // else
            // l = (Boolean)tmpL;
            if (tmpL instanceof Number) {
                if (tmpL instanceof BigInteger)
                    l = Boolean.valueOf(((BigInteger) tmpL).compareTo(BigInteger.valueOf(0)) != 0);
                else
                    l = Boolean.valueOf(((Number) tmpL).longValue() != 0);
            } else
                l = (Boolean) tmpL;
            // daz if (tmpR instanceof Number)
            // r = new Boolean (((Number)tmpR).longValue () != 0);
            // else
            // r = (Boolean)tmpR;
            if (tmpR instanceof Number) {
                if (tmpR instanceof BigInteger)
                    r = Boolean.valueOf(((BigInteger) tmpR).compareTo(zero) != 0);
                else
                    r = Boolean.valueOf(((Number) tmpR).longValue() != 0);
            } else
                r = (Boolean) tmpR;

            value(new Boolean(l.booleanValue() && r.booleanValue()));
        } catch (ClassCastException e) {
            String[] parameters = { Util.getMessage("EvaluationException.booleanAnd"), left().value().getClass().getName(),
                    right().value().getClass().getName() };
            throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
        }
        return value();
    } // evaluate
} // class BooleanAnd
