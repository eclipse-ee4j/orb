/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.portable;

/**
 * This interface is supplied by an ORB to a servant at invocation time and allows the servant to later retrieve an
 * OutputStream for returning the invocation results.
 */

public interface ResponseHandler {
    /**
     * Called by the servant during a method invocation. The servant should call this method to create a reply marshal
     * buffer if no exception occurred.
     *
     * @return an OutputStream suitable for marshalling the reply.
     *
     * @see <a href="package-summary.html#unimpl"><code>portable</code> package comments for unimplemented features</a>
     */
    OutputStream createReply();

    /**
     * Called by the servant during a method invocation. The servant should call this method to create a reply marshal
     * buffer if a user exception occurred.
     *
     * @return an OutputStream suitable for marshalling the exception ID and the user exception body.
     */
    OutputStream createExceptionReply();
}
