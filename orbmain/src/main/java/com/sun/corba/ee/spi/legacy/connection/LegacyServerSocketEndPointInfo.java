/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.legacy.connection;

/**
 * LegacyServerSocketEndPointInfo is an abstraction of a port.
 */
public interface LegacyServerSocketEndPointInfo {
    /**
     * e.g.: "CLEAR_TEXT", "SSL", ...
     */
    public String getType();

    /**
     * Get the host name of this end point. Subcontracts must use this instead of InetAddress.getHostName() because this
     * would take into account the value of the ORBServerHost property.
     */
    public String getHostName();

    public int getPort();

    /**
     * The ORBD's proxy port of this end point. Note: Pre-ORT "port-exchange" model.
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
