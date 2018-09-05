/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.tree;

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.asm.Assembler;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class SuperExpression extends ThisExpression {

    /**
     * Constructor
     */
    public SuperExpression(long where) {
        super(SUPER, where);
    }

    /**
     * Constructor for "outer.super()"
     */
    public SuperExpression(long where, Expression outerArg) {
        super(where, outerArg);
        op = SUPER;
    }

    public SuperExpression(long where, Context ctx) {
        super(where, ctx);
        op = SUPER;
    }

    /**
     * Check expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = checkCommon(env, ctx, vset, exp);
        if (type != Type.tError) {
            // "super" is not allowed in this context:
            env.error(where, "undef.var.super", idSuper);
        }
        return vset;
    }

    /**
     * Check if the present name is part of a scoping prefix.
     */
    public Vset checkAmbigName(Environment env, Context ctx,
                               Vset vset, Hashtable<Object, Object> exp,
                               UnaryExpression loc) {
        return checkCommon(env, ctx, vset, exp);
    }

    /** Common code for checkValue and checkAmbigName */
    private Vset checkCommon(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        ClassDeclaration superClass = ctx.field.getClassDefinition().getSuperClass();
        if (superClass == null) {
            env.error(where, "undef.var", idSuper);
            type = Type.tError;
            return vset;
        }
        vset = super.checkValue(env, ctx, vset, exp);
        type = superClass.getType();
        return vset;
    }

}
