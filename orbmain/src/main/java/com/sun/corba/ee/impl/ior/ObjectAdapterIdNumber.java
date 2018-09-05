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

import org.omg.CORBA_2_3.portable.OutputStream ;

/** ObjectAdapterIdNumber is used to represent pre-JDK 1.4 POA adapter
 * IDs.  The POA ID was simply represented as a single integer, which was
 * mapped to the actual POA instance.  Here, we just represent these
 * internally as arrays of the form { "OldRootPOA", "<number>" },
 * and provide an extra method to get the number back.
 */
public class ObjectAdapterIdNumber extends ObjectAdapterIdArray {
    private int poaid ;

    public ObjectAdapterIdNumber( int poaid ) 
    {
        super( "OldRootPOA", Integer.toString( poaid ) ) ;
        this.poaid = poaid ;
    }

    public int getOldPOAId()
    {
        return poaid ;
    }
}
