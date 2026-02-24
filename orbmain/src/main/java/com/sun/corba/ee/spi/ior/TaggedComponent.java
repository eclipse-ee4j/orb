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

import com.sun.corba.ee.impl.ior.GenericTaggedComponent ;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent ;
import com.sun.corba.ee.spi.ior.iiop.CodeSetsComponent ;
import com.sun.corba.ee.spi.ior.iiop.JavaCodebaseComponent ;
import com.sun.corba.ee.spi.ior.iiop.MaxStreamFormatVersionComponent ;
import com.sun.corba.ee.spi.ior.iiop.ORBTypeComponent ;
import com.sun.corba.ee.spi.ior.iiop.RequestPartitioningComponent ;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.IncludeSubclass ;
import org.glassfish.gmbal.ManagedData ;
import org.omg.CORBA.ORB ;

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
