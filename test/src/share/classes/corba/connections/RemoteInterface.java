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
// Created       : 2003 Sep 27 (Sat) 15:37:19 by Harold Carr.
// Last Modified : 2003 Sep 27 (Sat) 22:14:27 by Harold Carr.
//

package corba.connections;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface 
    extends 
        Remote 
{
    public Struct[] method(Struct[] in)
        throws RemoteException;

    public void block()
        throws RemoteException;

    public void resume()
        throws RemoteException;

    public String testMonitoring()
        throws RemoteException;
}

// End of file.
