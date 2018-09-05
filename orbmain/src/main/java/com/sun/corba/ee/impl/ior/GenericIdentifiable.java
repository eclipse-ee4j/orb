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

import java.util.Arrays ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.Identifiable ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

/**
 * @author 
 * This is used for unknown components and profiles.  A TAG_MULTICOMPONENT_PROFILE will be represented this way.
 */
@ManagedData
@Description( "A generic implementation of an IIOP encapsulation with an integer id" ) 
public abstract class GenericIdentifiable implements Identifiable 
{
    private int id;
    private byte data[];
    
    public GenericIdentifiable(int id, InputStream is) 
    {
        this.id = id ;
        data = EncapsulationUtility.readOctets( is ) ;
    }
    
    public int getId() 
    {
        return id ;
    }
    
    public void write(OutputStream os) 
    {
        os.write_ulong( data.length ) ;
        os.write_octet_array( data, 0, data.length ) ;
    }
    
    public String toString() 
    {
        return "GenericIdentifiable[id=" + getId() + "]" ;
    }
    
    public boolean equals(Object obj) 
    {
        if (obj == null)
            return false ;

        if (!(obj instanceof GenericIdentifiable))
            return false ;

        GenericIdentifiable encaps = (GenericIdentifiable)obj ;

        return (getId() == encaps.getId()) && 
            Arrays.equals( data, encaps.data ) ;
    }
   
    public int hashCode() 
    {
        int result = 17 ;
        for (int ctr=0; ctr<data.length; ctr++ )
            result = 37*result + data[ctr] ;
        return result ;
    }

    public GenericIdentifiable(int id, byte[] data) 
    {
        this.id = id ;
        this.data = (byte[])(data.clone()) ;
    }
    
    @ManagedAttribute
    @Description( "The tagged component or profile CDR encoded data" )
    public byte[] getData() 
    {
        return (byte[])data.clone() ;
    }
}
