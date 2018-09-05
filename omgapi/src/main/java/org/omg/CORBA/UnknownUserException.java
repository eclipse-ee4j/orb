/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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

