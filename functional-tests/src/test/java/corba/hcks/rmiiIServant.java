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
// Created       : Spring 1999 by Harold Carr.
// Last Modified : 2001 May 10 (Thu) 15:45:57 by Harold Carr.
//

package corba.hcks;

import java.rmi.RemoteException;
import org.omg.CORBA.INTERNAL;

//import java.rmi.server.UnicastRemoteObject; // JRMP
import javax.rmi.PortableRemoteObject;        // IIOP

public class rmiiIServant
    extends 
        //UnicastRemoteObject // JRMP
        PortableRemoteObject  // IIOP
    implements 
        rmiiI
{
    public static final String baseMsg = rmiiIServant.class.getName();

    public rmiiIServant ()
        throws
            RemoteException 
    {
        super();
    }

    public String sayHello ()
    {
        return  C.helloWorld;
    }

    public int sendBytes (byte[] x)
    {
        if (x == null)
            return -1;
        return x.length;
    }

    public Object sendOneObject (Object x)
        throws
            rmiiMyException
    {
        return x;
    }

    public Object sendTwoObjects (Object x, Object y)
    {
        return x;
    }

    // REVISIT
    public String makeColocatedCallFromServant ()
        throws
            RemoteException
    {
        String result;
        try {
            result = ((rmiiI)this.narrow(this, rmiiIServant.class))
                .colocatedCallFromServant("A");
        } catch (Exception e) {
            U.sopUnexpectedException(baseMsg + C.makeColocatedCallFromServant,
                                     e);
            throw new INTERNAL(U.SHOULD_NOT_SEE_THIS);
        }
        return result;
    }

    // REVISIT
    public String colocatedCallFromServant (String a)
        throws
            RemoteException,
            Exception
    {
        return "B" + a;
    }

    public String throwThreadDeathInServant (String a)
        throws
            RemoteException,
            ThreadDeath
    {
        U.sop(U.servant(a));
        throw new ThreadDeath();
    }

    public Object returnObjectFromServer (boolean isSerializable)
        throws
            RemoteException
    {
        if (isSerializable) {
            return new SerializableObject();
        } else {
            return new NonSerializableObject();
        }
    }

}

// End of file.

