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

public class IDLLeadingUnderscoresTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods. See TestIDLNameTranslator for sorting details.
    //
    static final String[] IDL_NAMES = {

            "J_", "J_0", "J_J_", "J__", "J______________________________Z", "J__a", "J_a", "J_jj" };

    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLLeadingUnderscores extends java.rmi.Remote {

        void _() throws java.rmi.RemoteException;

        void _0() throws java.rmi.RemoteException;

        void _J_() throws java.rmi.RemoteException;

        void __() throws java.rmi.RemoteException;

        void ______________________________Z() throws java.rmi.RemoteException;

        void __a() throws java.rmi.RemoteException;

        void _a() throws java.rmi.RemoteException;

        void _jj() throws java.rmi.RemoteException;

    }

}
