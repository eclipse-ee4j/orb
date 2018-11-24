/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

public interface SocketInfo {
    // Endpoint types known in advance.
    // If you change the value of this constant then update
    // activation.idl accordingly. It has a duplicate definition
    // to avoid a compilation dependency.

    String IIOP_CLEAR_TEXT = "IIOP_CLEAR_TEXT";
    String SSL_PREFIX = "SSL";

    String getType();

    String getHost();

    int getPort();
}
