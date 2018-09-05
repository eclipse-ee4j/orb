/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.custom;

import java.io.*;

/**
 * Simple data structure with a java.lang.Object array of links
 * and a data value.  Custom marshaled.
 */
public class ArrayListNode implements Serializable
{
    public Object[] next;
    public String data;

    // writeObject is necessary to make this custom marshaled,
    // but readObject isn't.
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        // Don't do anything unusual.  Serialization will be
        // normal except chunking will be used.
        out.defaultWriteObject();
    }
}
