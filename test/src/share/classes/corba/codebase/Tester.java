/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.codebase;

public interface Tester extends java.rmi.Remote {

    void printMessage(String message) throws java.rmi.RemoteException;

    Object requestValue() 
        throws java.rmi.RemoteException, 
               ClassNotFoundException, 
               InstantiationException,
               IllegalAccessException;

    String processValue(Object value) throws java.rmi.RemoteException;

    String SERVER_DOWNLOADING_FLAG = "SERVER_DOWNLOADING";
}
