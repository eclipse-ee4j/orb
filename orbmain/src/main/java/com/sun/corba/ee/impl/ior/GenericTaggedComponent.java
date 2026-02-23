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

import com.sun.corba.ee.spi.ior.TaggedComponent ;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedData ;
import org.omg.CORBA.ORB ;
import org.omg.CORBA_2_3.portable.InputStream ;

/**
 * Generic representation of a tagged component of a type unknown to the ORB 
 */
@ManagedData
@Description( "Generic representation of a tagged component of a type "
    + "unknown to the ORB" ) 
public class GenericTaggedComponent extends GenericIdentifiable 
    implements TaggedComponent 
{
    public GenericTaggedComponent( int id, InputStream is ) 
    {
        super( id, is ) ;
    }

    public GenericTaggedComponent( int id, byte[] data ) 
    {
        super( id, data ) ;
    }
    
    @Override
    public org.omg.IOP.TaggedComponent getIOPComponent( ORB orb ) 
    {
        return new org.omg.IOP.TaggedComponent( getId(), 
            getData() ) ;
    }
}
