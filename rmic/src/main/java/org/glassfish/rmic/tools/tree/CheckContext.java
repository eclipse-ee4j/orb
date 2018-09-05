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

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class CheckContext extends Context {
    public Vset vsBreak = Vset.DEAD_END;
    public Vset vsContinue = Vset.DEAD_END;

    // Accumulate (join) all DA/DU state prior to
    // any abnormal exit from a try-statement.
    // This field is ignored unless this
    // context is associated with a try-statement.
    public Vset vsTryExit = Vset.DEAD_END;

    /**
     * Create a new nested context, for a block statement
     */
    CheckContext(Context ctx, Statement stat) {
        super(ctx, stat);
    }
}
