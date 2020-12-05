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

package com.sun.corba.ee.impl.transport;

import org.glassfish.external.probe.provider.annotations.Probe ;
import org.glassfish.external.probe.provider.annotations.ProbeProvider ;
import org.glassfish.external.probe.provider.annotations.ProbeParam ;

/** This simple class is for the GFv3 probe mechanism.  As of 
 * 9/16/09, gfprobes have the unfortunate requirement that ALL
 * methods in the class MUST be probe methods (see issue 9536).
 * Otherwise I would put these methods in the CorbaInboundConnectionCacheImpl class.
 *
 * @author Ken Cavanaugh 
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="orb" , probeProviderName="inboundconnection")
public class InboundConnectionCacheProbeProvider {
    @Probe(name="inboundConnectionOpened" )
    public void connectionOpenedEvent( 
        @ProbeParam( "acceptor" ) String acceptor, 
        @ProbeParam( "connection" ) String connection ) {}

    @Probe(name="inboundConnectionClosed" )
    public void connectionClosedEvent( 
        @ProbeParam( "connection" ) String connection ) {}
}


