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
import org.glassfish.rmic.tools.asm.Label;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
class CodeContext extends Context {
    Label breakLabel;
    Label contLabel;

    /**
     * Create a new nested context, for a block statement
     */
    CodeContext(Context ctx, Node node) {
        super(ctx, node);
        switch (node.op) {
        case DO:
        case WHILE:
        case FOR:
        case FINALLY:
        case SYNCHRONIZED:
            this.breakLabel = new Label();
            this.contLabel = new Label();
            break;
        case SWITCH:
        case TRY:
        case INLINEMETHOD:
        case INLINENEWINSTANCE:
            this.breakLabel = new Label();
            break;
        default:
            if ((node instanceof Statement) && (((Statement) node).labels != null)) {
                this.breakLabel = new Label();
            }
        }
    }
}
