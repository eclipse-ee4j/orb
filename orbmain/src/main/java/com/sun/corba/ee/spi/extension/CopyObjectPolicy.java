/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.extension ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.LocalObject ;
import com.sun.corba.ee.spi.misc.ORBConstants ;

/** Policy used to specify the copyObject implementation to use.
*/
public class CopyObjectPolicy extends LocalObject implements Policy
{
    private final int value ;

    public CopyObjectPolicy( int value ) 
    {
        this.value = value ;
    }

    public int getValue()
    {
        return value ;
    }

    public int policy_type ()
    {
        return ORBConstants.COPY_OBJECT_POLICY ;
    }

    public org.omg.CORBA.Policy copy ()
    {
        return this ;
    }

    public void destroy ()
    {
        // NO-OP
    }

    public String toString() 
    {
        return "CopyObjectPolicy[" + value + "]" ;
    }
}
