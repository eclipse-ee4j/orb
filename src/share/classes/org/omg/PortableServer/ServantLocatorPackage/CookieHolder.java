/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.PortableServer.ServantLocatorPackage;

/**
 * The native type PortableServer::ServantLocator::Cookie is mapped 
 * to java.lang.Object. A CookieHolder class is provided for passing 
 * the Cookie type as an out parameter. The CookieHolder class 
 * follows exactly the same pattern as the other holder classes 
 * for basic types.
 */

final public class CookieHolder implements org.omg.CORBA.portable.Streamable
{
    public java.lang.Object value;

    public CookieHolder() { }

    public CookieHolder(java.lang.Object initial) {
        value = initial;
    }

    public void _read( org.omg.CORBA.portable.InputStream is) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    } 

    public void _write( org.omg.CORBA.portable.OutputStream os) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    } 

    public org.omg.CORBA.TypeCode _type() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    } 
}
