/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa;

import org.omg.CORBA.SystemException;

import com.sun.corba.ee.spi.oa.NullServant;

public class NullServantImpl implements NullServant {
    private SystemException sysex;

    public NullServantImpl(SystemException ex) {
        this.sysex = ex;
    }

    public SystemException getException() {
        return sysex;
    }
}
