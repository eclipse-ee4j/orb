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

import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.oa.OADestroyed;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.trace.Subcontract;

@Subcontract
public class POALocalCRDImpl extends LocalClientRequestDispatcherBase {

    public POALocalCRDImpl(ORB orb, int scid, IOR ior) {
        super(orb, scid, ior);
    }

    @Subcontract
    private OAInvocationInfo servantEnter(ObjectAdapter oa) throws OADestroyed {
        oa.enter();

        OAInvocationInfo info = oa.makeInvocationInfo(objectId);
        orb.pushInvocationInfo(info);

        return info;
    }

    @Subcontract
    private void servantExit(ObjectAdapter oa) {
        try {
            oa.returnServant();
        } finally {
            oa.exit();
            orb.popInvocationInfo();
        }
    }

    // Look up the servant for this request and return it in a
    // ServantObject. Note that servant_postinvoke is always called
    // by the stub UNLESS this method returns null. However, in all
    // cases we must be sure that ObjectAdapter.getServant and
    // ObjectAdapter.returnServant calls are paired, as required for
    // Portable Interceptors and Servant Locators in the POA.
    // Thus, this method must call returnServant if it returns null.
    @Subcontract
    @Override
    public ServantObject internalPreinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) throws OADestroyed {

        ObjectAdapter oa = null;

        oa = oaf.find(oaid);

        OAInvocationInfo info = servantEnter(oa);
        info.setOperation(operation);

        try {
            oa.getInvocationServant(info);
            if (!checkForCompatibleServant(info, expectedType)) {
                servantExit(oa);
                return null;
            }

            return info;
        } catch (Error err) {
            // Cleanup after this call, then throw to allow
            // outer try to handle the exception appropriately.
            servantExit(oa);
            throw err;
        } catch (RuntimeException re) {
            // Cleanup after this call, then throw to allow
            // outer try to handle the exception appropriately.
            servantExit(oa);
            throw re;
        }
    }

    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servantobj) {
        ObjectAdapter oa = orb.peekInvocationInfo().oa();
        servantExit(oa);
    }
}

// End of file.
