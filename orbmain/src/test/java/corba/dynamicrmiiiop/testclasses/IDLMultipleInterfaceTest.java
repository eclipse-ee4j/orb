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

import java.rmi.RemoteException;

public class IDLMultipleInterfaceTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods. See TestIDLNameTranslator for sorting details.
    //
    public static final String[] IDL_NAMES = { "A1", "CB", "CC", "CD" };

    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface first extends java.rmi.Remote {
        void A1() throws RemoteException;

        void CC(int a) throws RemoteException;
    }

    public interface second extends java.rmi.Remote {
        void CB(int a) throws RemoteException;

        void CD(int a) throws RemoteException;
    }
}
