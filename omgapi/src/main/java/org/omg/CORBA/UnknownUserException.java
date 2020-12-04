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

package org.omg.CORBA;

/**
 * A class that contains user exceptions returned by the server.
 * When the client uses the DII to make an invocation, any user exception
 * returned from the server is enclosed in an <code>Any</code> object contained in the
 * <code>UnknownUserException</code> object. This is available from the
 * <code>Environment</code> object returned by the method <code>Request.env</code>.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @see Request
 */

public final class UnknownUserException extends UserException {

    /** The <code>Any</code> instance that contains the actual user exception thrown
     *  by the server.
     * @serial
     */
    public Any except;

    /**
     * Constructs an <code>UnknownUserException</code> object.
     */
    public UnknownUserException() {
        super();
    }

    /**
     * Constructs an <code>UnknownUserException</code> object that contains the given
     * <code>Any</code> object.
     *
     * @param a an <code>Any</code> object that contains a user exception returned
     *         by the server
     */
    public UnknownUserException(Any a) {
        super();
        except = a;
    }
}

