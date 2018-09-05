/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.islocal;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class MessageBuilderServantPOA extends PortableRemoteObject implements MessageBuilder {
    private static final String baseMsg = MessageBuilderServantPOA.class.getName();

    public MessageBuilderServantPOA() throws RemoteException {
        // DO NOT CALL SUPER - that would connect the object.
    }

    public String m(String x) {
        String result = x + baseMsg;
        System.out.println(baseMsg);
        return result;
    }
}
