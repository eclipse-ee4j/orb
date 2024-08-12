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
