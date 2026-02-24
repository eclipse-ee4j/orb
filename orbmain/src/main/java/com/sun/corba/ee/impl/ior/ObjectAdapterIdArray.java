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

package com.sun.corba.ee.impl.ior ;

import java.util.Arrays ;
import java.util.Iterator ;

public class ObjectAdapterIdArray extends ObjectAdapterIdBase {
    private final String[] objectAdapterId ;

    @Override
    public boolean equals( Object obj ) {
        // Note that the superclass equals method is correct and general,
        // because it tests for equality using the iterator.  The state of
        // the derived classes simply implements the iterator.
        // This equals method is added here to appease findbugs.
        return super.equals( obj ) ;
    }

    public ObjectAdapterIdArray( String[] objectAdapterId )
    {
        this.objectAdapterId = (String[])objectAdapterId.clone() ;
    }

    /** 
     * Special constructor used for OA IDs of length 2.
     * @param name1 First name part
     * @param name2 Second name part
     */
    public ObjectAdapterIdArray( String name1, String name2 ) 
    {
        objectAdapterId = new String[2] ;
        objectAdapterId[0] = name1 ;
        objectAdapterId[1] = name2 ;
    }

    @Override
    public int getNumLevels()
    {
        return objectAdapterId.length ;
    }

    @Override
    public Iterator<String> iterator()
    {
        return Arrays.asList( objectAdapterId ).iterator() ;
    }

    @Override
    public String[] getAdapterName()
    {      
        return (String[])objectAdapterId.clone() ;
    }
}
