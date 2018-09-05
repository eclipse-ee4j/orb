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

import org.omg.IOP.TAG_ORB_TYPE ;

import com.sun.corba.ee.spi.ior.TaggedComponentBase ;

import com.sun.corba.ee.spi.ior.iiop.ORBTypeComponent ;

import org.omg.CORBA_2_3.portable.OutputStream ;

/**
 * @author Ken Cavanaugh
 */
public class ORBTypeComponentImpl extends TaggedComponentBase 
    implements ORBTypeComponent
{
    private int ORBType;
   
    public boolean equals( Object obj )
    {
        if (!(obj instanceof ORBTypeComponentImpl))
            return false ;

        ORBTypeComponentImpl other = (ORBTypeComponentImpl)obj ;

        return ORBType == other.ORBType ;
    }

    public int hashCode()
    {
        return ORBType ;
    }

    public String toString()
    {
        return "ORBTypeComponentImpl[ORBType=" + ORBType + "]" ;
    }

    public ORBTypeComponentImpl(int ORBType) 
    {
        this.ORBType = ORBType ;
    }
    
    public int getId() 
    {
        return TAG_ORB_TYPE.value ; // 0 in CORBA 2.3.1 13.6.3
    }
    
    public int getORBType() 
    {
        return ORBType ;
    }
    
    public void writeContents(OutputStream os) 
    {
        os.write_ulong( ORBType ) ;
    }
}
