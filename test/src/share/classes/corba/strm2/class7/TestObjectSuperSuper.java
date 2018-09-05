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

public class TestObjectSuperSuper implements Serializable
{
    public int dataxssup0;
    public Integer dataxssup1;

    public static final int INITIAL_DATAXSSUP0 = 512;
    public static final Integer INITIAL_DATAXSSUP1 = new Integer(102400);

    private static final long serialVersionUID = -6910104656092098469L;

    public TestObjectSuperSuper() {
        dataxssup0 = 512;
        dataxssup1 = new Integer(102400);
    }

    public boolean testObjectSuperSuperHasStreamDefaults() {
        return dataxssup0 == 0 && dataxssup1 == null;
    }

    public boolean equals(Object obj) {
        try {
            TestObjectSuperSuper other = (TestObjectSuperSuper)obj;
            
            if (other == null)
                return false;

            return (testObjectSuperSuperHasStreamDefaults() ||
                    other.testObjectSuperSuperHasStreamDefaults() ||
                    (dataxssup0 == other.dataxssup0 &&
                     dataxssup1.equals(other.dataxssup1))) &&
                super.equals(obj);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
    }
}
