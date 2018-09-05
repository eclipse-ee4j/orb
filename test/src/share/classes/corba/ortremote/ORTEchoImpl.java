/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.ortremote ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableServer.POA ;
import com.sun.corba.ee.spi.oa.ObjectAdapter ;
import javax.rmi.PortableRemoteObject ;

public class ORTEchoImpl extends PortableRemoteObject implements ORTEcho
{
    private POA poa ;

    public ORTEchoImpl( POA poa ) throws java.rmi.RemoteException
    {
        this.poa = poa ;
    }

    public ObjectReferenceTemplate getORT() throws RemoteException 
    {
        ObjectAdapter oa = (ObjectAdapter)poa ;
        return oa.getAdapterTemplate() ;
    }

    public ObjectReferenceFactory getORF() throws RemoteException 
    {
        ObjectAdapter oa = (ObjectAdapter)poa ;
        return oa.getCurrentFactory() ;
    }
} 

