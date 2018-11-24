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

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;

@ManagedData
@Description("An address of a transport endpoint that the ORB " + "uses for listening to incoming requests")
public class USLPort {
    private String type;
    private int port;

    public USLPort(String type, int port) {
        this.type = type;
        this.port = port;
    }

    @ManagedAttribute
    @Description("The type of the port (e.g. plain text vs. SSL)")
    public String getType() {
        return type;
    }

    @ManagedAttribute
    @Description("The TCP port number")
    public int getPort() {
        return port;
    }

    public String toString() {
        return type + ":" + port;
    }
}

// End of file.
