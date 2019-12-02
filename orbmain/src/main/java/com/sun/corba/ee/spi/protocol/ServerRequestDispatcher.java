/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;


import com.sun.corba.ee.spi.ior.ObjectKey;

import com.sun.corba.ee.spi.ior.IOR ;

/**
 * Server delegate adds behavior on the server-side -- specifically
 * on the dispatch path. A single server delegate instance serves
 * many server objects.  This is the second level of the dispatch 
 * on the server side: Acceptor to ServerSubcontract to ServerRequestDispatcher to
 * ObjectAdapter to Servant, although this may be short-circuited.
 * Instances of this class are registered in the subcontract Registry.
 */
public abstract interface ServerRequestDispatcher
{
    /**
     * Handle a locate request.
     * @param key key to object to locate
     * @return IOR for the request
     */
    public IOR locate(ObjectKey key);

    public void dispatch(MessageMediator messageMediator);
}

// End of file.

