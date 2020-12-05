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

/**
This interface is supplied by an ORB to a servant at invocation time and allows
the servant to later retrieve an OutputStream for returning the invocation results.
*/

public interface ResponseHandler {
    /**
     * Called by the servant during a method invocation. The servant
     * should call this method to create a reply marshal buffer if no
     * exception occurred.
     *
     * @return an OutputStream suitable for marshalling the reply.
     *
     * @see <a href="package-summary.html#unimpl"><code>portable</code>
     * package comments for unimplemented features</a>
     */
    OutputStream createReply();

    /**
     * Called by the servant during a method invocation. The servant
     * should call this method to create a reply marshal buffer if a
     * user exception occurred.
     *
     * @return an OutputStream suitable for marshalling the exception
     * ID and the user exception body.
     */
    OutputStream createExceptionReply();
}
