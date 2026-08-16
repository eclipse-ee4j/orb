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

package com.sun.corba.ee.spi.ior;

import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;

/**
 * The full object key, which is contained in an IIOPProfile. The object identifier corresponds to the information
 * passed into POA::create_reference_with_id and POA::create_reference (in the POA case). The template represents the
 * information that is object adapter specific and shared across multiple ObjectKey instances.
 */
public interface ObjectKey extends Writeable {
    /**
     * Return the object identifier for this Object key.
     * 
     * @return the object identifier
     */
    ObjectId getId();

    /**
     * Return the template for this object key.
     * 
     * @return the template
     */
    ObjectKeyTemplate getTemplate();

    byte[] getBytes(org.omg.CORBA.ORB orb);

    ServerRequestDispatcher getServerRequestDispatcher();
}
