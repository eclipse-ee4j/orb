/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfoList;

public abstract class ClientDelegate extends org.omg.CORBA_2_3.portable.Delegate {
    /**
     * The ORB associated * with an invocation.
     *
     * @return ORB
     */
    public abstract ORB getBroker();

    /**
     * Get the CorbaContactInfoList which represents they encoding/protocol/transport combinations that may be used to
     * contact the service.
     *
     * @return CorbaContactInfoList
     */
    public abstract ContactInfoList getContactInfoList();
}

// End of file.
