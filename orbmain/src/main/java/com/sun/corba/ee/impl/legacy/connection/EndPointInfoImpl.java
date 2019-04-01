/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.legacy.connection;

import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

public class EndPointInfoImpl implements SocketInfo, LegacyServerSocketEndPointInfo {

    protected String type;
    protected String hostname;
    protected int port;
    protected int locatorPort;
    protected String name;

    public EndPointInfoImpl(String type, int port, String hostname) {
        this.type = type;
        this.port = port;
        this.hostname = hostname;
        this.locatorPort = -1;
        this.name = LegacyServerSocketEndPointInfo.NO_NAME;
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return hostname;
    }

    public String getHostName() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getLocatorPort() {
        return locatorPort;
    }

    public void setLocatorPort(int port) {
        locatorPort = port;
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        return type.hashCode() ^ hostname.hashCode() ^ port;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof EndPointInfoImpl)) {
            return false;
        }
        EndPointInfoImpl other = (EndPointInfoImpl) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (!hostname.equals(other.hostname)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return type + " " + name + " " + hostname + " " + port;
    }
}

// End of file.
