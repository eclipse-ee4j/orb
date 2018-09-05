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

package omega;

public class AB implements A, B {

    public void a() {}
    public void b() {}

    // public void a() throws java.rmi.RemoteException {}
    // public void b() throws java.rmi.RemoteException {}

    public static void main(String argv[]) {
        System.out.println("hi");
        AB it = new AB();
        it.a();
    }
}
