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
