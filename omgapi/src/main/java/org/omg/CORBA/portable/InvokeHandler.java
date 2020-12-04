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

package org.omg.CORBA.portable;

import org.omg.CORBA.SystemException;

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
        throws SystemException;
}

