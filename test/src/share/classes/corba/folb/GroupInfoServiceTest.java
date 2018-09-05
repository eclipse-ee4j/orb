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
// Created       : 2005 Jun 13 (Mon) 11:04:09 by Harold Carr.
// Last Modified : 2005 Sep 23 (Fri) 12:11:36 by Harold Carr.
//

package corba.folb;

import java.util.List;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

/**
 * @author Harold Carr
 */
public interface GroupInfoServiceTest
    extends Remote
{
    public boolean addInstance(String x)
        throws RemoteException;

    public boolean removeInstance(String x)
        throws RemoteException;

    public boolean addAcceptor(String x)
        throws RemoteException;

    public boolean removeAcceptorAndConnections(String x)
        throws RemoteException;

    public void doThreadDump()
        throws RemoteException;
}

// End of file.
