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

import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;

/** The full object key, which is contained in an IIOPProfile.
* The object identifier corresponds to the information passed into
* POA::create_reference_with_id and POA::create_reference
* (in the POA case).  The template 
* represents the information that is object adapter specific and 
* shared across multiple ObjectKey instances.
*/
public interface ObjectKey extends Writeable
{
    /** Return the object identifier for this Object key.
    */
    ObjectId getId() ;

    /** Return the template for this object key.
    */
    ObjectKeyTemplate getTemplate()  ;

    byte[] getBytes( org.omg.CORBA.ORB orb ) ;
    
    ServerRequestDispatcher getServerRequestDispatcher() ;
}
