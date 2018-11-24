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

import org.omg.CORBA.portable.ServantObject;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.oa.OADestroyed;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Subcontract
public class FullServantCacheLocalCRDImpl extends ServantCacheLocalCRDBase {
    public FullServantCacheLocalCRDImpl(ORB orb, int scid, IOR ior) {
        super(orb, scid, ior);
    }

    @Subcontract
    @Override
    public ServantObject internalPreinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) throws OADestroyed {

        OAInvocationInfo cachedInfo = getCachedInfo();
        if (!checkForCompatibleServant(cachedInfo, expectedType)) {
            return null;
        }

        // Note that info is shared across multiple threads
        // using the same subcontract, each of which may
        // have its own operation. Therefore we need to clone it.
        OAInvocationInfo newInfo = new OAInvocationInfo(cachedInfo, operation);
        newInfo.oa().enter();
        orb.pushInvocationInfo(newInfo);
        return newInfo;
    }

    @Subcontract
    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servantobj) {
        try {
            OAInvocationInfo cachedInfo = getCachedInfo();
            cachedInfo.oa().exit();
        } catch (OADestroyed oades) {
            caughtOADestroyed();
            // ignore this: if I can't get the OA, I don't
            // need to call exit on it.
        } finally {
            orb.popInvocationInfo();
        }
    }

    @InfoMethod
    private void caughtOADestroyed() {
    }
}
