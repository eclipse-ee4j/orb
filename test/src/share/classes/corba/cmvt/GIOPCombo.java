/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cmvt;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import org.omg.CORBA.*;

public interface GIOPCombo extends Remote {
    public int sayHello(int value) 
        throws RemoteException ;
    public String echo(String s) 
        throws java.rmi.RemoteException ;
    public java.util.Vector echo(java.util.Vector vector) 
        throws java.rmi.RemoteException ;
    public java.util.Hashtable echo(java.util.Hashtable ht) 
        throws java.rmi.RemoteException ;
    public CustomMarshalledValueType echo(CustomMarshalledValueType cmvt) 
        throws java.rmi.RemoteException ;
    public void throwError( Error it) 
        throws java.rmi.RemoteException ;
}
