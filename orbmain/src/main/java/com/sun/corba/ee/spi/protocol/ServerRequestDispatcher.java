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

