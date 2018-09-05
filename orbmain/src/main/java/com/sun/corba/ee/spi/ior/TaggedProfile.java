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

import com.sun.corba.ee.spi.orb.ORB ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.IncludeSubclass ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;

/** TaggedProfile represents a tagged profile in an IOR.
 * A profile contains all of the information necessary for an invocation.
 * It contains one or more endpoints that may be used for an invocation.
 * A TaggedProfile conceptually has three parts: A TaggedProfileTemplate,
 * an ObjectKeyTemplate, and an ObjectId.  
 */
@ManagedData
@Description( "A TaggedProfile contained in an IOR" )
@IncludeSubclass( { com.sun.corba.ee.spi.ior.iiop.IIOPProfile.class } ) 
public interface TaggedProfile extends Identifiable, MakeImmutable
{
    @ManagedAttribute
    @Description( "Template for this TaggedProfile" ) 
    TaggedProfileTemplate getTaggedProfileTemplate() ;

    @ManagedAttribute
    @Description( "The ObjectId used in the IIOPProfile in this IOR" )
    ObjectId getObjectId() ;

    @ManagedAttribute
    @Description( "The template for the ObjectKey in the IIOPProfile in this IOR" ) 
    ObjectKeyTemplate getObjectKeyTemplate() ;

    ObjectKey getObjectKey() ;

    /** Return true is prof is equivalent to this TaggedProfile.
     * This means that this and prof are indistinguishable for 
     * the purposes of remote invocation.  Typically this means that
     * the profile data is identical and both profiles contain exactly
     * the same components (if components are applicable).
     * isEquivalent( prof ) should imply that getObjectId().equals( 
     * prof.getObjectId() ) is true, and so is
     * getObjectKeyTemplate().equals( prof.getObjectKeyTemplate() ).
     */
    boolean isEquivalent( TaggedProfile prof ) ;

    /** Return the TaggedProfile as a CDR encapsulation in the standard
     * format.  This is required for Portable interceptors.
     */
    org.omg.IOP.TaggedProfile getIOPProfile();

    /** Return true if this TaggedProfile was created in orb.  
     *  Caches the result.
     */
    boolean isLocal() ;
}
