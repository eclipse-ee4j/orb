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
