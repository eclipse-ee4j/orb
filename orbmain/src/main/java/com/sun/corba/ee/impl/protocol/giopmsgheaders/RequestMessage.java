/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.spi.servicecontext.ServiceContexts;

/**
 * This interface captures the RequestMessage contract.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public interface RequestMessage extends Message {

    byte RESPONSE_EXPECTED_BIT = 0x01;

    ServiceContexts getServiceContexts();
    void setServiceContexts(ServiceContexts sc);
    int getRequestId();
    boolean isResponseExpected();
    byte[] getReserved();
    ObjectKeyCacheEntry getObjectKeyCacheEntry();
    String getOperation();
    @SuppressWarnings({"deprecation"})
    org.omg.CORBA.Principal getPrincipal();

    // NOTE: This is a SUN PROPRIETARY EXTENSION
    void setThreadPoolToUse(int poolToUse);


} // interface RequestMessage
