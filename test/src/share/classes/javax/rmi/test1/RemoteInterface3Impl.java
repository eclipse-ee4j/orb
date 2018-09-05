/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.test1;

public  class RemoteInterface3Impl implements RemoteInterface3 {

    public String EchoRemoteInterface1() throws java.rmi.RemoteException {
        return "EchoRemoteInterface1";
    }

    public String EchoRemoteInterface2() throws java.rmi.RemoteException {
        return "EchoRemoteInterface2";
    }

    public String EchoRemoteInterface3() throws java.rmi.RemoteException {
        return "EchoRemoteInterface3";
    }
}
