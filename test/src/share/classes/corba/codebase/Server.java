/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package corba.codebase;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

import java.io.*;

public class Server extends PortableRemoteObject implements Tester
{
    public Server() throws java.rmi.RemoteException {
    }

    public void printMessage(String message)
    {
        System.out.println(message);
    }

    public Object requestValue() 
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        Class valueClass = Class.forName("TestValue");
        return valueClass.newInstance();
    }

    public String processValue(Object value)
    {
        Class valueClass = value.getClass();

        System.out.println("Received instance of: " + valueClass.getName());

        return valueClass.getName();
    }

    // This is just helpful for debugging to see whether or not the
    // server has access to these files.
    public static void tryLoadingClasses()
    {
        System.out.println("java.rmi.server.codebase = "
                           + System.getProperty("java.rmi.server.codebase"));

        try {
            System.out.println("Trying to load the stub class");
            Class stub = Class.forName("corba.codebase._Tester_Stub");
            System.out.println("Server has access to the stub");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Server doesn't have access to the stub");
        }

        try {
            System.out.println("Trying to load the tie class");
            Class tie = Class.forName("corba.codebase._Server_Tie");
            System.out.println("Server has access to the tie");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Server doesn't have access to the tie");
        }

        try {
            System.out.println("Trying to load the TestValue class");
            Class testValue = Class.forName("TestValue");
            System.out.println("Server has access to the TestValue class");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Server doesn't have access to TestValue");
        }
    }

    private static InitialContext rootContext ;

    public static void main(String[] args) {
        try {
            System.setSecurityManager(new NoSecurityManager());

            Server.tryLoadingClasses();

            rootContext = new InitialContext();
            Server p = new Server();

            rootContext.rebind("Tester", p);
            System.out.println("Server is ready.");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}

