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

package javax.rmi;

import com.sun.corba.ee.spi.JndiConstants;

import javax.naming.InitialContext;
import java.rmi.RemoteException;

public class PROImpl extends PortableRemoteObject implements PROHello {

    public PROImpl() throws RemoteException {
        super();
    }

    public String sayHello() throws RemoteException {
        return HELLO;
    }

    public Dog getDogValue() throws RemoteException {
        return new DogImpl("Bow wow!");
    }

    public Dog getDogServer() throws RemoteException {
        return new DogServer("Yip Yip Yip!");
    }

    public void unexport() throws RemoteException {
        PortableRemoteObject.unexportObject(this);
    }

    private static InitialContext context;

    public static void main(String[] args) {

        // args[0] == 'iiop' || 'jrmp'
        // args[1] == publishName

        try {

            if (args[0].equalsIgnoreCase("iiop")) {
                System.getProperties().put("java.naming.factory.initial", JndiConstants.COSNAMING_CONTEXT_FACTORY);
            } else if (args[0].equalsIgnoreCase("jrmp")) {
                System.getProperties().put("java.naming.factory.initial", JndiConstants.REGISTRY_CONTEXT_FACTORY);
            }

            context = new InitialContext();
            context.rebind(args[1], new PROImpl());

        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}
