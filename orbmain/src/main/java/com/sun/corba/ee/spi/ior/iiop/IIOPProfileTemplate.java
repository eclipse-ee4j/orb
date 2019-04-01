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

import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

/**
 * IIOPProfileTemplate represents the parts of an IIOPProfile that are independent of the object identifier. It is a
 * container of tagged components.
 */
@ManagedData
@Description("Template for an IIOP profile")
public interface IIOPProfileTemplate extends TaggedProfileTemplate {
    /**
     * Return the GIOP version of this profile.
     */
    public GIOPVersion getGIOPVersion();

    /**
     * Return the IIOP address from the IIOP profile. This is called the primary address here since other addresses may be
     * contained in components.
     */
    @ManagedAttribute
    @Description("The host and port of the IP address for the primary endpoint of this profile")
    public IIOPAddress getPrimaryAddress();
}
