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

package com.sun.corba.ee.impl.legacy.connection;

import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

public class EndPointInfoImpl
    implements
        SocketInfo,
        LegacyServerSocketEndPointInfo
{

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

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getHost() {
        return hostname;
    }

    @Override
    public String getHostName() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getLocatorPort ()
    {
        return locatorPort;
    }

    @Override
    public void setLocatorPort (int port)
    {
        locatorPort = port;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ hostname.hashCode() ^ port;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EndPointInfoImpl)) {
            return false;
        }
        EndPointInfoImpl other = (EndPointInfoImpl)obj;
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

    @Override
    public String toString ()
    {
        return
            type + " " +
            name + " " +
            hostname + " " +
            port;
    }
}

// End of file.
