/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.fvd;

public class ServantImpl implements Servant {

    public String ping(String s) throws java.rmi.RemoteException {
        return "ServantImpl:"+s;
    }


    public long send(ParentClass value) throws java.rmi.RemoteException {
        return value.getTotal();
    }

    public ParentClass receiveMismatch(ParentClass value) 
        throws java.rmi.RemoteException {
        try{
            return (ParentClass)Class.forName("javax.rmi.download.values.ClientA").newInstance();
        }
        catch(Throwable t){
            return null;
        }
    }

    public ParentClass receiveABC(ParentClass value) 
        throws java.rmi.RemoteException {
        try{
            return (ParentClass)Class.forName("javax.rmi.download.values.ClassC").newInstance();
        }
        catch(Throwable t){
            return null;
        }
    }

    public ParentClass receiveAE(ParentClass value) 
        throws java.rmi.RemoteException {
        try{
            return (ParentClass)Class.forName("javax.rmi.download.values.ClassE").newInstance();
        }
        catch(Throwable t){
            return null;
        }
    }


}
