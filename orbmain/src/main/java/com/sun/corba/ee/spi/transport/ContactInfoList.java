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

import java.util.Iterator;

import com.sun.corba.ee.spi.ior.IOR;

import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher;

/**
 * @author Harold Carr
 */
public abstract interface ContactInfoList {
    public Iterator<ContactInfo> iterator(); // covariant override

    public void setTargetIOR(IOR ior);

    public IOR getTargetIOR();

    public void setEffectiveTargetIOR(IOR locatedIor);

    public IOR getEffectiveTargetIOR();

    public LocalClientRequestDispatcher getLocalClientRequestDispatcher();

    public int hashCode();
}

// End of file.
