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
import org.omg.CORBA_2_3.portable.OutputStream ;

import org.omg.CORBA.OctetSeqHolder ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;

import com.sun.corba.ee.spi.ior.ObjectAdapterId ;

import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl ;

/**
 * @author 
 */
public final class POAObjectKeyTemplate extends NewObjectKeyTemplateBase 
{
    public static String[] readPOAName(
        org.omg.CORBA.portable.InputStream istream)
    {
        String value[] = null;
        int _len0 = istream.read_long();
        value = new String[_len0];
        for (int _o1 = 0;_o1 < value.length; ++_o1) {
            value[_o1] = istream.read_string();
        }
        return value;
    }

    /** This constructor reads the template ONLY from the stream.
    */
    public POAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is ) 
    {
        super( orb, magic, scid, is.read_long(), is.read_string(),
            new ObjectAdapterIdArray( readPOAName( is ) ) ) ;

        setORBVersion( is ) ;
    }

    /** This constructor reads a complete ObjectKey (template and Id)
    * from the stream.
    */
    public POAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is,
        OctetSeqHolder osh ) 
    {
        super( orb, magic, scid, is.read_long(), is.read_string(),
            new ObjectAdapterIdArray( readPOAName( is ) ) ) ;
        
        osh.value = readObjectKey( is ) ;

        setORBVersion( is ) ;
    }
    
    public POAObjectKeyTemplate( ORB orb, int scid, int serverid, String orbid, 
        ObjectAdapterId objectAdapterId) 
    {
        super( orb, ObjectKeyFactoryImpl.JAVAMAGIC_NEWER, scid, serverid, orbid,
            objectAdapterId ) ;

        setORBVersion( ORBVersionFactory.getORBVersion() ) ;
    }
    
    public void writeTemplate(OutputStream os) 
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;
        os.write_string( getORBId() ) ;
        getObjectAdapterId().write( os ) ;
    }
}
