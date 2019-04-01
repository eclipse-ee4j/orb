/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.poa;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.NoServant;

import com.sun.corba.ee.impl.oa.NullServantImpl;

/**
 * Implementation of POARequesHandler that provides policy specific operations on the POA in the case:
 * <ul>
 * <li>retain</li>
 * <li>useActiveObjectMapOnly</li>
 * </ul>
 */
public class POAPolicyMediatorImpl_R_AOM extends POAPolicyMediatorBase_R {
    POAPolicyMediatorImpl_R_AOM(Policies policies, POAImpl poa) {
        // assert policies.retainServants()
        super(policies, poa);

        // policies.useActiveObjectMapOnly()
        if (!policies.useActiveMapOnly()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }
    }

    protected java.lang.Object internalGetServant(byte[] id, String operation) throws ForwardRequest {
        poa.lock();
        try {
            java.lang.Object servant = internalIdToServant(id);
            if (servant == null) {
                servant = new NullServantImpl(wrapper.nullServant());
            }
            return servant;
        } finally {
            poa.unlock();
        }
    }

    public void etherealizeAll() {
        // NO-OP
    }

    public ServantManager getServantManager() throws WrongPolicy {
        throw new WrongPolicy();
    }

    public void setServantManager(ServantManager servantManager) throws WrongPolicy {
        throw new WrongPolicy();
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy {
        throw new WrongPolicy();
    }

    public void setDefaultServant(Servant servant) throws WrongPolicy {
        throw new WrongPolicy();
    }

    public Servant idToServant(byte[] id) throws WrongPolicy, ObjectNotActive {
        Servant s = internalIdToServant(id);

        if (s == null) {
            throw new ObjectNotActive();
        } else {
            return s;
        }
    }
}
