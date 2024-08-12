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

package com.sun.corba.ee.spi.transport;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.ORB;

/**
 * Interface used to create a ContactInfoList from an IOR, as required for supporting CORBA semantics using the DCS
 * framework. This is a natural correspondence since an IOR contains the information for contacting one or more
 * communication endpoints that can be used to invoke a method on an object, along with the necessary information on
 * particular transports, encodings, and protocols to use. Note that the actual implementation may support more than one
 * IOR in the case of GIOP with Location Forward messages.
 */
public interface ContactInfoListFactory {
    /**
     * This will be called after the no-arg constructor before create is called.
     * 
     * @param orb ORB to use in factory
     */
    public void setORB(ORB orb);

    public ContactInfoList create(IOR ior);
}
