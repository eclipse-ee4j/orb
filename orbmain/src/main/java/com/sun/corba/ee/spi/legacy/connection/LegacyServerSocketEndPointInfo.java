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

package com.sun.corba.ee.spi.legacy.connection;

/**
 * LegacyServerSocketEndPointInfo is an abstraction of a port.
 */
public interface LegacyServerSocketEndPointInfo {
    /**
     * e.g.: "CLEAR_TEXT", "SSL", ...
     * 
     * @return type
     */
    public String getType();

    /**
     * Get the host name of this end point. Subcontracts must use this instead of InetAddress.getHostName() because this
     * would take into account the value of the ORBServerHost property.
     * 
     * @return the host name
     */
    public String getHostName();

    public int getPort();

    /**
     * The ORBD's proxy port of this end point. Note: Pre-ORT "port-exchange" model.
     * 
     * @return proxy port
     */
    public int getLocatorPort();

    public void setLocatorPort(int port);

    // NAME is used while we still have a "port-exchange" ORBD
    // to get what used to be called "default" or "bootstrap" endpoints.

    public static final String DEFAULT_ENDPOINT = "DEFAULT_ENDPOINT";
    public static final String BOOT_NAMING = "BOOT_NAMING";
    public static final String NO_NAME = "NO_NAME";

    public String getName();
}

// End of file.
