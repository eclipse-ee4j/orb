/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior ;

import java.util.Iterator ;
import java.util.Arrays ;

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

    /** Special constructor used for OA IDs of length 2. 
     */
    public ObjectAdapterIdArray( String name1, String name2 ) 
    {
        objectAdapterId = new String[2] ;
        objectAdapterId[0] = name1 ;
        objectAdapterId[1] = name2 ;
    }

    public int getNumLevels()
    {
        return objectAdapterId.length ;
    }

    public Iterator<String> iterator()
    {
        return Arrays.asList( objectAdapterId ).iterator() ;
    }

    public String[] getAdapterName()
    {      
        return (String[])objectAdapterId.clone() ;
    }
}
