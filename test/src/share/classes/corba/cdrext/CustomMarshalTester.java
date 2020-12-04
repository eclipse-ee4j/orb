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

package corba.cdrext;

import java.io.*;
import java.util.*;

public class CustomMarshalTester extends MarshalTester implements Serializable
{
    private transient List items;

    public CustomMarshalTester() {
        items = new LinkedList();
    }

    public void add(Object obj) {
        items.add(obj);
    }

    public Iterator iterator() {
        return items.iterator();
    }

    public boolean equals(Object obj) {
        try {
            return super.equals(obj) 
                && items.equals(((CustomMarshalTester)obj).items);
        } catch (ClassCastException cce) {
            cce.printStackTrace();
            return false;
        }
    }

    public int size() {
        return items.size();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {

        s.defaultWriteObject();

        s.writeInt(size());

        Iterator iter = iterator();

        while (iter.hasNext())
            s.writeObject(iter.next());
    }
    
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {

        if (items != null)
            throw new IOException("Default constructor was invoked [1]!");

        s.defaultReadObject();

        if (items != null)
            throw new IOException("Default constructor was invoked [2]!");

        int numItems = s.readInt();

        items = new LinkedList();

        for (int i = 0; i < numItems; i++)
            items.add(s.readObject());
    }
}

