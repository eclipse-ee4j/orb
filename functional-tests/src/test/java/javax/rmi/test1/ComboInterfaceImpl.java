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

package javax.rmi.test1;

public class ComboInterfaceImpl implements ComboInterface {

    public String EchoRemoteInterface1() throws java.rmi.RemoteException {
        return "EchoRemoteInterface1";
    }

    public String EchoRemoteInterface2() throws java.rmi.RemoteException {
        return "EchoRemoteInterface2";
    }

    public String EchoRemoteInterface3() throws java.rmi.RemoteException {
        return "EchoRemoteInterface3";
    }

    public String EchoSingleRemoteInterface() throws java.rmi.RemoteException {
        return "EchoSingleRemoteInterface";
    }
}
