/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverrequestinfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.omg.PortableInterceptor.*;
import ServerRequestInfo.*;

/**
 * Hello interface for RMI-IIOP version of test
 */
public interface helloIF
    extends Remote
{
  String sayHello () throws RemoteException;
  void sayOneway () throws RemoteException;
  void saySystemException () throws RemoteException;
  void sayUserException () throws ExampleException, RemoteException;
  String syncWithServer ( boolean exceptionRaised ) throws RemoteException;
  void sayInvokeAgain ( int n ) throws RemoteException;
}

