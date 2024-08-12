/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
     * 
     * @param os stream to write to
     * @param gv version to use
     */
    void write(OutputStream os, GIOPVersion gv);

    /**
     * Add a service context. Silently replaces an existing service context with the same id.
     * 
     * @param sc service context to use
     */
    public void put(ServiceContext sc);

    /**
     * Remove the service context with the id, if any.
     * 
     * @param scId service context id
     */
    public void delete(int scId);

    /**
     * Return the service context with scId, or null if not found.
     * 
     * @param scId service context id
     * @return ServiceContext with the specified ID
     */
    public ServiceContext get(int scId);

    public ServiceContexts copy();
}
