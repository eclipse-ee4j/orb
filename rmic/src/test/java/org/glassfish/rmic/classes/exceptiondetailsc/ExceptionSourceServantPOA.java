/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.exceptiondetailsc;

import org.omg.CORBA.FREE_MEM;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class ExceptionSourceServantPOA extends PortableRemoteObject implements ExceptionSource {
    public static final String baseMsg = ExceptionSourceServantPOA.class.getName();

    public ExceptionSourceServantPOA() throws RemoteException {
        // DO NOT CALL SUPER - that would connect the object.
    }

    public void raiseSystemException(String x) throws RemoteException {
        throw new FREE_MEM(x);
    }

    public void raiseUserException(String x) throws RemoteException, RmiIException {
        throw new RmiIException(x);
    }

    public void raiseRuntimeException(String x) throws RemoteException {
        throw new RuntimeException(x);
    }
}

