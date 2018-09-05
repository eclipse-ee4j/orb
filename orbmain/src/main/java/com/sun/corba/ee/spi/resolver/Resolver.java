/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.resolver ;

/** Resolver defines the operations needed to support ORB operations for 
 * resolve_initial_references and list_initial_services.
 */
public interface Resolver {
    /** Look up the name using this resolver and return the CORBA object
     * reference bound to this name, if any.  Returns null if no object
     * is bound to the name.
     */
    org.omg.CORBA.Object resolve( String name ) ;

    /** Return the entire collection of names that are currently bound 
     * by this resolver.  Resulting collection contains only strings for
     * which resolve does not return null.  Some resolvers may not support
     * this method, in which case they return an empty set.
     */
    java.util.Set<String> list() ;
}
