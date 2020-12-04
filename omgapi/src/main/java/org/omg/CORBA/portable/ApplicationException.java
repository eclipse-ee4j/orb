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
This class is used for reporting application level exceptions between ORBs and stubs.
*/

public class ApplicationException extends Exception {
    /**
     * Constructs an ApplicationException from the CORBA repository ID of the exception
     * and an input stream from which the exception data can be read as its parameters.
     * @param id the repository id of the user exception
     * @param ins the stream which contains the user exception data
     */
    public ApplicationException(String id,
                                InputStream ins) {
        this.id = id;
        this.ins = ins;
    }

    /**
     * Returns the CORBA repository ID of the exception
     * without removing it from the exceptions input stream.
     * @return The CORBA repository ID of this exception
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the input stream from which the exception data can be read as its parameters.
     * @return The stream which contains the user exception data
     */
    public InputStream getInputStream() {
        return ins;
    }

    private String id;
    private InputStream ins;
}
