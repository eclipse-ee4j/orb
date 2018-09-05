/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop.testclasses;

public class IDLCaseSensitivityTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    public static final String[] IDL_NAMES = {   
        "ABCDEFGHIJKLmNOPQRSTUVWXYzA_0_1_2_3_4_5_6_7_8_9_10_11_13_14_15_16_17_18_19_20_21_22_23_24_26",
        "B_0", 
        "JACK_0_1_2_3",
        "JACKY",
        "Jack_0",
        "a",
        "abcdefghijklMnopqrstuvwxyza_12",
        "abcdefghijklmnopqrstuvwxyzA_26",
        "b_",
        "b__",
        "jAcK_1_3",
        "jack_"
    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLCaseSensitivity extends java.rmi.Remote {
        String ABCDEFGHIJKLmNOPQRSTUVWXYzA(int a) 
            throws java.rmi.RemoteException;

        void B() throws java.rmi.RemoteException;

        boolean JACK() throws java.rmi.RemoteException;
        void JACKY() throws java.rmi.RemoteException;
        void Jack() throws java.rmi.RemoteException;

        void a() throws java.rmi.RemoteException;

        void abcdefghijklMnopqrstuvwxyza() throws java.rmi.RemoteException;
        void abcdefghijklmnopqrstuvwxyzA() throws java.rmi.RemoteException;

        void b() throws java.rmi.RemoteException;

        void b__() throws java.rmi.RemoteException;


        int jAcK() throws java.rmi.RemoteException;
        void jack() throws java.rmi.RemoteException;        
        
    }

}
