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

public class Xor extends BinaryExpr {
    protected Xor(com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand) {
        super("^", leftOperand, rightOperand);
    } // ctor

    public Object evaluate() throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException {
        try {
            Number l = (Number) left().evaluate();
            Number r = (Number) right().evaluate();

            if (l instanceof Float || l instanceof Double || r instanceof Float || r instanceof Double) {
                String[] parameters = { Util.getMessage("EvaluationException.xor"), left().value().getClass().getName(), right().value().getClass().getName() };
                throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
            } else {
                // Xor (^)
                // daz value (new Long (l.longValue () ^ r.longValue ()));
                // BigInteger uL = (BigInteger)toUnsigned((BigInteger)l);
                // BigInteger uR = (BigInteger)toUnsigned((BigInteger)r);
                // value (coerceToTarget(uL.xor (uR)));
                BigInteger uL = (BigInteger) coerceToTarget((BigInteger) l);
                BigInteger uR = (BigInteger) coerceToTarget((BigInteger) r);
                value(uL.xor(uR));
            }
        } catch (ClassCastException e) {
            String[] parameters = { Util.getMessage("EvaluationException.xor"), left().value().getClass().getName(), right().value().getClass().getName() };
            throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
        }
        return value();
    } // evaluate
} // class Xor
