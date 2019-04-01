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
// -D52042<daz> Added protypical code for computing mixed-operand binary
//  expressions, which promotes result to Double only when the target type
//  is floating-point. Code violates spec, but may be usable at some future
//  time.

import com.sun.tools.corba.ee.idl.Util;

import java.math.BigInteger;

/**
 *
 **/
public class Divide extends BinaryExpr {
    /**
     * Constructor: set operation and operands.
     **/
    protected Divide(com.sun.tools.corba.ee.idl.constExpr.Expression leftOperand, com.sun.tools.corba.ee.idl.constExpr.Expression rightOperand) {
        super("/", leftOperand, rightOperand);
    } // ctor

    /**
     *
     **/
    public Object evaluate() throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException {
        try {
            Number l = (Number) left().evaluate();
            Number r = (Number) right().evaluate();

            boolean lIsNonInteger = l instanceof Float || l instanceof Double;
            boolean rIsNonInteger = r instanceof Float || r instanceof Double;

            if (lIsNonInteger && rIsNonInteger)
                value(new Double(l.doubleValue() / r.doubleValue()));
            else if (lIsNonInteger || rIsNonInteger) {
                String[] parameters = { Util.getMessage("EvaluationException.divide"), left().value().getClass().getName(),
                        right().value().getClass().getName() };
                throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
            } else {
                BigInteger tmpL = (BigInteger) l, tmpR = (BigInteger) r;
                value(tmpL.divide(tmpR));
            }
            // <d52042> Allow evaluation over mixed operands. Supplant code above.
            /*
             * Number l = (Number)left ().evaluate (); Number r = (Number)right ().evaluate ();
             *
             * boolean lIsNonInteger = l instanceof Float || l instanceof Double; boolean rIsNonInteger = r instanceof Float || r
             * instanceof Double;
             *
             * // Floating-point operands. if (lIsNonInteger && rIsNonInteger) { value (new Double (l.doubleValue () / r.doubleValue
             * ())); } // Integral operands. else if (!(lIsNonInteger || rIsNonInteger)) { BigInteger tmpL = (BigInteger)l, tmpR =
             * (BigInteger)r; value (tmpL.divide (tmpR)); } // Mixed operands: one operand is floating-point, the other is integral.
             * else { // Legal over floating-point types only. if (type ().equals ("float") || type ().equals ("double")) { value
             * (new Double (l.doubleValue () / r.doubleValue ())); } else { String[] parameters = {Util.getMessage
             * ("EvaluationException.divide"), left ().value ().getClass ().getName (), right ().value ().getClass ().getName ()};
             * throw new EvaluationException (Util.getMessage ("EvaluationException.1", parameters)); } }
             */
        } catch (ClassCastException e) {
            String[] parameters = { Util.getMessage("EvaluationException.divide"), left().value().getClass().getName(), right().value().getClass().getName() };
            throw new com.sun.tools.corba.ee.idl.constExpr.EvaluationException(Util.getMessage("EvaluationException.1", parameters));
        }
        return value();
    } // evaluate
} // class Divide
