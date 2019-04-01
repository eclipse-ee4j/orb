/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/**
 * INSServerRequestDispatcher handles all INS related discovery request. The INS Service can be registered using
 * ORB.register_initial_reference(). This Singleton subcontract just finds the target IOR and does location forward. XXX
 * PI points are not invoked in either dispatch() or locate() method this should be fixed in Tiger.
 */
public class INSServerRequestDispatcher implements ServerRequestDispatcher {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private ORB orb = null;

    public INSServerRequestDispatcher(ORB orb) {
        this.orb = orb;
    }

    // Need to signal one of OBJECT_HERE, OBJECT_FORWARD, OBJECT_NOT_EXIST.
    public IOR locate(ObjectKey okey) {
        // send a locate forward with the right IOR. If the insKey is not
        // registered then it will throw OBJECT_NOT_EXIST Exception
        String insKey = new String(okey.getBytes(orb));
        return getINSReference(insKey);
    }

    public void dispatch(MessageMediator request) {
        // send a locate forward with the right IOR. If the insKey is not
        // registered then it will throw OBJECT_NOT_EXIST Exception
        String insKey = new String(request.getObjectKeyCacheEntry().getObjectKey().getBytes(orb));
        request.getProtocolHandler().createLocationForward(request, getINSReference(insKey), null);
        return;
    }

    /**
     * getINSReference if it is registered in INSObjectKeyMap.
     */
    private IOR getINSReference(String insKey) {
        IOR entry = orb.getIOR(orb.getLocalResolver().resolve(insKey), false);
        if (entry != null) {
            // If entry is not null then the locate is with an INS Object key,
            // so send a location forward with the right IOR.
            return entry;
        }

        throw wrapper.servantNotFound();
    }
}
