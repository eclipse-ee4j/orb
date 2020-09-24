/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

import java.io.*;

public class ReplaceSubClass extends ReplaceSuperClass
{
    private long longValue;

    public ReplaceSubClass() {
        longValue = 92431;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReplaceSubClass))
            return false;
        else {
            return super.equals(obj)
                && longValue == ((ReplaceSubClass)obj).longValue;
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ReplaceSubClass [longValue=");
        sbuf.append(longValue);
        sbuf.append(", ");
        sbuf.append(super.toString());
        sbuf.append(']');
        return sbuf.toString();
    }
}
