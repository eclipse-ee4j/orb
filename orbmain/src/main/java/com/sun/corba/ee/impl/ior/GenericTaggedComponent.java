/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import org.omg.CORBA.ORB ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.spi.ior.TaggedComponent ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.Description ;

/**
 * @author 
 */
@ManagedData
@Description( "Generic representation of a tagged component of a type "
    + "unknown to the ORB" ) 
public class GenericTaggedComponent extends GenericIdentifiable 
    implements TaggedComponent 
{
    public GenericTaggedComponent( int id, InputStream is ) 
    {
        super( id, is ) ;
    }

    public GenericTaggedComponent( int id, byte[] data ) 
    {
        super( id, data ) ;
    }
    
    /**
     * @return org.omg.IOP.TaggedComponent
     * @author 
     */
    public org.omg.IOP.TaggedComponent getIOPComponent( ORB orb ) 
    {
        return new org.omg.IOP.TaggedComponent( getId(), 
            getData() ) ;
    }
}
