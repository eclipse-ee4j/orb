/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

