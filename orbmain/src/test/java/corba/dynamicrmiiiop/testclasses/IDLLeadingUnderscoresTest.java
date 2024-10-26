/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

public class IDLLeadingUnderscoresTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.
    //
    static final String[] IDL_NAMES = {

        "J_0",
        "J_J_",
        "J__",
        "J______________________________Z",
        "J__a",
        "J_a",
        "J_jj"
    };

    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLLeadingUnderscores extends java.rmi.Remote {

        void _0() throws java.rmi.RemoteException;

        void _J_() throws java.rmi.RemoteException;

        void __() throws java.rmi.RemoteException;

        void ______________________________Z() throws java.rmi.RemoteException;

        void __a() throws java.rmi.RemoteException;

        void _a() throws java.rmi.RemoteException;

        void _jj() throws java.rmi.RemoteException;

    }

}
