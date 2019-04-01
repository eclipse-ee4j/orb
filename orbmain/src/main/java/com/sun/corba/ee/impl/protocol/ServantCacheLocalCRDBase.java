/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.protocol.ForwardException;

import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.oa.OADestroyed;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.trace.Subcontract;

@Subcontract
public abstract class ServantCacheLocalCRDBase extends LocalClientRequestDispatcherBase {

    private OAInvocationInfo cachedInfo;

    protected ServantCacheLocalCRDBase(ORB orb, int scid, IOR ior) {
        super(orb, scid, ior);
    }

    @Subcontract
    protected void cleanupAfterOADestroyed() {
        cachedInfo = null;
    }

    @Subcontract
    protected synchronized OAInvocationInfo getCachedInfo() throws OADestroyed {
        if (!servantIsLocal) {
            throw poaWrapper.servantMustBeLocal();
        }

        if (cachedInfo == null) {
            updateCachedInfo();
        }

        return cachedInfo;
    }

    @Subcontract
    private void updateCachedInfo() throws OADestroyed {
        // If find throws an exception, just let it propagate out
        ObjectAdapter oa = oaf.find(oaid);
        cachedInfo = oa.makeInvocationInfo(objectId);
        oa.enter();

        // InvocationInfo must be pushed before calling getInvocationServant
        orb.pushInvocationInfo(cachedInfo);

        try {
            oa.getInvocationServant(cachedInfo);
        } catch (ForwardException freq) {
            throw poaWrapper.illegalForwardRequest(freq);
        } finally {
            oa.returnServant();
            oa.exit();
            orb.popInvocationInfo();
        }

        return;
    }
}

// End of File
