/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
