/*
 * Copyright (c) 1996, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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
