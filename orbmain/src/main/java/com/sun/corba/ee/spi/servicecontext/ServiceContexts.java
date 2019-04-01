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

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.spi.servicecontext.ServiceContext;

/**
 * The collection of ServiceContext instances used in a particular request.
 */
public interface ServiceContexts {
    /**
     * Write the service contexts to the output stream. If an UnknownExceptionInfo service context is present, it is written
     * out last, so that it follows any SendingContext service context. This is required so that the codebase is available
     * to handle value types if necessary.
     *
     * We should really do this as SendingContext goes first, so that we can guarantee correct marshalling of non-standard
     * service contexts.
     */
    void write(OutputStream os, GIOPVersion gv);

    /**
     * Add a service context. Silently replaces an existing service context with the same id.
     */
    public void put(ServiceContext sc);

    /**
     * Remove the service context with the id, if any.
     */
    public void delete(int scId);

    /**
     * Return the service context with scId, or null if not found.
     */
    public ServiceContext get(int scId);

    public ServiceContexts copy();
}
