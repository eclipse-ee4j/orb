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
 * A container (holder) for an exception that is used in <code>Request</code>
 * operations to make exceptions available to the client.  An
 * <code>Environment</code> object is created with the <code>ORB</code>
 * method <code>create_environment</code>.
 *
 * @version 1.11, 09/09/97
 * @since   JDK1.2
 */

public abstract class Environment {

    /**
     * Retrieves the exception in this <code>Environment</code> object.
     *
     * @return                  the exception in this <code>Environment</code> object
     */

    public abstract java.lang.Exception exception();

    /**
     * Inserts the given exception into this <code>Environment</code> object.
     *
     * @param except            the exception to be set
     */

    public abstract void exception(java.lang.Exception except);

    /**
     * Clears this <code>Environment</code> object of its exception.
     */

    public abstract void clear();

}
