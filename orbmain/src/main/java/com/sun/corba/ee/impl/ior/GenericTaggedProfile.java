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

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.ObjectKey ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;

/**
 * @author 
 */
public class GenericTaggedProfile extends GenericIdentifiable implements TaggedProfile 
{
    private ORB orb ;

    public GenericTaggedProfile( int id, InputStream is ) 
    {
        super( id, is ) ;
        this.orb = (ORB)(is.orb()) ;
    }

    public GenericTaggedProfile( ORB orb, int id, byte[] data ) 
    {
        super( id, data ) ;
        this.orb = orb ;
    }
    
    public TaggedProfileTemplate getTaggedProfileTemplate() 
    {
        return null ;
    }

    public ObjectId getObjectId() 
    {
        return null ;
    }

    public ObjectKeyTemplate getObjectKeyTemplate() 
    {
        return null ;
    }

    public ObjectKey getObjectKey() 
    {
        return null ;
    }

    public boolean isEquivalent( TaggedProfile prof ) 
    {
        return equals( prof ) ;
    }

    public void makeImmutable()
    {
        // NO-OP
    }

    public boolean isLocal() 
    {
        return false ;
    }
    
    public org.omg.IOP.TaggedProfile getIOPProfile() 
    {
        EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream( orb ) ;
        write( os ) ;
        InputStream is = (InputStream)(os.create_input_stream()) ;
        return org.omg.IOP.TaggedProfileHelper.read( is ) ;
    }
}
