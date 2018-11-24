/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.legacy.connection;

import java.util.Iterator;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.impl.transport.ContactInfoListImpl;
import com.sun.corba.ee.impl.transport.ContactInfoListIteratorImpl;

/**
 * @author Harold Carr
 */
public class SocketFactoryContactInfoListImpl extends ContactInfoListImpl {
    // XREVISIT - is this used?
    public SocketFactoryContactInfoListImpl(ORB orb) {
        super(orb);
    }

    public SocketFactoryContactInfoListImpl(ORB orb, IOR targetIOR) {
        super(orb, targetIOR);
    }

    public Iterator iterator() {
        return new SocketFactoryContactInfoListIteratorImpl(orb, this);
    }
}

// End of file.
