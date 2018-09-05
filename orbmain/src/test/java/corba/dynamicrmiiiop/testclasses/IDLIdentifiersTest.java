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

public class IDLIdentifiersTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    static final String[] IDL_NAMES = {   

        "ABCDEFGHIJKLMNOPQRSTUVWXYZ0", 
        "aU0024U0024U0024U0024",
        "a0123456789", 
        "a_", 
        "a_0", 
        "abcdefghijklmnopqrstuvwxyz",

        "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF",
        "\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF",

        "U0100U0101U0170\u00FF\u00FEU0024",
        "U0393U0394U0398U03B6", 
        "U0393U0394U0398U03B6_abc",
        "U04E3U04F3U04D5U04E6", 
        "U05E9U05EAU05F0U05E5U05DE", 
        "U13E0U13F0U13F2U13F4",
        "U50AFU50BFU50EF______",
    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLIdentifiers extends java.rmi.Remote {

        void ABCDEFGHIJKLMNOPQRSTUVWXYZ0() throws java.rmi.RemoteException;

        void a$$$$() throws java.rmi.RemoteException;
        void a0123456789() throws java.rmi.RemoteException;
        void a_() throws java.rmi.RemoteException;
        void a_0() throws java.rmi.RemoteException;
        void abcdefghijklmnopqrstuvwxyz() throws java.rmi.RemoteException;
                
        // note : no 00D7 (x) 
        void \u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF() throws java.rmi.RemoteException;

        // note : no 00F7 (/)
        void \u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF() throws java.rmi.RemoteException;

        // latin extended-A, right outside the IDL range
        void \u0100\u0101\u0170\u00FF\u00FE\u0024() throws java.rmi.RemoteException;
        
        // some greek letters
        void \u0393\u0394\u0398\u03B6() throws java.rmi.RemoteException;
        void \u0393\u0394\u0398\u03B6_abc() throws java.rmi.RemoteException;

        // some Cyrillic letters
        void \u04e3\u04f3\u04D5\u04E6() throws java.rmi.RemoteException;

        // some hebrew letters
        void \u05E9\u05EA\u05f0\u05E5\u05DE() throws java.rmi.RemoteException;

        // some Cherokee letters
        void \u13E0\u13F0\u13f2\u13F4() throws java.rmi.RemoteException;

        // some CJK Unified Ideographs
        void \u50AF\u50bF\u50Ef______() throws java.rmi.RemoteException;
                
    }

}
