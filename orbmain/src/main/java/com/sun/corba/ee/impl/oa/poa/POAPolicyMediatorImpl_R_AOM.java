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
