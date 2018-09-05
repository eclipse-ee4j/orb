/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.legacy.interceptor;

/** This extension is used to provide information about whether or not
 * the object to which the incoming request is dispatched is a name service
 * or not.  This is added to the implementation of the PortableInterceptor
 * ServerRequestInfo interface (see impl.interceptors.ServerRequestInfoImpl)
 * to provide this extended functionality).
 *
 * @author ken
 */
public interface ServerRequestInfoExt {
    boolean isNameService() ;
}
