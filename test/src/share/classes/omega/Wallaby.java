/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
