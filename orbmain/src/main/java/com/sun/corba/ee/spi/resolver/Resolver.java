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

package com.sun.corba.ee.spi.resolver ;

/** Resolver defines the operations needed to support ORB operations for 
 * resolve_initial_references and list_initial_services.
 */
public interface Resolver {
    /** Look up the name using this resolver and return the CORBA object
     * reference bound to this name, if any.
     * @param name name of CORBA object to look up
     * @return {@code null} if no object is bound to the name.
     */
    org.omg.CORBA.Object resolve( String name ) ;

    /** Return the entire collection of names that are currently bound 
     * by this resolver.  Resulting collection contains only strings for
     * which resolve does not return null.  Some resolvers may not support
     * this method, in which case they return an empty set.
     * @return All currently bound names
     */
    java.util.Set<String> list() ;
}
