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

import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import org.omg.CORBA.SystemException;
import com.sun.corba.ee.spi.ior.IOR;

/**
 * This interface captures the ReplyMessage contract.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public interface ReplyMessage extends Message, LocateReplyOrReplyMessage {

    // Note: If the value, order, or number of these constants change,
    // please update the REPLY_MESSAGE_TO_PI_REPLY_STATUS table in PIHandlerImpl.
    int NO_EXCEPTION = 0;
    int USER_EXCEPTION = 1;
    int SYSTEM_EXCEPTION = 2;
    int LOCATION_FORWARD = 3;
    int LOCATION_FORWARD_PERM = 4; // 1.2
    int NEEDS_ADDRESSING_MODE = 5; // 1.2

    ServiceContexts getServiceContexts();

    void setIOR(IOR newIOR);
}
