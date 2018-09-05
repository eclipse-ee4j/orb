/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import java.io.*;

public class TestObjectSuperSub extends TestObjectSuper 
{
    public int dataxss0;
    public Long dataxss1;

    public static final int INITIAL_DATAXSS0 = 256;
    public static final Long INITIAL_DATAXSS1 = new Long(128000L);

    private static final long serialVersionUID = -2547646174414134925L;

    public TestObjectSuperSub() {
        dataxss0 = 256;
        dataxss1 = new Long(128000L);
    }

    public boolean testObjectSuperSubHasStreamDefaults() {
        return dataxss0 == 0 && dataxss1 == null;
    }

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObjectSuperSub dataxss0=" + dataxss0
            + ", dataxss1" + dataxss1
            + "]";
    }

    public boolean equals(Object obj) {
        try {
            TestObjectSuperSub other = (TestObjectSuperSub)obj;
            
            if (other == null)
                return false;

            return (testObjectSuperSubHasStreamDefaults() ||
                    other.testObjectSuperSubHasStreamDefaults() ||
                    (dataxss0 == other.dataxss0 &&
                     dataxss1.equals(other.dataxss1))) &&
                super.equals(obj);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}



    

