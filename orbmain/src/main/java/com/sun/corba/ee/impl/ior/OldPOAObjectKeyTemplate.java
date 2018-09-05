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

import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;


/**
 * @author Ken Cavanaugh
 */
public final class OldPOAObjectKeyTemplate extends OldObjectKeyTemplateBase 
{
    /** This constructor reads the template ONLY from the stream
    */
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is ) 
    {
        this( orb, magic, scid, is.read_long(), is.read_long(), is.read_long() ) ;
    }
    
    /** This constructor reads a complete ObjectKey (template and Id)
    * from the stream.
    */
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is,
        OctetSeqHolder osh ) 
    {
        this( orb, magic, scid, is ) ;
        osh.value = readObjectKey( is ) ;
    }
    
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, int serverid, 
        int orbid, int poaid) 
    {
        super( orb, magic, scid, serverid,
            Integer.toString( orbid ), 
            new ObjectAdapterIdNumber( poaid ) ) ;
    }
    
    public void writeTemplate(OutputStream os) 
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;

        int orbid = Integer.parseInt( getORBId() ) ;
        os.write_long( orbid ) ;

        ObjectAdapterIdNumber oaid = (ObjectAdapterIdNumber)(getObjectAdapterId()) ;
        int poaid = oaid.getOldPOAId()  ;
        os.write_long( poaid ) ;
    }
 
    @Override
    public ORBVersion getORBVersion()
    {
        if (getMagic() == ObjectKeyFactoryImpl.JAVAMAGIC_OLD)
            return ORBVersionFactory.getOLD() ;
        else if (getMagic() == ObjectKeyFactoryImpl.JAVAMAGIC_NEW)
            return ORBVersionFactory.getNEW() ;
        else
            throw new INTERNAL() ;
    }
}

