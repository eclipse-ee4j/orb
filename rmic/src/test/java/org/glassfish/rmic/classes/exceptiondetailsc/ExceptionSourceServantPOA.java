/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package org.glassfish.rmic.classes.exceptiondetailsc;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.FREE_MEM;

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

