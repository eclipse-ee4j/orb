/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.toa ;

import com.sun.corba.ee.spi.oa.ObjectAdapter ;

/** The Transient Object Adapter is used for standard RMI-IIOP and Java-IDL 
 * (legacy JDK 1.2) object implementations.  Its protocol for managing objects is very
 * simple: just connect and disconnect.  There is only a single TOA instance per ORB,
 * and its lifetime is the same as the ORB.  The TOA instance is always ready to receive
 * messages except when the ORB is shutting down.
 */
public interface TOA extends ObjectAdapter {
    /** Connect the given servant to the ORB by allocating a transient object key
     *  and creating an IOR and object reference using the current factory.
     */
    void connect( org.omg.CORBA.Object servant ) ;

    /** Disconnect the object from this ORB.
    */
    void disconnect( org.omg.CORBA.Object obj ) ;
}

