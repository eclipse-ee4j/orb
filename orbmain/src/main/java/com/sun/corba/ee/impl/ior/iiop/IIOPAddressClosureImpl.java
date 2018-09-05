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

import org.glassfish.pfl.basic.func.NullaryFunction;

/**
 * @author 
 */
public final class IIOPAddressClosureImpl extends IIOPAddressBase
{
    private NullaryFunction<String> host;
    private NullaryFunction<Integer> port;
    
    public IIOPAddressClosureImpl( NullaryFunction<String> host,
        NullaryFunction<Integer> port ) {
        this.host = host ;
        this.port = port ;
    }

    public String getHost()
    {
        return host.evaluate() ;
    }

    public int getPort()
    {
        return port.evaluate() ;
    }
}
