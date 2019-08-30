/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior ;

import com.sun.corba.ee.spi.orb.ORB ;

/** An IORFactory provides the capability of creating IORs.  It contains
 * some collection of TaggedProfileTemplates, which can be iterated over
 * for portable interceptors.
 */
public interface IORFactory extends Writeable, MakeImmutable {
    /** Construct an IOR containing the given ORB, typeid, and ObjectId.
     * The same ObjectId will be used for all TaggedProfileTemplates in
     * the IORFactory.
     * 
     * @param orb  orb to use
     * @param typeid typeID of IOR
     * @param oid ObjectID of IOR
     * @return constructed IOR
     */
    IOR makeIOR( ORB orb, String typeid, ObjectId oid ) ;

    /** Return true iff this.makeIOR(orb,typeid,oid).isEquivalent(
     * other.makeIOR(orb,typeid,oid) for all orb, typeid, and oid.
     * 
     * @param other IORFactory to check
     * @return if they are equal
     * @see IORFactory#makeIOR(com.sun.corba.ee.spi.orb.ORB, java.lang.String, com.sun.corba.ee.spi.ior.ObjectId) 
     */
    boolean isEquivalent( IORFactory other ) ;
}
