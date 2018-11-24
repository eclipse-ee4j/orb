/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.presentation.rmi;

/**
 * Represents an interceptor that is invoked around a RMI-IIOP method invocation when dynamic RMI-IIOP is used. This
 * facility is not available either in IDL-based calls, or in static RMI-IIOP.
 */
public interface InvocationInterceptor {
    /**
     * Called just before a dynamic RMI-IIOP stub is called. Any exceptions thrown by this method are ignored.
     */
    void preInvoke();

    /**
     * Called just before a dynamic RMI-IIOP stub returns control to the caller. Any exceptions thrown by this method are
     * ignored.
     */
    void postInvoke();
}
