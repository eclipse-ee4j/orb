/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA.ORB ;

import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent ;
import com.sun.corba.ee.spi.ior.iiop.CodeSetsComponent ;                
import com.sun.corba.ee.spi.ior.iiop.JavaCodebaseComponent ;
import com.sun.corba.ee.spi.ior.iiop.MaxStreamFormatVersionComponent ;
import com.sun.corba.ee.spi.ior.iiop.ORBTypeComponent ;
import com.sun.corba.ee.spi.ior.iiop.RequestPartitioningComponent ;

import com.sun.corba.ee.impl.ior.GenericTaggedComponent ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.IncludeSubclass ;

/** Generic interface for all tagged components.  Users of the ORB may
* create implementations of this class and also corresponding factories
* of type TaggedComponentFactory.  The factories can be registered with an
* ORB instance, in which case they will be used to unmarshal IORs containing
* the registered tagged component.
*/
@ManagedData
@Description( "Base class for all TaggedComponents" )
@IncludeSubclass( { AlternateIIOPAddressComponent.class, 
    CodeSetsComponent.class, JavaCodebaseComponent.class,
    MaxStreamFormatVersionComponent.class, ORBTypeComponent.class,
    RequestPartitioningComponent.class,
    GenericTaggedComponent.class } )
public interface TaggedComponent extends Identifiable
{
    org.omg.IOP.TaggedComponent getIOPComponent( ORB orb ) ;
}
