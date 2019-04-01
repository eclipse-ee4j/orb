/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

/**
 * This exception is thrown when an internal compiler error occurs
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
@SuppressWarnings("serial") // JDK implementation class
public class CompilerError extends Error {
    Throwable e;

    /**
     * Constructor
     */
    public CompilerError(String msg) {
        super(msg);
        this.e = this;
    }

    /**
     * Create an exception given another exception.
     */
    public CompilerError(Exception e) {
        super(e.getMessage());
        this.e = e;
    }

    public void printStackTrace() {
        if (e == this)
            super.printStackTrace();
        else
            e.printStackTrace();
    }
}
