/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Apr 10 (Thu) 11:38:12 by Harold Carr.
// Last Modified : 2003 Sep 28 (Sun) 13:15:45 by Harold Carr.
//

package corba.connections;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;

import com.sun.corba.ee.spi.orb.ORB;

public class RemoteService
    extends 
        PortableRemoteObject
    implements 
        RemoteInterface
{
    ORB orb;
    String serverName;
    Object blocker;
    ConnectionStatistics stats;

    public RemoteService (ORB orb, String serverName)
        throws RemoteException 
    {
        super();
        this.orb = orb;
        this.serverName = serverName;
        this.blocker = new Object();
        this.stats = new ConnectionStatistics( orb );
    }

    public Struct[] method(Struct[] in)
        throws RemoteException 
    {
        return in;
    }

    public void block()
        throws RemoteException
    {
        synchronized (blocker) {
            try {
                blocker.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public void resume()
        throws RemoteException
    {
        synchronized (blocker) {
            blocker.notifyAll();
        }
    }

    public String testMonitoring ()
        throws RemoteException
    {
        return
            stats.outbound(serverName, orb)
            +
            stats.inbound(serverName, orb);
    }
}

// End of file.

