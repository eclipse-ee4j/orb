/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.msgtypes;

import java.rmi.RemoteException ;

public interface FragmentTester extends java.rmi.Remote
{
    public byte[] verifyTransmission(byte array[])
        throws RemoteException, BadArrayException;
    public boolean verifyOutcome() throws RemoteException;
    public java.lang.Object testFragmentedReply(boolean isSerializable) 
        throws RemoteException;

    // used for header padding test only. The following two method names differ
    // just by one character. This ensures that the request header for atleast
    // one of these methods would not be naturally aligned on an 8-octet
    // boundary.
    public void fooA(char ch) throws RemoteException;
    public void fooB(char ch) throws RemoteException;
}
