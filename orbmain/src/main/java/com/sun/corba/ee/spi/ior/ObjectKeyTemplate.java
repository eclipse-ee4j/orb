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

import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

/** An ObjectKeyTemplate represents the part of an Object Key
 * that corresponds to the object adapter used to create an
 * object reference.  The template is shared between many
 * object references.
 */
@ManagedData
@Description( "The template used to represent all IORs created by the same Object adapter" )
public interface ObjectKeyTemplate extends Writeable
{
    @ManagedAttribute
    @Description( "The ORB version that created this template" )
    public ORBVersion getORBVersion() ;

    /** An ID used to determine how to perform operations on this
     * ObjectKeyTemplate.  This id determines how to process requests
     * on this object reference, and what object adapter type to use.
     * 
     * @return The subcontract ID which identifies a particular type-independent 
     * implementation of an IOR
     */
    @ManagedAttribute
    @Description( "The subcontract ID which identifies a particular type-independent " 
        + " implementation of an IOR" )
    public int getSubcontractId();

    /** Return the server ID for this template.
    * For CORBA 3.0, this should be a String, but it is currently
    * an int in the object key template.
    * @return The ID of the server that handles requests to this IOR"
    */
    @ManagedAttribute
    @Description( "The ID of the server that handles requests to this IOR" )
    public int getServerId() ;

    /** Return the ORB ID for this template.
     * @return the ORB ID that created this IOR
    */
    @ManagedAttribute
    @Description( "the ORB ID that created this IOR" )
    public String getORBId() ;

    /** Return the object adapter ID for this template.
     * @return The ObjectAdapterId that identifies the ObjectAdapter that created this IOR
    */
    @ManagedAttribute
    @Description( "The ObjectAdapterId that identifies the ObjectAdapter that created this IOR" )
    public ObjectAdapterId getObjectAdapterId() ;

    /** Compute an adapter ID for this template than includes
    * all of the template information.
    * This value is cached to avoid the expense of recomputing
    * it.
    * @return adapter ID for this template
    */
    public byte[] getAdapterId() ;

    public void write(ObjectId objectId, OutputStream os);
    
    public ServerRequestDispatcher getServerRequestDispatcher( ObjectId id ) ;
}
