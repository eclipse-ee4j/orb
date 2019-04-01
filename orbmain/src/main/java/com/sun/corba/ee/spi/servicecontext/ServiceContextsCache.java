/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.servicecontext;

import java.util.EnumMap;
import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.ee.spi.orb.ORB;

public class ServiceContextsCache {

    public static enum CASE {
        CLIENT_INITIAL, CLIENT_SUBSEQUENT, SERVER_INITIAL, SERVER_SUBSEQUENT
    };

    private EnumMap<CASE, ServiceContexts> data;
    private ORB orb;

    public ServiceContextsCache(com.sun.corba.ee.spi.orb.ORB orb) {

        data = new EnumMap<CASE, ServiceContexts>(CASE.class);
        this.orb = orb;

    }

    public synchronized ServiceContexts get(CASE c) {

        if (data.size() == 0) {

            // CLIENT_INITIAL
            ServiceContexts scContainer = ServiceContextDefaults.makeServiceContexts(orb);
            scContainer.put(ServiceContextDefaults.getMaxStreamFormatVersionServiceContext());
            scContainer.put(ServiceContextDefaults.getORBVersionServiceContext());
            scContainer.put(ServiceContextDefaults.makeSendingContextServiceContext(orb.getFVDCodeBaseIOR()));

            data.put(CASE.CLIENT_INITIAL, scContainer);

            // CLIENT_SUBSEQUENT
            scContainer = ServiceContextDefaults.makeServiceContexts(orb);
            scContainer.put(ServiceContextDefaults.getMaxStreamFormatVersionServiceContext());
            scContainer.put(ServiceContextDefaults.getORBVersionServiceContext());

            data.put(CASE.CLIENT_SUBSEQUENT, scContainer);

            // SERVER_INITIAL
            scContainer = ServiceContextDefaults.makeServiceContexts(orb);
            scContainer.put(ServiceContextDefaults.getORBVersionServiceContext());
            scContainer.put(ServiceContextDefaults.makeSendingContextServiceContext(orb.getFVDCodeBaseIOR()));

            data.put(CASE.SERVER_INITIAL, scContainer);

            // SERVER_SUBSEQUENT
            scContainer = ServiceContextDefaults.makeServiceContexts(orb);
            scContainer.put(ServiceContextDefaults.getORBVersionServiceContext());

            data.put(CASE.SERVER_SUBSEQUENT, scContainer);

        }

        return (data.get(c)).copy();
    }
}
