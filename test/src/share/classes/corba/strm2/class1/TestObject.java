/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import corba.strm2.Testable;

/**
 * Second stage in evolution.  Added a superclass.
 */
public class TestObject extends TestObjectSuper implements Testable
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

        desc = "class1";
    }

    public boolean equals(Object obj) {
        try {
            TestObject other = (TestObject)obj;
            if (other == null)
                return false;

            return data0.equals(other.data0) &&
                data1 == other.data1 &&
                data2.equals(other.data2) &&
                super.equals(other);
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
