/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.corba;

import java.util.List;
import java.util.ArrayList;

import org.omg.CORBA.Any;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;

import com.sun.corba.ee.spi.orb.ORB ;

public class NVListImpl extends NVList 
{
    private final static int     INITIAL_CAPACITY       = 4;

    private List<NamedValue> _namedValues;
    private ORB orb;

    public NVListImpl(ORB orb) 
    {
        // Note: This orb could be an instanceof ORBSingleton or ORB
        this.orb = orb;
        _namedValues = new ArrayList<NamedValue>(INITIAL_CAPACITY);
    }

    public NVListImpl(ORB orb, int size) 
    {
        this.orb = orb;

        // Note: the size arg is only a hint of the size of the NVList.
        _namedValues = new ArrayList<NamedValue>(size);
    }

    public synchronized int count() 
    {
        return _namedValues.size();
    }

    public synchronized NamedValue add(int flags)
    {
        NamedValue tmpVal = new NamedValueImpl(orb, "", new AnyImpl(orb), flags);
        _namedValues.add(tmpVal);
        return tmpVal;
    }

    public synchronized NamedValue add_item(String itemName, int flags)
    {
        NamedValue tmpVal = new NamedValueImpl(orb, itemName, new AnyImpl(orb), 
            flags);
        _namedValues.add(tmpVal);
        return tmpVal;
    }

    public synchronized NamedValue add_value(String itemName, Any val, int flags)
    {
        NamedValue tmpVal = new NamedValueImpl(orb, itemName, val, flags);
        _namedValues.add(tmpVal);
        return tmpVal;
    }

    public synchronized NamedValue item(int index)
        throws Bounds
    {
        try {
            return _namedValues.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }

    public synchronized void remove(int index)
        throws Bounds
    {
        try {
            _namedValues.remove(index);
        } catch (IndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }
}
