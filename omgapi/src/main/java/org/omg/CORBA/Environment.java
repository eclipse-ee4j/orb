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
 * A container (holder) for an exception that is used in <code>Request</code> operations to make exceptions available to
 * the client. An <code>Environment</code> object is created with the <code>ORB</code> method
 * <code>create_environment</code>.
 *
 * @version 1.11, 09/09/97
 * @since JDK1.2
 */

public abstract class Environment {

    /**
     * Retrieves the exception in this <code>Environment</code> object.
     *
     * @return the exception in this <code>Environment</code> object
     */

    public abstract java.lang.Exception exception();

    /**
     * Inserts the given exception into this <code>Environment</code> object.
     *
     * @param except the exception to be set
     */

    public abstract void exception(java.lang.Exception except);

    /**
     * Clears this <code>Environment</code> object of its exception.
     */

    public abstract void clear();

}
