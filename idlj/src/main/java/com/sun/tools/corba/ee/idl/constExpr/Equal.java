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

public class Equal extends BinaryExpr {
    protected Equal(com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand) {
        super("==", leftOperand, rightOperand);
    } // ctor

    public Object evaluate() throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException {
        try {
            Object left = left().evaluate();
            if (left instanceof Boolean) {
                Boolean l = (Boolean) left;
                Boolean r = (Boolean) right().evaluate();
                value(Boolean.valueOf(l.booleanValue() == r.booleanValue()));
            } else {
                Number l = (Number) left;
                Number r = (Number) right().evaluate();
                if (l instanceof Float || l instanceof Double || r instanceof Float || r instanceof Double)
                    value(new Boolean(l.doubleValue() == r.doubleValue()));
                else
                    // daz value (Boolean.valueOf (l.longValue () == r.longValue ()));
                    value(Boolean.valueOf(((BigInteger) l).equals((BigInteger) r)));
            }
        } catch (ClassCastException e) {
            String[] parameters = { Util.getMessage("EvaluationException.equal"), left().value().getClass().getName(), right().value().getClass().getName() };
            throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
        }
        return value();
    } // evaluate
} // class Equal
