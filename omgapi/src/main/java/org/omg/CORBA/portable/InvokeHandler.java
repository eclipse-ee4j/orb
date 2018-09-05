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
This interface provides a dispatching mechanism for an incoming call.
It is invoked by the ORB to dispatch a request to a servant.
*/

public interface InvokeHandler {
    /**
     * Invoked by the ORB to dispatch a request to the servant.
     *
     * ORB passes the method name, an InputStream containing the
     * marshalled arguments, and a ResponseHandler which the servant
     * uses to construct a proper reply.
     *
     * Only CORBA SystemException may be thrown by this method.
     *
     * The method must return an OutputStream created by the
     * ResponseHandler which contains the marshalled reply.
     *
     * A servant must not retain a reference to the ResponseHandler
     * beyond the lifetime of a method invocation.
     *
     * Servant behaviour is defined as follows:
     * <p>1. Determine correct method, and unmarshal parameters from
     *    InputStream.
     * <p>2. Invoke method implementation.
     * <p>3. If no user exception, create a normal reply using
     *    ResponseHandler.
     * <p>4. If user exception occurred, create exception reply using
     *    ResponseHandler.
     * <p>5. Marshal reply into OutputStream returned by
     *    ResponseHandler.
     * <p>6. Return OutputStream to ORB.
     * <p>
     * @param method The method name.
     * @param input The <code>InputStream</code> containing the marshalled arguments.
     * @param handler The <code>ResponseHandler</code> which the servant uses
     * to construct a proper reply
     * @return The <code>OutputStream</code> created by the
     * ResponseHandler which contains the marshalled reply
     * @throws SystemException is thrown when invocation fails due to a CORBA system exception.
     */

    OutputStream _invoke(String method, InputStream input,
                         ResponseHandler handler)
        throws org.omg.CORBA.SystemException;
}

