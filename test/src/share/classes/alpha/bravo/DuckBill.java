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

package alpha.bravo;

public interface DuckBill extends java.rmi.Remote {

    public void hello()                  throws java.rmi.RemoteException;
    public void hello( int x,B y,int z ) throws java.rmi.RemoteException;
    public void hello( int z[] )         throws java.rmi.RemoteException;
    public void hello( int z )           throws java.rmi.RemoteException;
    public void hello( long z )          throws java.rmi.RemoteException;

    public void jack() throws java.rmi.RemoteException;
    public void Jack() throws java.rmi.RemoteException;
    public void jAcK() throws java.rmi.RemoteException;

}
