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

package com.sun.corba.ee.spi.presentation.rmi;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.OutputStream;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.State;
import org.omg.PortableServer.Servant;

import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import com.sun.corba.ee.impl.oa.poa.POAManagerImpl;

/**
 * Provide access to stub delegate and type id information independent of the stub type. This class exists because
 * ObjectImpl does not have an interface for the 3 delegate and type id methods, so a DynamicStub has a different type.
 * We cannot simply change ObjectImpl as it is a standard API. We also cannot change the code generation of Stubs, as
 * that is also standard. Hence I am left with this ugly class.
 */
public abstract class StubAdapter {
    private StubAdapter() {
    }

    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    public static boolean isStubClass(Class cls) {
        return (ObjectImpl.class.isAssignableFrom(cls)) || (DynamicStub.class.isAssignableFrom(cls));
    }

    public static boolean isStub(Object stub) {
        return (stub instanceof DynamicStub) || (stub instanceof ObjectImpl);
    }

    public static void setDelegate(Object stub, Delegate delegate) {
        if (stub instanceof DynamicStub) {
            ((DynamicStub) stub).setDelegate(delegate);
        } else if (stub instanceof ObjectImpl) {
            ((ObjectImpl) stub)._set_delegate(delegate);
        } else {
            throw wrapper.setDelegateRequiresStub();
        }
    }

    /**
     * Use implicit activation to get an object reference for the servant.
     * 
     * @param servant servant to activate
     * @return reference to servant
     */
    public static org.omg.CORBA.Object activateServant(Servant servant) {
        POA poa = servant._default_POA();
        org.omg.CORBA.Object ref = null;

        try {
            ref = poa.servant_to_reference(servant);
        } catch (ServantNotActive sna) {
            throw wrapper.getDelegateServantNotActive(sna);
        } catch (WrongPolicy wp) {
            throw wrapper.getDelegateWrongPolicy(wp);
        }

        // Make sure that the POAManager is activated if no other
        // POAManager state management has taken place.
        POAManager mgr = poa.the_POAManager();
        if (mgr instanceof POAManagerImpl) {
            // This servant is managed by one of our POAs,
            // so only activate it if there has not been
            // an explicit state change, that is, if the POA
            // has never changed state from the initial
            // HOLDING state.
            POAManagerImpl mgrImpl = (POAManagerImpl) mgr;
            mgrImpl.implicitActivation();
        } else {
            // This servant is not managed by one of our POAs,
            // so activate it if the state is HOLDING, which is the
            // initial state. Note that this may NOT be exactly
            // what the user intended!
            if (mgr.get_state().value() == State._HOLDING) {
                try {
                    mgr.activate();
                } catch (AdapterInactive ai) {
                    throw wrapper.adapterInactiveInActivateServant(ai);
                }
            }
        }

        return ref;
    }

    /**
     * Given any Tie, return the corresponding object refernce, activating the Servant if necessary.
     * 
     * @param tie tie to activate
     * @return reference to Tie
     */
    public static org.omg.CORBA.Object activateTie(Tie tie) {
        /**
         * Any implementation of Tie should be either a Servant or an ObjectImpl, depending on which style of code generation is
         * used. rmic -iiop by default results in an ObjectImpl-based Tie, while rmic -iiop -poa results in a Servant-based Tie.
         * Dynamic RMI-IIOP also uses Servant-based Ties (see impl.presentation.rmi.ReflectiveTie).
         */
        if (tie instanceof ObjectImpl) {
            return tie.thisObject();
        } else if (tie instanceof Servant) {
            Servant servant = (Servant) tie;
            return activateServant(servant);
        } else {
            throw wrapper.badActivateTieCall();
        }
    }

    /**
     * This also gets the delegate from a Servant by using Servant._this_object()
     * 
     * @param stub stub to get delegate of
     * @return the stub's Delegate
     */
    public static Delegate getDelegate(Object stub) {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub) stub).getDelegate();
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._get_delegate();
        } else if (stub instanceof Tie) {
            Tie tie = (Tie) stub;
            org.omg.CORBA.Object ref = activateTie(tie);
            return getDelegate(ref);
        } else {
            throw wrapper.getDelegateRequiresStub();
        }
    }

    public static ORB getORB(Object stub) {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub) stub).getORB();
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._orb();
        } else {
            throw wrapper.getOrbRequiresStub();
        }
    }

    public static String[] getTypeIds(Object stub) {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub) stub).getTypeIds();
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._ids();
        } else {
            throw wrapper.getTypeIdsRequiresStub();
        }
    }

    public static void connect(Object stub, ORB orb) throws java.rmi.RemoteException {
        if (stub instanceof DynamicStub) {
            ((DynamicStub) stub).connect((com.sun.corba.ee.spi.orb.ORB) orb);
        } else if (stub instanceof javax.rmi.CORBA.Stub) {
            ((javax.rmi.CORBA.Stub) stub).connect(orb);
        } else if (stub instanceof ObjectImpl) {
            orb.connect((org.omg.CORBA.Object) stub);
        } else {
            throw wrapper.connectRequiresStub();
        }
    }

    public static boolean isLocal(Object stub) {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub) stub).isLocal();
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._is_local();
        } else {
            throw wrapper.isLocalRequiresStub();
        }
    }

    public static OutputStream request(Object stub, String operation, boolean responseExpected) {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub) stub).request(operation, responseExpected);
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._request(operation, responseExpected);
        } else {
            throw wrapper.requestRequiresStub();
        }
    }
}
