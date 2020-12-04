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

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.ObjectKey ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;

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
