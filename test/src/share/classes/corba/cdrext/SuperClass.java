/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

package corba.cdrext;

public class SuperClass
{
    private String strValue;
    private int intValue;

    public SuperClass() {
        strValue = "Test";
        intValue = 3241;
    }

    @Override
    public String toString() {

        StringBuilder sbuf = new StringBuilder();
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
