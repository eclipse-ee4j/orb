/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package sun.rmi.rmic;

import sun.tools.java.Identifier;

public interface RMIConstants extends sun.rmi.rmic.Constants {

    /*
     * identifiers for RMI classes referenced by rmic
     */
    public static final Identifier idRemoteObject =
        Identifier.lookup("java.rmi.server.RemoteObject");
    public static final Identifier idRemoteStub =
        Identifier.lookup("java.rmi.server.RemoteStub");
    public static final Identifier idRemoteRef =
        Identifier.lookup("java.rmi.server.RemoteRef");
    public static final Identifier idOperation =
        Identifier.lookup("java.rmi.server.Operation");
    public static final Identifier idSkeleton =
        Identifier.lookup("java.rmi.server.Skeleton");
    public static final Identifier idSkeletonMismatchException =
        Identifier.lookup("java.rmi.server.SkeletonMismatchException");
    public static final Identifier idRemoteCall =
        Identifier.lookup("java.rmi.server.RemoteCall");
    public static final Identifier idMarshalException =
        Identifier.lookup("java.rmi.MarshalException");
    public static final Identifier idUnmarshalException =
        Identifier.lookup("java.rmi.UnmarshalException");
    public static final Identifier idUnexpectedException =
        Identifier.lookup("java.rmi.UnexpectedException");

    /*
     * stub protocol versions
     */
    public static final int STUB_VERSION_1_1  = 1;
    public static final int STUB_VERSION_FAT  = 2;
    public static final int STUB_VERSION_1_2  = 3;

    /** serialVersionUID for all stubs that can use 1.2 protocol */
    public static final long STUB_SERIAL_VERSION_UID = 2;

    /** version number used to seed interface hash computation */
    public static final int INTERFACE_HASH_STUB_VERSION = 1;
}
