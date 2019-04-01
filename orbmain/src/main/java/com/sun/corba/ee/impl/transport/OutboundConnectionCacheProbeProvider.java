/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 * This simple class is for the GFv3 probe mechanism. As of 9/16/09, gfprobes have the unfortunate requirement that ALL
 * methods in the class MUST be probe methods (see issue 9536). Otherwise I would put these methods in the
 * CorbaOutboundConnectionCacheImpl class.
 *
 * @author Ken Cavanaugh
 */
@ProbeProvider(moduleProviderName = "glassfish", moduleName = "orb", probeProviderName = "outboundconnection")
public class OutboundConnectionCacheProbeProvider {
    @Probe(name = "outboundConnectionOpened")
    public void connectionOpenedEvent(@ProbeParam("contactInfo") String contactInfo, @ProbeParam("connection") String connection) {
    }

    @Probe(name = "outboundConnectionClosed")
    public void connectionClosedEvent(@ProbeParam("contactInfo") String contactInfo, @ProbeParam("connection") String connection) {
    }
}

// End of file.
