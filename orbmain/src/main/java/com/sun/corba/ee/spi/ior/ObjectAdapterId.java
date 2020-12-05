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
    * @return iterator over components
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
