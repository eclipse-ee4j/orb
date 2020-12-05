/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package rmic;

import sun.rmi.rmic.Main;
import sun.tools.java.ClassPath;
import java.io.OutputStream;

public class TestEnv extends sun.rmi.rmic.iiop.BatchEnvironment {

    private boolean firstLine = true;
    
    public TestEnv(ClassPath path, OutputStream out) {
        super(out,path,new Main(System.out, "rmic"));
    }
    
    public TestEnv(ClassPath path) {
        super(System.out,path,new Main(System.out, "rmic"));
    }

    public void reset() {
        firstLine = true;
        nerrors = 0;
        nwarnings = 0;
        ndeprecations = 0;
        super.reset();
    }
    
    public void output(String msg) {
        if (firstLine) {
            System.out.println();
            firstLine = false;
        }
        System.out.println("          " + msg);
    }
}

