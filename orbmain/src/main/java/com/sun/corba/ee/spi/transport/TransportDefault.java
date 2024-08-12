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

package com.sun.corba.ee.spi.transport;

import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.protocol.ClientDelegateFactory;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.ior.IOR;

// Internal imports, not used in the interface to this package
import com.sun.corba.ee.impl.protocol.ClientDelegateImpl;
import com.sun.corba.ee.impl.transport.AcceptorAcceptOnlyImpl;
import com.sun.corba.ee.impl.transport.ContactInfoListImpl;
import com.sun.corba.ee.impl.transport.AcceptorImpl;
import com.sun.corba.ee.impl.transport.AcceptorLazyImpl;
import java.net.Socket;
import org.glassfish.pfl.basic.func.UnaryVoidFunction;

/**
 * This class provices standard building blocks for the ORB, as do all Default classes in the various packages.
 */
public abstract class TransportDefault {
    private TransportDefault() {
    }

    public static ContactInfoListFactory makeCorbaContactInfoListFactory(final ORB broker) {
        return new ContactInfoListFactory() {
            public void setORB(ORB orb) {
            }

            public ContactInfoList create(IOR ior) {
                return new ContactInfoListImpl((com.sun.corba.ee.spi.orb.ORB) broker, ior);
            }
        };
    }

    public static ClientDelegateFactory makeClientDelegateFactory(final ORB broker) {
        return new ClientDelegateFactory() {
            public ClientDelegate create(ContactInfoList info) {
                return new ClientDelegateImpl((com.sun.corba.ee.spi.orb.ORB) broker, info);
            }
        };
    }

    public static IORTransformer makeIORTransformer(final ORB broker) {
        return null;
    }

    public static Acceptor makeStandardCorbaAcceptor(ORB orb, int port, String name, String type) {

        return new AcceptorImpl(orb, port, name, type);
    }

    public static Acceptor makeLazyCorbaAcceptor(ORB orb, int port, String name, String type) {

        return new AcceptorLazyImpl(orb, port, name, type);
    }

    public static Acceptor makeAcceptOnlyCorbaAcceptor(ORB orb, int port, String name, String type, UnaryVoidFunction<Socket> operation) {

        return new AcceptorAcceptOnlyImpl(orb, port, name, type, operation);
    }
}

// End of file.
