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

package com.sun.corba.ee.impl.plugin.hwlb;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.omg.CORBA.LocalObject;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;

import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.OutboundConnectionCache;

import com.sun.corba.ee.impl.encoding.CDRInputObject;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.orb.DataCollector;

import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;

import com.sun.corba.ee.spi.ior.IOR;

import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.impl.transport.ConnectionCacheBase;
import com.sun.corba.ee.impl.transport.ConnectionImpl;

// The following 3 implementation classes are needed as base 
// classes.  This needs some architectural changes, perhaps
// adding a codegen-based proxy layer for dynamic inheritance.
import com.sun.corba.ee.impl.protocol.ClientRequestDispatcherImpl;

import com.sun.corba.ee.impl.transport.ContactInfoImpl;
import com.sun.corba.ee.impl.transport.ContactInfoListImpl;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * Install this in an ORB using the property ORBConstants.USER_CONFIGURATOR_PREFIX + "corba.lb.NoConnectionCacheImpl" =
 * "dummy"
 */
@Transport
public class NoConnectionCacheImpl extends LocalObject implements ORBConfigurator {
    @Transport
    private static class NCCConnectionCacheImpl extends ConnectionCacheBase implements OutboundConnectionCache {
        // store is a dummy variable
        private Map store = new HashMap();

        // holds only one connection
        private Connection connection = null;

        public NCCConnectionCacheImpl(ORB orb) {
            super(orb, "Dummy", "Dummy");
        }

        public Collection values() {
            return store.values();
        }

        protected Object backingStore() {
            return store;
        }

        public Connection get(ContactInfo contactInfo) {
            return connection;
        }

        @Transport
        public void put(ContactInfo contactInfo, Connection conn) {
            remove(contactInfo);
            connection = conn;
        }

        @InfoMethod
        private void removeConnectionInfo(Connection conn) {
        }

        @InfoMethod
        private void connectionIsNull() {
        }

        @Transport
        public void remove(ContactInfo contactInfo) {
            if (connection != null) {
                removeConnectionInfo(connection);
                connection.close();
                connection = null;
            } else {
                connectionIsNull();
            }
        }
    }

    private static ThreadLocal connectionCache = new ThreadLocal();

    private static NCCConnectionCacheImpl getConnectionCache(ORB orb) {
        NCCConnectionCacheImpl result = (NCCConnectionCacheImpl) connectionCache.get();
        if (result == null) {
            result = new NCCConnectionCacheImpl(orb);
            connectionCache.set(result);
        }

        return result;
    }

    @Transport
    private static class NCCConnectionImpl extends ConnectionImpl {
        private static int count = 0;
        private int myCount;

        @Transport
        private void constructedNCCConnectionImpl(String str) {
        }

        public NCCConnectionImpl(ORB orb, ContactInfo contactInfo, String socketType, String hostname, int port) {

            super(orb, contactInfo, socketType, hostname, port);
            myCount = count++;
            constructedNCCConnectionImpl(toString());
        }

        @Override
        public String toString() {
            return "NCCConnectionImpl(" + myCount + ")[" + super.toString() + "]";
        }

        @Transport
        @Override
        public synchronized void close() {
            super.closeConnectionResources();
        }
    }

    @Transport
    private static class NCCContactInfoImpl extends ContactInfoImpl {
        public NCCContactInfoImpl(ORB orb, ContactInfoList contactInfoList, IOR effectiveTargetIOR, short addressingDisposition,
                String socketType, String hostname, int port) {

            super(orb, contactInfoList, effectiveTargetIOR, addressingDisposition, socketType, hostname, port);
        }

        @Transport
        @Override
        public boolean shouldCacheConnection() {
            return false;
        }

        @InfoMethod
        private void createdConnection(Connection conn) {
        }

        @Transport
        @Override
        public Connection createConnection() {
            Connection connection = new NCCConnectionImpl(orb, this, socketType, hostname, port);
            createdConnection(connection);
            NCCConnectionCacheImpl cc = NoConnectionCacheImpl.getConnectionCache(orb);
            cc.put(this, connection);
            connection.setConnectionCache(cc);

            return connection;
        }
    }

    public static class NCCContactInfoListImpl extends ContactInfoListImpl {
        public NCCContactInfoListImpl(ORB orb, IOR ior) {
            super(orb, ior);
        }

        @Override
        public ContactInfo createContactInfo(String type, String hostname, int port) {

            return new NCCContactInfoImpl(orb, this, effectiveTargetIOR, orb.getORBData().getGIOPAddressDisposition(), type, hostname,
                    port);
        }
    }

    private static class NCCClientRequestDispatcherImpl extends ClientRequestDispatcherImpl {
        @Override
        public void endRequest(ORB broker, Object self, CDRInputObject inputObject) {
            super.endRequest(broker, self, inputObject);
            getConnectionCache(broker).remove(null);
        }
    }

    public void configure(DataCollector dc, final ORB orb) {
        ContactInfoListFactory factory = new ContactInfoListFactory() {
            public void setORB(ORB orb) {
            }

            public ContactInfoList create(IOR ior) {
                return new NCCContactInfoListImpl(orb, ior);
            }
        };

        // Disable a read optimization that forces a blocking read on all
        // OP_READ events. It makes little sense to enable this optimization
        // when using a NCCConnectionCache because clients will close a
        // Connection when a response to a request has been received. Hence,
        // if the client knows when it receives a response to a request and
        // then closes the Connection, there's no reason to enable an
        // optimization that will force a blocking read which will wait for
        // more data to arrive after the client's response has been received.
        orb.getORBData().alwaysEnterBlockingRead(false);

        orb.setCorbaContactInfoListFactory(factory);

        ClientRequestDispatcher crd = new NCCClientRequestDispatcherImpl();
        RequestDispatcherRegistry rdr = orb.getRequestDispatcherRegistry();
        // Need to register crd with all scids. Assume range is 0 to MAX_POA_SCID.
        for (int ctr = 0; ctr < ORBConstants.MAX_POA_SCID; ctr++) {
            ClientRequestDispatcher disp = rdr.getClientRequestDispatcher(ctr);
            if (disp != null) {
                rdr.registerClientRequestDispatcher(crd, ctr);
            }
        }
    }
}
