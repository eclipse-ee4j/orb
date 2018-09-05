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

public interface ThrowCORBAUserException extends Remote {
    void a () throws RemoteException;
    void b () throws RemoteException, CORBAUserException;
    void c () throws RemoteException, CORBAUserException, TestException;
    void d () throws RemoteException, CORBAUserException, UserException, TestException;
    void e () throws RemoteException, CORBAUserException, CORBAUserException2, UserException, TestException;
    void f () throws RemoteException, TestException, CORBAUserException, CORBAUserException2, UserException;
    void g () throws RemoteException, UserException, CORBAUserException, TestException, CORBAUserException2;
    void h () throws RemoteException, TestException, CORBAUserException, CORBAUserException2;
    void i () throws RemoteException, CORBAUserException, TestException, CORBAUserException2;
    void j () throws RemoteException, CORBAUserException, CORBAUserException2,TestException;
}
