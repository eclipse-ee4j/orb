/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package performance.simpleperf;

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;
import org.omg.CORBA.ORB ;
import java.io.File ;
import java.io.RandomAccessFile ;
import org.omg.PortableServer.POA ;

public class counterImpl extends PortableRemoteObject implements counterIF  
{
    private int value ;

    public counterImpl() throws RemoteException
    {
        value = 0 ;
    }

    public synchronized long increment(long invalue) throws RemoteException
    {
        value += invalue;
     
        return value;
    }
}

