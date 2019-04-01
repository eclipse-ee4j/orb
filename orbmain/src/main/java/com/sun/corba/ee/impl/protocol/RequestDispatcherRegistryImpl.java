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

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;

import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcherFactory;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;

import com.sun.corba.ee.spi.oa.ObjectAdapterFactory;
import org.glassfish.pfl.basic.contain.DenseIntMapImpl;

/**
 * This is a registry of all subcontract ID dependent objects. This includes: LocalClientRequestDispatcherFactory,
 * ClientRequestDispatcher, ServerSubcontract, and ObjectAdapterFactory.
 */
public class RequestDispatcherRegistryImpl implements RequestDispatcherRegistry {
    protected int defaultId; // The default subcontract ID to use if there is no more specific ID available.
                             // This happens when invoking a foreign IOR.

    private DenseIntMapImpl<ServerRequestDispatcher> SDRegistry;
    private DenseIntMapImpl<ClientRequestDispatcher> CSRegistry;
    private DenseIntMapImpl<ObjectAdapterFactory> OAFRegistry;
    private DenseIntMapImpl<LocalClientRequestDispatcherFactory> LCSFRegistry;
    private Set<ObjectAdapterFactory> objectAdapterFactories;
    private Set<ObjectAdapterFactory> objectAdapterFactoriesView; // Read-only view of oaf instances
    private Map<String, ServerRequestDispatcher> stringToServerSubcontract;

    public RequestDispatcherRegistryImpl(int defaultId) {
        this.defaultId = defaultId;
        SDRegistry = new DenseIntMapImpl<ServerRequestDispatcher>();
        CSRegistry = new DenseIntMapImpl<ClientRequestDispatcher>();
        OAFRegistry = new DenseIntMapImpl<ObjectAdapterFactory>();
        LCSFRegistry = new DenseIntMapImpl<LocalClientRequestDispatcherFactory>();
        objectAdapterFactories = new HashSet<ObjectAdapterFactory>();
        objectAdapterFactoriesView = Collections.unmodifiableSet(objectAdapterFactories);
        stringToServerSubcontract = new HashMap<String, ServerRequestDispatcher>();
    }

    public synchronized void registerClientRequestDispatcher(ClientRequestDispatcher csc, int scid) {
        CSRegistry.set(scid, csc);
    }

    public synchronized void registerLocalClientRequestDispatcherFactory(LocalClientRequestDispatcherFactory csc, int scid) {
        LCSFRegistry.set(scid, csc);
    }

    public synchronized void registerServerRequestDispatcher(ServerRequestDispatcher ssc, int scid) {
        SDRegistry.set(scid, ssc);
    }

    public synchronized void registerServerRequestDispatcher(ServerRequestDispatcher scc, String name) {
        stringToServerSubcontract.put(name, scc);
    }

    public synchronized void registerObjectAdapterFactory(ObjectAdapterFactory oaf, int scid) {
        objectAdapterFactories.add(oaf);
        OAFRegistry.set(scid, oaf);
    }

    // **************************************************
    // Methods to find the subcontract side subcontract
    // **************************************************

    // Note that both forms of getServerRequestDispatcher need to return
    // the default server delegate if no other match is found.
    // This is essential to proper handling of errors for
    // malformed requests. In particular, a bad MAGIC will
    // result in a lookup in the named key table (stringToServerSubcontract),
    // which must return a valid ServerRequestDispatcher. A bad subcontract ID
    // will similarly need to return the default ServerRequestDispatcher.

    public ServerRequestDispatcher getServerRequestDispatcher(int scid) {
        ServerRequestDispatcher sdel = SDRegistry.get(scid);
        if (sdel == null)
            sdel = SDRegistry.get(defaultId);

        return sdel;
    }

    public ServerRequestDispatcher getServerRequestDispatcher(String name) {
        ServerRequestDispatcher sdel = stringToServerSubcontract.get(name);

        if (sdel == null)
            sdel = SDRegistry.get(defaultId);

        return sdel;
    }

    public LocalClientRequestDispatcherFactory getLocalClientRequestDispatcherFactory(int scid) {
        LocalClientRequestDispatcherFactory factory = LCSFRegistry.get(scid);
        if (factory == null) {
            factory = LCSFRegistry.get(defaultId);
        }

        return factory;
    }

    public ClientRequestDispatcher getClientRequestDispatcher(int scid) {
        ClientRequestDispatcher subcontract = CSRegistry.get(scid);
        if (subcontract == null) {
            subcontract = CSRegistry.get(defaultId);
        }

        return subcontract;
    }

    public ObjectAdapterFactory getObjectAdapterFactory(int scid) {
        ObjectAdapterFactory oaf = OAFRegistry.get(scid);
        if (oaf == null)
            oaf = OAFRegistry.get(defaultId);

        return oaf;
    }

    public Set<ObjectAdapterFactory> getObjectAdapterFactories() {
        return objectAdapterFactoriesView;
    }
}
