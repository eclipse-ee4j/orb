/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
     */
    public void setORB(ORB orb);

    public ContactInfoList create(IOR ior);
}
