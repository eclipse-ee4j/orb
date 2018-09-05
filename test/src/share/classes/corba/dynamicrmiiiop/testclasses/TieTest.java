/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop.testclasses ; 

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import java.util.Map ;

public interface TieTest extends Remote 
{
    void hasAByteArray( byte[] arg ) throws RemoteException ;

    int throwsException( int arg ) throws Exception, RemoteException ;

    int throwsDeclaredException( int arg ) throws MyApplicationExceptionBase,
        RemoteException ;

    int throwsSystemException( int arg ) throws RemoteException ;

    int throwsJavaException( int arg ) throws RemoteException ;

    String m0() throws RemoteException ;

    String m1( String another ) throws RemoteException ;

    String m2( Map map, String key ) throws RemoteException ;

    void vm0() throws RemoteException ;

    void vm1( String another ) throws RemoteException ;

    void vm2( Map map, String key ) throws RemoteException ;
}
