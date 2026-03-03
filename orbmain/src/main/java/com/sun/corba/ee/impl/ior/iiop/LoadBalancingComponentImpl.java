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
import com.sun.corba.ee.spi.ior.iiop.LoadBalancingComponent;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.misc.ORBConstants;

import org.omg.CORBA_2_3.portable.OutputStream;

public class LoadBalancingComponentImpl extends TaggedComponentBase 
    implements LoadBalancingComponent
{

    private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private int loadBalancingValue;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof LoadBalancingComponentImpl)) {
            return false;
        }

        LoadBalancingComponentImpl other = 
            (LoadBalancingComponentImpl)obj ;

        return loadBalancingValue == other.loadBalancingValue ;
    }

    @Override
    public int hashCode()
    {
        return loadBalancingValue;
    }

    @Override
    public String toString()
    {
        return "LoadBalancingComponentImpl[loadBalancingValue=" + loadBalancingValue + "]" ;
    }

    public LoadBalancingComponentImpl()
    {
        loadBalancingValue = 0;
    }

    public LoadBalancingComponentImpl(int theLoadBalancingValue) {
        if (theLoadBalancingValue < ORBConstants.FIRST_LOAD_BALANCING_VALUE ||
            theLoadBalancingValue > ORBConstants.LAST_LOAD_BALANCING_VALUE) {
            throw wrapper.invalidLoadBalancingComponentValue(
                  theLoadBalancingValue,
                  ORBConstants.FIRST_LOAD_BALANCING_VALUE,
                  ORBConstants.LAST_LOAD_BALANCING_VALUE );
        }
        loadBalancingValue = theLoadBalancingValue;
    }

    public int getLoadBalancingValue()
    {
        return loadBalancingValue;
    }

    public void writeContents(OutputStream os) 
    {
        os.write_ulong(loadBalancingValue);
    }
    
    public int getId() 
    {
        return ORBConstants.TAG_LOAD_BALANCING_ID;
    }
}

