/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.ior;

import com.sun.corba.ee.spi.ior.Identifiable ;

import java.util.Arrays ;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedData ;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

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
