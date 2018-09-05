/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport.connection;

import java.io.IOException ;

/** A Connection represents some kind of channel to a (possibly different) process.
 * Here we only need the capability of closing the connection.  Any connection
 * must also define hashCode and equals properly so that it can be used in a map.
 * It is also recommended that toString() be defined to return a useful summary
 * of the connection (e.g. address information).
 */
public interface Connection {
    void close() throws IOException ;
}

