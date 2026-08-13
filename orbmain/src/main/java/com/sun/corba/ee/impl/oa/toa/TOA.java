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

package com.sun.corba.ee.impl.oa.toa;

import com.sun.corba.ee.spi.oa.ObjectAdapter;

/**
 * The Transient Object Adapter is used for standard RMI-IIOP and Java-IDL (legacy JDK 1.2) object implementations. Its
 * protocol for managing objects is very simple: just connect and disconnect. There is only a single TOA instance per
 * ORB, and its lifetime is the same as the ORB. The TOA instance is always ready to receive messages except when the
 * ORB is shutting down.
 */
public interface TOA extends ObjectAdapter {
    /**
     * Connect the given servant to the ORB by allocating a transient object key and creating an IOR and object reference
     * using the current factory.
     * 
     * @param servant servant to connect to the ORB
     */
    void connect(org.omg.CORBA.Object servant);

    /**
     * Disconnect the object from this ORB.
     * 
     * @param obj ORB to disconnect from
     */
    void disconnect(org.omg.CORBA.Object obj);
}
