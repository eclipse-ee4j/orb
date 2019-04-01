/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi;

import java.rmi.RemoteException;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_INV_ORDER;

import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.Delegate;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import com.sun.corba.ee.impl.util.Utility;

import com.sun.corba.ee.impl.ior.StubIORImpl;

import com.sun.corba.ee.spi.logging.UtilSystemException;

import com.sun.corba.ee.impl.corba.CORBAObjectImpl;

public abstract class StubConnectImpl {
    private static UtilSystemException wrapper = UtilSystemException.self;

    /**
     * Connect the stub to the orb if necessary.
     *
     * @param ior The StubIORImpl for this stub (may be null)
     * @param proxy The externally visible stub seen by the user (may be the same as stub)
     * @param stub The stub implementation that extends ObjectImpl
     * @param orb The ORB to which we connect the stub.
     */
    public static StubIORImpl connect(StubIORImpl ior, org.omg.CORBA.Object proxy, org.omg.CORBA.portable.ObjectImpl stub, ORB orb) throws RemoteException {
        Delegate del = null;

        try {
            try {
                del = StubAdapter.getDelegate(stub);

                if (del.orb(stub) != orb)
                    throw wrapper.connectWrongOrb();
            } catch (org.omg.CORBA.BAD_OPERATION err) {
                if (ior == null) {
                    // No IOR, can we get a Tie for this stub?
                    Tie tie = (javax.rmi.CORBA.Tie) Utility.getAndForgetTie(proxy);
                    if (tie == null)
                        throw wrapper.connectNoTie();

                    // Is the tie already connected? If it is, check that it's
                    // connected to the same ORB, otherwise connect it.
                    ORB existingOrb = orb;
                    try {
                        existingOrb = tie.orb();
                    } catch (BAD_OPERATION exc) {
                        // Thrown when tie is an ObjectImpl and its delegate is not set.
                        tie.orb(orb);
                    } catch (BAD_INV_ORDER exc) {
                        // Thrown when tie is a Servant and its delegate is not set.
                        tie.orb(orb);
                    }

                    if (existingOrb != orb)
                        throw wrapper.connectTieWrongOrb();

                    // Get the delegate for the stub from the tie.
                    del = StubAdapter.getDelegate(tie);
                    ObjectImpl objref = new CORBAObjectImpl();
                    objref._set_delegate(del);
                    ior = new StubIORImpl(objref);
                } else {
                    // ior is initialized, so convert ior to an object, extract
                    // the delegate, and set it on ourself
                    del = ior.getDelegate(orb);
                }

                StubAdapter.setDelegate(stub, del);
            }
        } catch (SystemException exc) {
            throw new RemoteException("CORBA SystemException", exc);
        }

        return ior;
    }
}
