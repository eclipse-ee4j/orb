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
