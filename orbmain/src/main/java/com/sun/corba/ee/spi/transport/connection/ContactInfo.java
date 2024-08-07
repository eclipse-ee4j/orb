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

package com.sun.corba.ee.spi.transport.connection;

import java.io.IOException;

/**
 * The ContactInfo represents the information needed to establish a connection to a (possibly different) process. This
 * is a subset of the PEPt 2.0 connection. Any implemetnation of this interface must define hashCode and equals properly
 * so that it may be used in a Map. It is also recommended that toString() be defined to return a useful summary of the
 * contact info (e.g. address information).
 */
public interface ContactInfo<C extends Connection> {
    /**
     * Create a new Connection from this ContactInfo. Throws an IOException if Connection creation fails.
     * 
     * @throws IOException if creation fails
     * @return a new {@link Connection}
     */
    C createConnection() throws IOException;
}
