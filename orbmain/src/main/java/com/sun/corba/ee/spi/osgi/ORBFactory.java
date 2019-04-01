/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.osgi;

import java.util.Properties;

import org.glassfish.external.amx.AMXGlassfish;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ClassCodeBaseHandler;

import com.sun.corba.ee.impl.orb.ORBImpl;

import com.sun.corba.ee.impl.osgi.loader.OSGIListener;
import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * A simple factory for creating our ORB that avoids the ClassLoader problems with org.omg.CORBA.ORB.init, which must
 * load the ORB impl class. The usual OSGi configuration prevents this, so we just directly use a static factory method
 * here. Note that this also assumes that the created ORB should be suitable for running inside GlassFish v3.
 */
public class ORBFactory {
    private ORBFactory() {
    }

    public static ORB create(String[] args, Properties props, boolean useOSGi) {
        ORB result = create();
        initialize(result, args, props, useOSGi);
        return result;
    }

    /**
     * Create but do not initialize an ORB instance.
     *
     * @return The newly created uninitialized ORB.
     */
    public static ORB create() {
        ORB result = new ORBImpl();
        return result;
    }

    /**
     * Complete the initialization of the ORB. useOSGi if true will cause an ORB initialization suitable for use in
     * GlassFish v3.
     *
     * @param orb The orb to initialize.
     * @param args Usual args passed to an ORB.init() call.
     * @param props Usual props passed to an ORB.init() call.
     * @param useOSGi true if the ORB is running in GFv3 or later (generally means an OSGI environment).
     */
    @SuppressWarnings("static-access")
    public static void initialize(ORB orb, String[] args, Properties props, boolean useOSGi) {
        // Always disable ORBD if coming through the ORBFactory.
        // Anyone that wants ORBD must use ORB.init as usual.
        // Actually we assume that we are running in GFv3 if this method is called,
        // regardless of whether OSGi is used or not.
        props.setProperty(ORBConstants.DISABLE_ORBD_INIT_PROPERTY, "true");

        if (useOSGi) {
            orb.classNameResolver(orb.makeCompositeClassNameResolver(OSGIListener.classNameResolver(), orb.defaultClassNameResolver()));

            ClassCodeBaseHandler ccbh = OSGIListener.classCodeBaseHandler();
            orb.classCodeBaseHandler(ccbh);
        }

        orb.setRootParentObjectName(AMXGlassfish.DEFAULT.serverMonForDAS());

        orb.setParameters(args, props);
    }
}

// End of file.
