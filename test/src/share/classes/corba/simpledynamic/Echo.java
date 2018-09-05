/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.simpledynamic;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import corba.misc.BuckPasserAL  ;
import corba.misc.BuckPasserV  ;

public interface Echo extends Remote {
    String sayHello( Object obj ) throws RemoteException ;

    Echo say( Echo obj ) throws RemoteException ;

    String name() throws RemoteException ;

    Object testExceptionContext() throws RemoteException ;
    /** Can send enough data to force fragmentation
     */
    int[] echo( int[] arg ) throws RemoteException ;

    Object echo( Object obj ) throws RemoteException ;

    BuckPasserAL echo( BuckPasserAL arg ) throws RemoteException ;
    BuckPasserV echo( BuckPasserV arg ) throws RemoteException ;
    BuckPasserVectorOriginal echo( BuckPasserVectorOriginal arg ) throws RemoteException ;
}


