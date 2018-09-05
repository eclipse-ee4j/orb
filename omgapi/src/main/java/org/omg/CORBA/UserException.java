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
 * The root class for CORBA IDL-defined user exceptions.
 * All CORBA user exceptions are checked exceptions, which
 * means that they need to
 * be declared in method signatures.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version     1.28 09/09/97
 */
public abstract class UserException extends java.lang.Exception implements org.omg.CORBA.portable.IDLEntity {

    /**
     * Constructs a <code>UserException</code> object.
     * This method is called only by subclasses.
     */
    protected UserException() {
        super();
    }

    /**
     * Constructs a <code>UserException</code> object with a
     * detail message. This method is called only by subclasses.
     *
     * @param reason a <code>String</code> object giving the reason for this
     *         exception
     */
    protected UserException(String reason) {
        super(reason);
    }
}

