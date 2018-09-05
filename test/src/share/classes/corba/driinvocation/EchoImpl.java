/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.driinvocation;

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;
import org.omg.CORBA.BAD_PARAM ;

public class EchoImpl implements Echo 
{
    public void throwsSystemException() throws RemoteException 
    {
        throw new BAD_PARAM() ;
    }

    public void throwsUnknownException() throws RemoteException 
    {
        throw new IllegalArgumentException() ;
    }

    public int twice( int arg ) throws RemoteException 
    {
        return 2*arg ;
    }
}
