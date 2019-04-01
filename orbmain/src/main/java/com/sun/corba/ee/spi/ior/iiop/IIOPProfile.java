/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedProfile;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;

/**
 * IIOPProfile represents an IIOP tagged profile. It is essentially composed of an object identifier and a template. The
 * template contains all of the IIOP specific information in the profile. Templates are frequently shared between many
 * different profiles, while the object identifiy is unique to each profile.
 */
@ManagedData
@Description("The IIOPProfile version of a TaggedProfile")
public interface IIOPProfile extends TaggedProfile {
    @ManagedAttribute
    @Description("The ORB version in use")
    ORBVersion getORBVersion();

    /**
     * Return the servant for this profile, if it is local AND if the OA that implements this objref supports direct access
     * to servants outside of an invocation.
     */
    java.lang.Object getServant();

    /**
     * Return the GIOPVersion of this profile. Caches the result.
     */
    GIOPVersion getGIOPVersion();

    /**
     * Return the Codebase of this profile. Caches the result.
     */
    String getCodebase();
}
