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

package omega;

public interface Thrower extends java.rmi.Remote {

    void doThrowFruitbat() throws FruitbatException, java.rmi.RemoteException;
    FruitbatException getLastException() throws java.rmi.RemoteException;

    void doThrowClimax() throws Climax, java.rmi.RemoteException;

    void doThrowUser() throws org.omg.CORBA.UserException, java.rmi.RemoteException;

    //Climax getaClimax() throws java.rmi.RemoteException;
    //void hurl() throws NCRemoteException, java.rmi.RemoteException;
    //NCRemoteException getHurled() throws java.rmi.RemoteException;

}
