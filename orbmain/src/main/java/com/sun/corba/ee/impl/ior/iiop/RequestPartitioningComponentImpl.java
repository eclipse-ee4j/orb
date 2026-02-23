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

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;
import com.sun.corba.ee.spi.ior.iiop.RequestPartitioningComponent;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.misc.ORBConstants;

import org.omg.CORBA_2_3.portable.OutputStream;

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
