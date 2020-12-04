/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObjectSuperSuper dataxssup0=" + dataxssup0
            + ", dataxssup1" + dataxssup1
            + "]";
    }

    public boolean equals(Object obj) {
        try {
            TestObjectSuperSuper other = (TestObjectSuperSuper)obj;
            
            if (other == null)
                return false;

            return (testObjectSuperSuperHasStreamDefaults() ||
                    other.testObjectSuperSuperHasStreamDefaults() ||
                    (dataxssup0 == other.dataxssup0 &&
                     dataxssup1.equals(other.dataxssup1)));
        } catch (ClassCastException cce) {
            return false;
        }
    }
}
