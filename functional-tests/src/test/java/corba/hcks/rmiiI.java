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

