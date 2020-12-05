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
