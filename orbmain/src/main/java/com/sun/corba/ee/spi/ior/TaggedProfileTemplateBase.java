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

package com.sun.corba.ee.spi.ior;

import com.sun.corba.ee.impl.ior.EncapsulationUtility ;
import com.sun.corba.ee.spi.orb.ORB ;

import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.OutputStream ;

public abstract class TaggedProfileTemplateBase 
    extends IdentifiableContainerBase<TaggedComponent> 
    implements TaggedProfileTemplate
{   
    public void write( OutputStream os )
    {
        EncapsulationUtility.writeEncapsulation( this, os ) ;
    }

    public org.omg.IOP.TaggedComponent[] getIOPComponents( ORB orb, int id )
    {
        int count = 0 ;
        Iterator<TaggedComponent> iter = iteratorById( id ) ;
        while (iter.hasNext()) {
            iter.next() ;
            count++ ;
        }

        org.omg.IOP.TaggedComponent[] result = new
            org.omg.IOP.TaggedComponent[count] ;

        int index = 0 ;
        iter = iteratorById( id ) ;
        while (iter.hasNext()) {
            TaggedComponent comp = iter.next() ;
            result[index++] = comp.getIOPComponent( orb ) ;
        }

        return result ;
    }

    public <T extends TaggedComponent> Iterator<T> iteratorById( int id,
        Class<T> cls ) {

        return (Iterator<T>)iteratorById( id ) ;
    }
}
