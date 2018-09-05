/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.giopheaderpadding;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class FooServantPOA extends PortableRemoteObject implements Foo {

    public FooServantPOA() throws RemoteException {
        // DO NOT CALL SUPER - that would connect the object.
    }

    public byte fooA(byte x) {
        System.out.println(x + "");
        return x;
    }

    public void fooB() {
    }
}
