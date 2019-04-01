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

import com.sun.corba.ee.spi.transport.ContactInfoList;

import com.sun.corba.ee.spi.protocol.ClientDelegate;

/**
 * Interface used to create a ClientDelegate from a ContactInfoList.
 */
public interface ClientDelegateFactory {
    ClientDelegate create(ContactInfoList list);
}
