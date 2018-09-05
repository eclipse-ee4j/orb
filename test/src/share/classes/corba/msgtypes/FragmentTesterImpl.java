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
import javax.rmi.PortableRemoteObject;

public class FragmentTesterImpl extends PortableRemoteObject implements FragmentTester
{
    Server.InterceptorImpl interceptor = null;

    public FragmentTesterImpl(Server.InterceptorImpl interceptor) throws RemoteException
    {
        super();
        this.interceptor = interceptor;
    }

    public byte[] verifyTransmission(byte array[]) throws BadArrayException
    {
        if (array == null)
            throw new BadArrayException("Array is null");

        if (array.length % 4 != 0)
            throw new BadArrayException("Invalid array length: " + array.length);

        System.out.println("Array length = " + array.length);

        int i = 0;

        do {

            System.out.println("" + i + ": ");

            for (int check = 0; check < 4; check++) {

                System.out.print("" + array[i] + " ");

                if (array[i++] != check) {
                    throw new BadArrayException("Bad array at index " + i
                                                + " value: " + array[i]);
                }
            }

            System.out.println();


        } while (i < array.length);

        return array;
    }

    public boolean verifyOutcome() {
        return this.interceptor.isBalanced();
    }

    public java.lang.Object testFragmentedReply(boolean isSerializable) 
            throws RemoteException {

        if (isSerializable) {
            return new java.lang.Object();
        }
            
        return new java.io.Serializable() {};             
    }
    
    // used for header padding test only. The following two method names differ
    // just by one character. This ensures that the request header for atleast
    // one of these methods would not be naturally aligned on an 8-octet
    // boundary.
    public void fooA(char ch) {}
    public void fooB(char ch) {}
}
