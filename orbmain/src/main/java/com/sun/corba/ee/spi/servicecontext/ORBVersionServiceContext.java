/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.servicecontext;

import com.sun.corba.ee.spi.orb.ORBVersion;

import com.sun.corba.ee.spi.misc.ORBConstants;

public interface ORBVersionServiceContext extends ServiceContext {
    int SERVICE_CONTEXT_ID = ORBConstants.TAG_ORB_VERSION;

    ORBVersion getVersion();
}
