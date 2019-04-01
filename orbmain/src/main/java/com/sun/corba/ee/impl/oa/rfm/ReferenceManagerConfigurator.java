/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.rfm;

import org.omg.CORBA.LocalObject;

import org.omg.PortableServer.POA;

import org.omg.PortableInterceptor.IORInterceptor_3_0;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.oa.ObjectAdapter;

import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;

import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.spi.logging.POASystemException;

/**
 * Used to initialize the ReferenceManager in the ORB. The ReferenceManager is an optional component built on top of the
 * ORB that is used to manage a group of POAs that require reconfigurability. This class sets up the ORB as follows:
 * <ol>
 * <li>Create an instance of ReferenceFactoryManagerImpl and register it with register_local_reference.
 * <li>Create and register an IORInterceptor that prevent outside POAs from interfering with the ReferenceManager.
 * </ol>
 */
public class ReferenceManagerConfigurator implements ORBConfigurator {
    private static final POASystemException wrapper = POASystemException.self;

    private static class RMIORInterceptor extends LocalObject implements IORInterceptor_3_0 {
        private ReferenceFactoryManagerImpl rm;

        public RMIORInterceptor(ReferenceFactoryManagerImpl rm) {
            this.rm = rm;
        }

        public String name() {
            return "##" + this.getClass().getName() + "##";
        }

        public void destroy() {
            // NO-OP
        }

        public void establish_components(IORInfo info) {
            // NO-OP
        }

        public void adapter_manager_state_changed(int id, short state) {
            // NO-OP
        }

        public void adapter_state_changed(ObjectReferenceTemplate[] templates, short state) {
            // NO-OP
        }

        // We must do the checking here, because exceptions are not
        // ignored. All exceptions thrown in establish_components
        // are ignored. The whole purpose of this interceptor is
        // to throw an exception if an error is detected.
        public void components_established(IORInfo info) {
            IORInfoExt ext = IORInfoExt.class.cast(info);
            ObjectAdapter oa = ext.getObjectAdapter();
            if (!(oa instanceof POA)) {
                return;
            } // if not POA, then there is no chance of a conflict.
            POA poa = POA.class.cast(oa);
            rm.validatePOACreation(poa);
        }
    }

    private static class RMORBInitializer extends LocalObject implements ORBInitializer {
        private IORInterceptor_3_0 interceptor;

        public RMORBInitializer(IORInterceptor_3_0 interceptor) {
            this.interceptor = interceptor;
        }

        public void pre_init(ORBInitInfo info) {
            // NO-OP
        }

        public void post_init(ORBInitInfo info) {
            try {
                info.add_ior_interceptor(interceptor);
            } catch (Exception exc) {
                throw wrapper.rfmPostInitException(exc);
            }
        }
    }

    public void configure(DataCollector collector, ORB orb) {
        try {
            ReferenceFactoryManagerImpl rm = new ReferenceFactoryManagerImpl(orb);
            orb.register_initial_reference(ORBConstants.REFERENCE_FACTORY_MANAGER, rm);
            IORInterceptor_3_0 interceptor = new RMIORInterceptor(rm);
            ORBInitializer initializer = new RMORBInitializer(interceptor);
            orb.getORBData().addORBInitializer(initializer);
        } catch (Exception exc) {
            throw wrapper.rfmConfigureException(exc);
        }
    }
}
