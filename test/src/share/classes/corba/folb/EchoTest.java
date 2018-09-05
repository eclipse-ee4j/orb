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
// Created       : 2005 Jun 09 (Thu) 14:44:09 by Harold Carr.
// Last Modified : 2005 Sep 29 (Thu) 22:15:12 by Harold Carr.
//

package corba.folb;

import java.util.List;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

/**
 * @author Harold Carr
 */
public interface EchoTest extends Remote {
    public String echo(String x)
        throws RemoteException;

    public void neverReturns()
        throws RemoteException;
}

// End of file.
