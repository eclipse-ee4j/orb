/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.enuminterop;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject; 

public class EchoServant extends PortableRemoteObject implements Echo
{
    public EchoServant() throws RemoteException {
        super() ;
    }

    public Object echoObject( String arg ) throws RemoteException {
        return getDay( arg ) ;
    }

    public Day echoDay( String arg ) throws RemoteException {
        return getDay( arg ) ;
    }

    private Day getDay( String arg ) throws RemoteException {
        try {
            return Enum.valueOf( Day.class, arg ) ;
        } catch (Exception exc) {
            throw new RemoteException( "Bad enum name", exc ) ;
        }
    }
}
