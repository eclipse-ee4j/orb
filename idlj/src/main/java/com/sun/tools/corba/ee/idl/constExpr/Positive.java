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

public class Positive extends UnaryExpr {
    protected Positive(com.sun.tools.corba.ee.idl.constExpr.Expression operand) {
        super("+", operand);
    } // ctor

    public Object evaluate() throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException {
        try {
            Number op = (Number) operand().evaluate();

            if (op instanceof Float || op instanceof Double)
                value(new Double(+op.doubleValue()));
            else {
                // Multiply by sign
                // daz value (new Long (+op.longValue ()));
                value(((BigInteger) op).multiply(BigInteger.valueOf(((BigInteger) op).signum())));
                // promote ();
            }
        } catch (ClassCastException e) {
            String[] parameters = { Util.getMessage("EvaluationException.pos"), operand().value().getClass().getName() };
            throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.2", parameters));
        }
        return value();
    } // evaluate
} // class Positive
