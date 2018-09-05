/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrstreams;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

public interface GraphProcessor extends java.rmi.Remote 
{
    public void process(Node graphStart) 
        throws RemoteException, InvalidGraphException;

    public Object verifyTransmission(Object input)
        throws RemoteException;

    public boolean receiveObject(Object input)
        throws RemoteException;
}
