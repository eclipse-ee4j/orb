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
// Last Modified : 2001 May 10 (Thu) 15:44:42 by Harold Carr.
//

package corba.hcks;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface rmiiI
    extends 
        Remote 
{ 
    String sayHello ()
        throws
            RemoteException; 

    int    sendBytes (byte[] x) 
        throws
            RemoteException;

    Object sendOneObject (Object x)
        throws
            RemoteException,
            rmiiMyException;

    Object sendTwoObjects (Object x, Object y)
        throws
            RemoteException;

    String makeColocatedCallFromServant ()
        throws
            RemoteException;

    String colocatedCallFromServant (String a)
        throws
            RemoteException,
            Exception;

    String throwThreadDeathInServant (String a)
        throws
            RemoteException,
            ThreadDeath;

    Object returnObjectFromServer (boolean isSerializable)
        throws
            RemoteException;
}

// End of file.

