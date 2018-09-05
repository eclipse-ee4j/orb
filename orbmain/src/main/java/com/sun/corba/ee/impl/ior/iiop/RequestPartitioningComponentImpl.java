/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;
import com.sun.corba.ee.spi.ior.iiop.RequestPartitioningComponent;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class RequestPartitioningComponentImpl extends TaggedComponentBase 
    implements RequestPartitioningComponent
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private int partitionToUse;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RequestPartitioningComponentImpl)) {
            return false;
        }

        RequestPartitioningComponentImpl other = 
            (RequestPartitioningComponentImpl)obj ;

        return partitionToUse == other.partitionToUse ;
    }

    @Override
    public int hashCode()
    {
        return partitionToUse;
    }

    @Override
    public String toString()
    {
        return "RequestPartitioningComponentImpl[partitionToUse=" + partitionToUse + "]" ;
    }

    public RequestPartitioningComponentImpl()
    {
        partitionToUse = 0;
    }

    public RequestPartitioningComponentImpl(int thePartitionToUse) {
        if (thePartitionToUse < ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID ||
            thePartitionToUse > ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID) {
            throw wrapper.invalidRequestPartitioningComponentValue(
                    thePartitionToUse,
                    ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID,
                    ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID);
        }
        partitionToUse = thePartitionToUse;
    }

    public int getRequestPartitioningId()
    {
        return partitionToUse;
    }

    public void writeContents(OutputStream os) 
    {
        os.write_ulong(partitionToUse);
    }
    
    public int getId() 
    {
        return ORBConstants.TAG_REQUEST_PARTITIONING_ID;
    }
}
