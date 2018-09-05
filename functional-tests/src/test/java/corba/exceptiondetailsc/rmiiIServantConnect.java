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
// Last Modified : 2003 Jul 28 (Mon) 09:27:11 by Harold Carr.
//

package corba.exceptiondetailsc;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.FREE_MEM;
import corba.hcks.U;

public class rmiiIServantConnect
    extends 
        PortableRemoteObject
    implements 
        rmiiI
{
    public static final String baseMsg = rmiiIServantConnect.class.getName();

    public rmiiIServantConnect ()
        throws
            RemoteException 
    {
        super();
    }

    public void raiseSystemException(String x)
        throws
            RemoteException
    {
        throw new FREE_MEM(x);
    }

    public void raiseUserException(String x)
        throws
            RemoteException,
            rmiiException
    {
        throw new rmiiException(x);
    }

    public void raiseRuntimeException(String x)
        throws
            RemoteException
    {
        throw new RuntimeException(x);
    }
}

// End of file.

