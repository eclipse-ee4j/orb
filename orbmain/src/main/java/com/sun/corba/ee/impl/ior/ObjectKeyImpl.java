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

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.logging.IORSystemException;

/**
 * @author  Ken Cavanaugh
 */
public class ObjectKeyImpl implements ObjectKey 
{
    private static final IORSystemException wrapper =
        IORSystemException.self ;

    private ObjectKeyTemplate oktemp;
    private ObjectId id;
    private byte[] array;
    
    public ObjectKeyImpl( ObjectKeyTemplate oktemp, ObjectId id) {
        this.oktemp = oktemp ;
        this.id = id ;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ObjectKeyImpl)) {
            return false;
        }

        ObjectKeyImpl other = (ObjectKeyImpl)obj ;

        return oktemp.equals( other.oktemp ) &&
            id.equals( other.id ) ;
    }

    @Override
    public int hashCode()
    {
        return oktemp.hashCode() ^ id.hashCode() ;
    }

    public ObjectKeyTemplate getTemplate() 
    {
        return oktemp ;
    }

    public ObjectId getId()
    {
        return id ;
    }

    public void write( OutputStream os ) 
    {
        oktemp.write( id, os ) ;
    }

    public synchronized byte[] getBytes(org.omg.CORBA.ORB orb) 
    {
        if (array == null) {        
            EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream((ORB)orb);
            try {
                write(os);
                array = os.toByteArray();
            } finally {
                try {
                    os.close();
                } catch (java.io.IOException e) {
                    wrapper.ioexceptionDuringStreamClose(e);    
                }
            }
        }

        return array.clone() ;
    }

    public ServerRequestDispatcher getServerRequestDispatcher()
    {
        return oktemp.getServerRequestDispatcher( id ) ;
    }
}
