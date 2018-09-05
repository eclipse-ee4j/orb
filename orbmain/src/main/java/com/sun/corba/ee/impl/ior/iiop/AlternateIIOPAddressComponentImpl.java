/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.TaggedComponentBase ;

import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent ;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS ;

/**
 * @author Ken Cavanaugh
 */
public class AlternateIIOPAddressComponentImpl extends TaggedComponentBase
    implements AlternateIIOPAddressComponent
{
    private IIOPAddress addr ;

    public boolean equals( Object obj )
    {
        if (!(obj instanceof AlternateIIOPAddressComponentImpl))
            return false ;

        AlternateIIOPAddressComponentImpl other = 
            (AlternateIIOPAddressComponentImpl)obj ;

        return addr.equals( other.addr ) ;
    }
     
    public int hashCode() 
    {
        return addr.hashCode() ;
    }

    public String toString()
    {
        return "AlternateIIOPAddressComponentImpl[addr=" + addr + "]" ;
    }

    public AlternateIIOPAddressComponentImpl( IIOPAddress addr ) 
    {
        this.addr = addr ;
    }
    
    public IIOPAddress getAddress()
    {
        return addr ;
    }

    public void writeContents(OutputStream os) 
    {
        addr.write( os ) ;
    }
    
    public int getId() 
    {
        return TAG_ALTERNATE_IIOP_ADDRESS.value ; // 3 in CORBA 2.3.1 13.6.3
    }
}
