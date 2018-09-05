/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.hcks;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class RmiIIServant extends PortableRemoteObject implements RmiII {

    public RmiIIServant() throws RemoteException {
        super();
    }

    public String sayHello() {
        return "Hello, World!";
    }

    public int sendBytes (byte[] x)
    {
        if (x == null)
            return -1;
        return x.length;
    }

    public Object sendOneObject(Object x) throws RmiIMyException {
        return x;
    }

    public Object sendTwoObjects (Object x, Object y)
    {
        return x;
    }

    public String makeColocatedCallFromServant() throws RemoteException {
        return "";
    }

    public String colocatedCallFromServant (String a) throws RemoteException {
        return "B" + a;
    }

    public String throwThreadDeathInServant (String a) throws RemoteException, ThreadDeath {
        throw new ThreadDeath();
    }

    public Object returnObjectFromServer (boolean isSerializable) throws RemoteException {
        return isSerializable ? "" : new RmiIIServant();
    }

}

