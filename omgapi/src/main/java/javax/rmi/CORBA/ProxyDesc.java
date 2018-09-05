/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA;

/**
 * This class is used to marshal java.lang.Proxy objects over IIOP.
 */
public class ProxyDesc implements java.io.Serializable {
    /**
     * @serial The class names of the interfaces that the Proxy object
     * implements.
     */
    public String[] interfaces;

    /**
     * @serial A space-separated list of codebase URLs.
     */
    public String codebase;

    /**
     * @serial The Proxy's InvocationHandler instance.
     */
    public java.lang.reflect.InvocationHandler handler;

    static final long serialVersionUID = 1234286961190911798L;
}
