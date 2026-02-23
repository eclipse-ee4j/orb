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

import com.sun.corba.ee.spi.ior.ObjectAdapterId ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;

import org.omg.CORBA.OctetSeqHolder ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

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
     * @param orb ORB to use
     * @param magic Magic number
     * @param scid ID of template
     * @param is stream to read from
    */
    public POAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is ) 
    {
        super( orb, magic, scid, is.read_long(), is.read_string(),
            new ObjectAdapterIdArray( readPOAName( is ) ) ) ;

        setORBVersion( is ) ;
    }

    /** This constructor reads a complete ObjectKey (template and Id)
    * from the stream.
     * @param orb  ORB to use
     * @param magic Magic number
     * @param scid ID of the Object
     * @param is Stream to read from
     * @param osh Holder for Octet
    */
    public POAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is, OctetSeqHolder osh )  {
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
    
    @Override
    public void writeTemplate(OutputStream os) 
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;
        os.write_string( getORBId() ) ;
        getObjectAdapterId().write( os ) ;
    }
}
