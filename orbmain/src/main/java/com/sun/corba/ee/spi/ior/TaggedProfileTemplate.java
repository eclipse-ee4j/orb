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

import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

import java.util.List ;
import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.orb.ORB ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.InheritedAttribute ;
import org.glassfish.gmbal.IncludeSubclass ;

/** Base template for creating TaggedProfiles.  A TaggedProfile will often contain
* tagged components.  A template that does not contain components acts like 
* an empty immutable list.
*
* @author Ken Cavanaugh
*/
@ManagedData
@Description( "A template for creating a TaggedProfile" ) 
@IncludeSubclass( { com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate.class } )
public interface TaggedProfileTemplate extends List<TaggedComponent>, 
    Identifiable, WriteContents, MakeImmutable
{    
    @ManagedAttribute
    @Description( "The list of TaggedComponents in this TaggedProfileTemplate" ) 
    public Iterator<TaggedComponent> getTaggedComponents() ;

    /** Return an iterator that iterates over tagged components with
    * identifier id.  It is not possible to modify the list through this
    * iterator.
    * @param id id to look up
    * @return Iterator over tagged components
    */
    public Iterator<TaggedComponent> iteratorById( int id ) ;

    public <T extends TaggedComponent> Iterator<T> iteratorById( int id, 
        Class<T> cls )  ;

    /** Create a TaggedProfile from this template.
     * @param oktemp template to create from
     * @param id id of object
     * @return created TaggedProfile
    */
    TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id ) ;

    /** Write the profile create( oktemp, id ) to the OutputStream os.
     * @param oktemp template to create from
     * @param id id of object
     * @param os stream to write to
     * @see #create(com.sun.corba.ee.spi.ior.ObjectKeyTemplate, com.sun.corba.ee.spi.ior.ObjectId)
    */
    void write( ObjectKeyTemplate oktemp, ObjectId id, OutputStream os) ;

    /** Return true if temp is equivalent to this template.  Equivalence
     * means that in some sense an invocation on a profile created by this
     * template has the same results as an invocation on a profile
     * created from temp.  Equivalence may be weaker than equality.  
     * @param temp template to compare with
     * @return true if they are equivalent
     */
    boolean isEquivalent( TaggedProfileTemplate temp );

    /** Return the tagged components in this profile (if any)
     * in the GIOP marshalled form, which is required for Portable
     * Interceptors.  Returns null if either the profile has no 
     * components, or if this type of profile can never contain
     * components.
     * @param orb ORB to get components from
     * @param id ID of components to look up
     * @return tagged components in this profile
     */
    org.omg.IOP.TaggedComponent[] getIOPComponents( 
        ORB orb, int id );
}
