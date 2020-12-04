/*
 * Copyright (c) 1994, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.tree;

import org.glassfish.rmic.tools.java.*;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
abstract public
class BinaryLogicalExpression extends BinaryExpression {
    /**
     * constructor
     */
    public BinaryLogicalExpression(int op, long where, Expression left, Expression right) {
        super(op, where, Type.tBoolean, left, right);
    }

    /**
     * Check a binary expression
     */
    public Vset checkValue(Environment env, Context ctx,
                           Vset vset, Hashtable<Object, Object> exp) {
        ConditionVars cvars = new ConditionVars();
        // evaluate the logical expression, determining which variables are
        // set if the resulting value is true or false
        checkCondition(env, ctx, vset, exp, cvars);
        // return the intersection.
        return cvars.vsTrue.join(cvars.vsFalse);
    }

    /*
     * Every subclass of this class must define a genuine implementation
     * of this method.  It cannot inherit the method of Expression.
     */
    abstract
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars);


    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        return this;
    }
}
