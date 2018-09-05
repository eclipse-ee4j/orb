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

package javax.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
// import com.sun.corba.ee.impl.io.FVDCodeBaseImpl;
import javax.rmi.CORBA.*;
import com.sun.org.omg.SendingContext.CodeBase;
import rmic.OnlyRemoteServant;

public class ServantInner implements SInner {
    public ServantInner () throws RemoteException {
    }
    
    public SInner echo (SInner in) throws RemoteException {
        return in;
    }
    
    public CodeBase getCodeBase() throws RemoteException {
        javax.rmi.CORBA.ValueHandler vh = Util.createValueHandler();
        return (CodeBase) vh.getRunTimeCodeBase();
    }
    
    public Remote getOnlyRemote() throws RemoteException {
        return new OnlyRemoteServant();
    }
    
}
