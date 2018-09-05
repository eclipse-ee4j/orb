/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

import java.rmi.*;
import java.io.*;
import java.util.*;
import java.sql.Date;

public interface Tester extends Remote, AbsTester {

    Object verify(Object obj) throws RemoteException;

    Map verify(Map map) throws RemoteException;

    List verify(List list) throws RemoteException;

    Date verify(Date date) throws RemoteException;

    Properties verify(Properties props) throws RemoteException;

    MarshalTester verify(byte[] predata, MarshalTester input, byte[] postdata) 
        throws RemoteException, DataCorruptedException;

    Hashtable verify(Hashtable table) throws RemoteException;

    void throwRuntimeException() throws RemoteException;

    void throwRemoteException() throws RemoteException;

    void throwCheckedException() 
        throws CheckedException, RemoteException;

    AbsTester getAbsTester() throws RemoteException;
}
