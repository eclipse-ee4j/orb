/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;

import org.omg.CORBA.BAD_PARAM ;

import com.sun.corba.ee.impl.misc.ORBUtility ;

import com.sun.corba.ee.spi.ior.IOR ;

import com.sun.corba.ee.spi.orb.ORB ;

/**
 * Thrown to signal an OBJECT_FORWARD or LOCATION_FORWARD
 */
public class ForwardException extends RuntimeException {
    private ORB orb ;
    private org.omg.CORBA.Object obj;
    private IOR ior ;

    public ForwardException( ORB orb, IOR ior ) {
        super();

        this.orb = orb ;
        this.obj = null ;
        this.ior = ior ;
    }

    public ForwardException( ORB orb, org.omg.CORBA.Object obj) {
        super();

        // This check is done early so that no attempt
        // may be made to do a location forward to a local
        // object.  Doing this lazily would allow 
        // forwarding to locals in some restricted cases.
        if (obj instanceof org.omg.CORBA.LocalObject)
            throw new BAD_PARAM() ;

        this.orb = orb ;
        this.obj = obj ;
        this.ior = null ;
    }

    public synchronized org.omg.CORBA.Object getObject()
    {
        if (obj == null) {
            obj = ORBUtility.makeObjectReference( ior ) ;
        }

        return obj ;
    }

    public synchronized IOR getIOR() 
    {
        if (ior == null) {
            ior = orb.getIOR( obj, false ) ;
        }

        return ior ;
    }
}
