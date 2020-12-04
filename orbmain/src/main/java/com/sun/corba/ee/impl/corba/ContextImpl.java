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

import org.omg.CORBA.Any;
import org.omg.CORBA.Context;
import org.omg.CORBA.NVList;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

public final class ContextImpl extends Context {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private org.omg.CORBA.ORB _orb;

    public ContextImpl(org.omg.CORBA.ORB orb) 
    {
        _orb = orb;
    }

    public ContextImpl(Context parent) 
    {
        // Ignore: no wrapper available
    }
    
    public String context_name() 
    {
        throw wrapper.contextNotImplemented() ;
    }

    public Context parent() 
    {
        throw wrapper.contextNotImplemented() ;
    }

    public Context create_child(String name) 
    {
        throw wrapper.contextNotImplemented() ;
    }

    public void set_one_value(String propName, Any propValue) 
    {
        throw wrapper.contextNotImplemented() ;
    }

    public void set_values(NVList values) 
    {
        throw wrapper.contextNotImplemented() ;
    }


    public void delete_values(String propName) 
    {
        throw wrapper.contextNotImplemented() ;
    }

    public NVList get_values(String startScope, 
                             int opFlags, 
                             String propName) 
    {
        throw wrapper.contextNotImplemented() ;
    }
};

