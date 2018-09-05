/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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

