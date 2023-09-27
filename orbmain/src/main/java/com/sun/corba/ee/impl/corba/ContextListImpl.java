/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.corba;

import java.util.List;
import java.util.ArrayList;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.ORB;

public class ContextListImpl extends ContextList {
    private final static int INITIAL_CAPACITY = 2;

    private org.omg.CORBA.ORB _orb;
    private List<String> _contexts;

    public ContextListImpl(org.omg.CORBA.ORB orb) {
        // Note: This orb could be an instanceof ORBSingleton or ORB
        _orb = orb;
        _contexts = new ArrayList<String>(INITIAL_CAPACITY);
    }

    public synchronized int count() {
        return _contexts.size();
    }

    public synchronized void add(String ctxt) {
        _contexts.add(ctxt);
    }

    public synchronized String item(int index) throws Bounds {
        try {
            return _contexts.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }

    public synchronized void remove(int index) throws Bounds {
        try {
            _contexts.remove(index);
        } catch (IndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }
}
