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

import java.util.List ;
import java.util.Iterator ;

import com.sun.corba.ee.spi.orb.ORBVersion ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;

import com.sun.corba.ee.spi.orb.ORB ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.InheritedAttribute ;
import org.glassfish.gmbal.Description ;

/** An IOR is represented as a list of profiles.
* Only instances of TaggedProfile are contained in the list.
*/
@ManagedData
@Description( "Interoperable Object Reference: the internal structure of a remote object reference" )
public interface IOR extends List<TaggedProfile>, Writeable, MakeImmutable
{
    // This is used only for management
    @ManagedAttribute
    @Description( "The list of profiles in this IOR" ) 
    Iterator<TaggedProfile> getTaggedProfiles() ;

    ORB getORB() ;

    /** Return the type id string from the IOR.
    */
    @ManagedAttribute
    @Description( "The repository ID of the IOR" ) 
    String getTypeId() ;
   
    /** Return an iterator that iterates over tagged profiles with
    * identifier id.  It is not possible to modify the list through this
    * iterator.
    */
    Iterator<TaggedProfile> iteratorById( int id ) ;

    /** Return a representation of this IOR in the standard GIOP stringified
     * format that begins with "IOR:".
     */
    String stringify() ;

    /** Return a representation of this IOR in the standard GIOP marshalled
     * form.
     */
    org.omg.IOP.IOR getIOPIOR() ;

    /** Return true if this IOR has no profiles.
     */
    boolean isNil() ;

    /** Return true if this IOR is equivalent to ior.  Here equivalent means
     * that the typeids are the same, they have the same number of profiles,
     * and each profile is equivalent to the corresponding profile.
     */
    boolean isEquivalent(IOR ior) ;

    /** Return the IORTemplate for this IOR.  This is simply a list
     * of all TaggedProfileTemplates derived from the TaggedProfiles
     * of the IOR.  
     */
    IORTemplateList getIORTemplates() ;

    /** Return the first IIOPProfile in this IOR.
     */
    IIOPProfile getProfile() ;
}
