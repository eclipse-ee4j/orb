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

import corba.strm2.Testable;

/**
 * Initial class for start of evolution.
 */
public class TestObject implements Testable
{
    private static final long serialVersionUID = 378730127323820502L;

    private String desc;

    private Integer data0;
    private long data1;
    private String data2;

    public TestObject() {
        data0 = new Integer(342141);
        data1 = 1209409213L;
        data2 = "This is a test\u98DB";

        desc = "class0";
    }

    public boolean equals(Object obj) {
        try {
            TestObject other = (TestObject)obj;
            if (other == null)
                return false;

            return data0.equals(other.data0) &&
                data1 == other.data1 &&
                data2.equals(other.data2);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public String getDescription() {
        return desc;
    }

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObject desc=" + desc
            + ", data0=" + data0
            + ", data1=" + data1
            + ", data2= " + data2
            + "]";
    }
}


        
        
