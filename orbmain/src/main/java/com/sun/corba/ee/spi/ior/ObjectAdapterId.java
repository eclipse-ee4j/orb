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

import java.util.Iterator ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

/** This is the object adapter ID for an object adapter.
* Typically this is the path of strings starting from the
* Root POA to get to a POA, but other implementations are possible.
*/
@ManagedData
@Description( "The identifier for a particular Object adapter in the ORB" ) 
public interface ObjectAdapterId extends Iterable<String>, Writeable {
    /** Return the number of elements in the adapter ID.
     * @return number of elements
    */
    int getNumLevels() ;

    /** Return an iterator that iterates over the components 
    * of this adapter ID.  Each element is returned as a String.
    * 
    * @return Sequence of strings in the ObjectAdapterId 
    */
    @ManagedAttribute
    @Description( "Sequence of strings in the ObjectAdapterId" ) 
    @Override
    Iterator<String> iterator();

    /** Get the adapter name simply as an array of strings.
     * @return adapter name
    */
    String[] getAdapterName() ;
}
