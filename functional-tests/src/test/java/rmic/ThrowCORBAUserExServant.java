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

package rmic;
import org.omg.CORBA.UserException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class ThrowCORBAUserExServant implements ThrowCORBAUserException {
    public ThrowCORBAUserExServant() throws RemoteException {}
    public void a () throws RemoteException {}
    public void b () throws RemoteException, CORBAUserException {}
    public void c () throws RemoteException, CORBAUserException, TestException {}
    public void d () throws RemoteException, CORBAUserException, UserException, TestException {}
    public void e () throws RemoteException, CORBAUserException, CORBAUserException2, UserException, TestException {}
    public void f () throws RemoteException, TestException, CORBAUserException, CORBAUserException2, UserException {}
    public void g () throws RemoteException, UserException, CORBAUserException, TestException, CORBAUserException2 {}
    public void h () throws RemoteException, TestException, CORBAUserException, CORBAUserException2 {}
    public void i () throws RemoteException, CORBAUserException, TestException, CORBAUserException2 {}
    public void j () throws RemoteException, CORBAUserException, CORBAUserException2,TestException {}
}
