/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

/*-----------------------------------------------------------------------------
  - bar() collides with constant
  - eat(), getFoo() throw superclass of RemoteException
  - drink() throws RemoteException and one of its subclasses
-----------------------------------------------------------------------------*/

package omega;

public interface Wallaby extends java.rmi.Remote {

    int     bar = 2;
    void    bar()              throws java.rmi.RemoteException,
    java.lang.RuntimeException;
    void    eat()              throws Exception;
    void    drink()            throws java.rmi.RemoteException,
    java.rmi.NoSuchObjectException;

    int     getFoo()           throws Exception;
    void    setFoo(int x)      throws java.rmi.RemoteException,
    java.rmi.NoSuchObjectException;
    String  getURL()           throws java.rmi.RemoteException,
    java.lang.RuntimeException;
    boolean isBoo()            throws java.rmi.RemoteException;
    void    setBoo(boolean b)  throws java.rmi.RemoteException;

}
