/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

public class SuperClass
{
    private String strValue;
    private int intValue;

    public SuperClass() {
        strValue = "Test";
        intValue = 3241;
    }

    public String toString() {

        StringBuffer sbuf = new StringBuffer();
        sbuf.append("SuperClass [strValue=");
        sbuf.append(strValue);
        sbuf.append(", intValue=");
        sbuf.append(intValue);
        sbuf.append(']');
        return sbuf.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SuperClass))
            return false;
        else {
            return intValue == ((SuperClass)obj).intValue
                && strValue.equals(((SuperClass)obj).strValue);
        }
    }
}
