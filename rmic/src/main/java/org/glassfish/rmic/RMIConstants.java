/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic;

import org.glassfish.rmic.tools.java.Identifier;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public interface RMIConstants extends org.glassfish.rmic.Constants {

    /*
     * identifiers for RMI classes referenced by rmic
     */
    public static final Identifier idRemoteObject = Identifier.lookup("java.rmi.server.RemoteObject");
    public static final Identifier idRemoteStub = Identifier.lookup("java.rmi.server.RemoteStub");
    public static final Identifier idRemoteRef = Identifier.lookup("java.rmi.server.RemoteRef");
    public static final Identifier idOperation = Identifier.lookup("java.rmi.server.Operation");
    public static final Identifier idSkeleton = Identifier.lookup("java.rmi.server.Skeleton");
    public static final Identifier idSkeletonMismatchException = Identifier.lookup("java.rmi.server.SkeletonMismatchException");
    public static final Identifier idRemoteCall = Identifier.lookup("java.rmi.server.RemoteCall");
    public static final Identifier idMarshalException = Identifier.lookup("java.rmi.MarshalException");
    public static final Identifier idUnmarshalException = Identifier.lookup("java.rmi.UnmarshalException");
    public static final Identifier idUnexpectedException = Identifier.lookup("java.rmi.UnexpectedException");

    /*
     * stub protocol versions
     */
    public static final int STUB_VERSION_1_1 = 1;
    public static final int STUB_VERSION_FAT = 2;
    public static final int STUB_VERSION_1_2 = 3;

    /** serialVersionUID for all stubs that can use 1.2 protocol */
    public static final long STUB_SERIAL_VERSION_UID = 2;

    /** version number used to seed interface hash computation */
    public static final int INTERFACE_HASH_STUB_VERSION = 1;
}
