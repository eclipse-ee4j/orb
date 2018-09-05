/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
