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
 * The CORBA <code>WrongTransaction</code> user-defined exception.
 * This exception is thrown only by the methods
 * <code>Request.get_response</code>
 * and <code>ORB.get_next_response</code> when they are invoked
 * from a transaction scope that is different from the one in
 * which the client originally sent the request.
 * See the OMG Transaction Service Specification for details.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 */

// @SuppressWarnings({"serial"})
public final class WrongTransaction extends UserException {
    /**
     * Constructs a WrongTransaction object with an empty detail message.
     */
    public WrongTransaction() {
        super(WrongTransactionHelper.id());
    }

    /**
     * Constructs a WrongTransaction object with the given detail message.
     * @param reason The detail message explaining what caused this exception to be thrown.
     */
    public WrongTransaction(String reason) {
        super(WrongTransactionHelper.id() + "  " + reason);
    }
}
