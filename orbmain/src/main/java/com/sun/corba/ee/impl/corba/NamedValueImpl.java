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

import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Any;

import com.sun.corba.ee.spi.orb.ORB ;

public class NamedValueImpl extends NamedValue 
{
    private String _name;
    private Any    _value;
    private int    _flags;
    private ORB    _orb;
 
    public NamedValueImpl(ORB orb) 
    {
        // Note: This orb could be an instanceof ORBSingleton or ORB
        _orb = orb;
        _value = new AnyImpl(_orb);
    }

    public NamedValueImpl(ORB orb,
                          String name, 
                          Any value, 
                          int flags) 
    {
        // Note: This orb could be an instanceof ORBSingleton or ORB
        _orb    = orb;
        _name   = name;
        _value  = value;
        _flags      = flags;
    }
    
    public String name() 
    {
        return _name;
    }

    public Any value()
    {
        return _value;
    }

    public int flags()
    {
        return _flags;
    }
}
