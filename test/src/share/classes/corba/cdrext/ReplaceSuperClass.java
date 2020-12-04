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

import java.io.*;

public class ReplaceSuperClass implements Serializable
{
    private String strValue;
    private int intValue;

    public ReplaceSuperClass() {
        strValue = "Test";
        intValue = 3241;
    }

    @Override
    public String toString() {

        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ReplaceSuperClass [strValue=");
        sbuf.append(strValue);
        sbuf.append(", intValue=");
        sbuf.append(intValue);
        sbuf.append(']');
        return sbuf.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReplaceSuperClass))
            return false;
        else {
            return intValue == ((ReplaceSuperClass)obj).intValue
                && strValue.equals(((ReplaceSuperClass)obj).strValue);
        }
    }
    
    protected Object writeReplace() {
        System.out.println("---- writeReplace ----");

        Status.inWriteReplace();

        return this;
    }

    protected Object readResolve() {
        System.out.println("--- readResolve ---");

        Status.inReadResolve();

        return this;
    }
}
