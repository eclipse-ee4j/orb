/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.copyobjectpolicy;

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;

public class EchoImpl extends PortableRemoteObject implements Echo  
{
    public EchoImpl() throws RemoteException
    {
    }

    public Object echo( Object invalue) throws RemoteException
    {
        return invalue;
    }
}
