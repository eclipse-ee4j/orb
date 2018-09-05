/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.strm2;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import java.io.*;
import javax.naming.*;

public class TesterImpl extends PortableRemoteObject implements Tester
{
    public TesterImpl() throws RemoteException, NamingException {
        super();
    }

    public String getDescription() {
        return createTestObject().getDescription();
    }

    public Testable createTestObject() {
        try {
            Class testObjectClass = Class.forName("TestObject");
            return (Testable)testObjectClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't create TestObject", ex);
        }
    }

    public Testable verify(Testable input) throws RemoteException {

        try {

            System.out.println("TesterImpl w/ version "
                               + getDescription()
                               + " received from client w/ version "
                               + input.getDescription());
            System.out.println(input.toString());

        } catch (RuntimeException rt) {
            rt.printStackTrace();
            throw new RemoteException("Problem in verify", rt.getCause());
        }

        return input;
    }
}

