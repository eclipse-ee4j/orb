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

package com.sun.corba.ee.impl.servicecontext;

import java.util.Map;
import java.util.HashMap;
import com.sun.corba.ee.spi.servicecontext.ServiceContext;
import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

public class ServiceContextFactoryRegistryImpl implements ServiceContextFactoryRegistry {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    final private ORB orb;
    private Map<Integer, ServiceContext.Factory> scMap;

    public ServiceContextFactoryRegistryImpl(ORB orb) {
        scMap = new HashMap<Integer, ServiceContext.Factory>();
        this.orb = orb;
    }

    public void register(ServiceContext.Factory factory) {
        if (scMap.get(factory.getId()) == null) {
            scMap.put(factory.getId(), factory);
        } else {
            wrapper.registerDuplicateServiceContext();
        } // BAD_PARAM
    }

    public ServiceContext.Factory find(int scId) {
        ServiceContext.Factory result = scMap.get(scId);
        return result;
    }
}
