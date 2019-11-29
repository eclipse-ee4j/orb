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

import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;

import com.sun.corba.ee.spi.orb.ORB ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
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
     * @return the repository ID
    */
    @ManagedAttribute
    @Description( "The repository ID of the IOR" ) 
    String getTypeId() ;
   
    /** Return an iterator that iterates over tagged profiles with
    * identifier id.  It is not possible to modify the list through this
    * iterator.
    * 
    * @param id id of tagged profiles
    * @return iterator of all tags
    */
    Iterator<TaggedProfile> iteratorById( int id ) ;

    /** Return a representation of this IOR in the standard GIOP stringified
     * format that begins with "IOR:".
     * This does not return the same as {@link Object#toString}
     * @return String representation
     * @see java.lang.Object#toString() 
     */
    String stringify() ;

    /** Return a representation of this IOR in the standard GIOP marshalled
     * form.
     * @return a representation of this IOR
     */
    org.omg.IOP.IOR getIOPIOR() ;

    /** Return true if this IOR has no profiles.
     * @return true if there aren't any profiles, false otherwise
     */
    boolean isNil() ;

    /** Return true if this IOR is equivalent to ior.  Here equivalent means
     * that the typeids are the same, they have the same number of profiles,
     * and each profile is equivalent to the corresponding profile.
     * @param ior IOR to compare to
     * @return true if they are equivalent
     * @see #equals(java.lang.Object)
     */
    boolean isEquivalent(IOR ior) ;
    
    /**
     * Return true if this IOR is equivalent to ior.  Here equivalent means
     * that the typeids and delegates are the same. It does not check if the profiles
     * are the same or of the same number.
     * @param other object to compare to
     * @return true if they are equivalent
     * @see #isEquivalent(IOR)
     */
    @Override
    boolean equals(Object other);

    /** Return the IORTemplate for this IOR.  This is simply a list
     * of all TaggedProfileTemplates derived from the TaggedProfiles
     * of the IOR.
     * @return the IORTemplate for this IOR
     */
    IORTemplateList getIORTemplates() ;

    /** Return the first IIOPProfile in this IOR.
     * @return the first IIOPProfile
     */
    IIOPProfile getProfile() ;
}
