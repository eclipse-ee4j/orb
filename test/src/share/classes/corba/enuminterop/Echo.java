/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.enuminterop ;

import java.rmi.Remote;
import java.rmi.RemoteException;
                                                                                
public interface Echo extends Remote
{
    enum Day { Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday } 

    Object echoObject( String arg ) throws RemoteException ;

    Day echoDay( String arg ) throws RemoteException ;
}

