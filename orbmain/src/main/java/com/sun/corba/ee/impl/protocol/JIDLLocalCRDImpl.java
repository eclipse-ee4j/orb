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

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.portable.ServantObject;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.IOR;

public class JIDLLocalCRDImpl extends LocalClientRequestDispatcherBase {
    public JIDLLocalCRDImpl(ORB orb, int scid, IOR ior) {
        super((com.sun.corba.ee.spi.orb.ORB) orb, scid, ior);
    }

    protected ServantObject servant;

    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) {
        if (!checkForCompatibleServant(servant, expectedType))
            return null;

        return servant;
    }

    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant) {
        // NO-OP
    }

    // REVISIT - This is called from TOAImpl.
    public void setServant(java.lang.Object servant) {
        if (servant != null && servant instanceof Tie) {
            this.servant = new ServantObject();
            this.servant.servant = ((Tie) servant).getTarget();
        } else {
            this.servant = null;
        }
    }

    public void unexport() {
        // DO NOT set the IOR to null. (Un)exporting is only concerns
        // the servant not the IOR. If the ior is set to null then
        // null pointer exceptions happen during a colocated invocation.
        // It is better to let the invocation proceed and get OBJECT_NOT_EXIST
        // from the server side.
        // ior = null;
        servant = null;
    }
}

// End of file.
