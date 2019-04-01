/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.poa;

import com.sun.corba.ee.spi.ior.ObjectKey;

/**
 * The bad server id handler is used to locate persistent objects. The Locator object registers the BadServerIdHandler
 * with the ORB and when requests for persistent objects for servers (other than itself) comes, it throws a
 * ForwardException with the IOR pointing to the active server.
 */
public interface BadServerIdHandler {
    void handle(ObjectKey objectKey);
}
