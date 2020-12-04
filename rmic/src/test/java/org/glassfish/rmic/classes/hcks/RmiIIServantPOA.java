/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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
// Created       : 2000 Nov 11 (Sat) 10:45:48 by Harold Carr.
// Last Modified : 2001 May 10 (Thu) 15:45:53 by Harold Carr.
//

package org.glassfish.rmic.classes.hcks;

import org.omg.CORBA.ORB;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RmiIIServantPOA extends PortableRemoteObject implements RmiII {
    public static final String baseMsg = RmiIIServantPOA.class.getName();

    public String name;

    RmiIIServantPOA(ORB orb, String name) throws RemoteException {
        // DO NOT CALL SUPER - that would connect the object.
        this.name = name;
    }

    public String sayHello() {
        return "Hello, World!";
    }

    public int sendBytes(byte[] x) {
        if (x == null) return -1;
        return x.length;
    }

    public Object sendOneObject(Object x) throws RmiIMyException {
        return x;
    }

    public Object sendTwoObjects(Object x, Object y) {
        return x;
    }

    public String makeColocatedCallFromServant() throws RemoteException {
        return "";
    }

    private String doCall(RmiII rrmiiI, String resultSoFar) throws Exception {
        String result = rrmiiI.colocatedCallFromServant(resultSoFar);
        String op = "op";
        return op + " " + result;
    }

    public String colocatedCallFromServant(String a) throws RemoteException {
        String op = "op";
        return op + " " + a;
    }

    public String throwThreadDeathInServant(String a) throws RemoteException, ThreadDeath {
        throw new ThreadDeath();
    }

    public Object returnObjectFromServer(boolean isSerializable) throws RemoteException {
        return isSerializable ? "" : new RmiIIServantPOA(null, "");
    }
}

// End of file.
