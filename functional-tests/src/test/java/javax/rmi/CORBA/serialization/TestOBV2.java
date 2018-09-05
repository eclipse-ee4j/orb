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

package javax.rmi.CORBA.serialization;

import java.util.*;
import java.io.*;

public class TestOBV2 extends TestOBV implements java.io.Serializable {
    public int bar;
    public static int foo = 1;
    public TestOBV2 self = null;
    public static final int FOO = 3;
    public TestOBV2 arrayOfThis[];

    public TestOBV2(){
        super();
        self = this;
        bar = new Random().nextInt();
        arrayOfThis = new TestOBV2[3];
        arrayOfThis[0] = null;
        arrayOfThis[1] = this;
        arrayOfThis[2] = null;
    }

    public boolean equals (Object o){
        try
            {
                TestOBV2 target = (TestOBV2)o;
                return ((target != null) &&
                        (target.self == target) &&      
                        (target.arrayOfThis != null) &&
                        (target.arrayOfThis[0] == null) &&
                        (target.arrayOfThis[1] == target) &&
                        (target.arrayOfThis[0] == null) &&
                        (target.bar == bar) &&
                        (target.foo == foo) &&
                        (target.FOO == FOO));
            }
        catch(Throwable t)
            {
                return false;
            }
    }
}
