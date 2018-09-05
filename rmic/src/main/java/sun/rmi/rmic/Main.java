/*
 * Copyright (c) 1996, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package sun.rmi.rmic;

import java.io.OutputStream;

/**
 * Legacy main class for "rmic" program, allowing the old class name to be used to run it.
 */
public class Main extends org.glassfish.rmic.Main {

    /**
     * Constructor.
     */
    public Main(OutputStream out, String program) {
        super(out, program);
    }

    /**
     * Main program
     */
    public static void main(String argv[]) {
        Main compiler = new Main(System.out, "rmic");
        System.exit(compiler.compile(argv) ? 0 : 1);
    }
}
