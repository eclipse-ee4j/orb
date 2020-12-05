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
// Created       : 2005 Jun 09 (Thu) 14:44:09 by Harold Carr.
// Last Modified : 2005 Sep 29 (Thu) 23:00:33 by Harold Carr.
//

package corba.folb;

import java.util.List;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;

public class EchoTestServant
    extends PortableRemoteObject
    implements EchoTest
{
    public static final String baseMsg = EchoTestServant.class.getName();

    private ORB orb;

    public EchoTestServant(ORB orb)
        throws RemoteException
    {
        this.orb = orb;
    }

    public String echo(String x)
        throws RemoteException
    {
        String result = "TestServant echoes: " + x;
        dprint(".echo: " + result);
        return result;
    }

    public void neverReturns()
        throws RemoteException
    {
        try {
            dprint(".neverReturns");
            Object o = new Object();
            try { 
                synchronized (o) {
                    o.wait(); 
                } 
            } catch (InterruptedException e) {
                ;
            }
        } catch (Exception e) {
            dprint(".neverReturns: !!! Unexpected Exception");
            e.printStackTrace(System.out);
        }
    }

    private void dprint(String msg)
    {
        ORBUtility.dprint("Server", msg);
    }
}

// End of file.
