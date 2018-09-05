/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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

