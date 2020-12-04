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

public class Client 
{
    // Tests stub/tie downloading
    public static void testDownloading(Tester tester) throws Exception
    {
        tester.printMessage("Simple message test with downloading");

        System.out.println("PASSED");
    }

    public static void testServerValueDownloading(Tester tester) 
        throws Exception
    {
        System.out.println("Testing server value downloading");

        Class testValueClass = Class.forName("TestValue");
        
        String result = tester.processValue(testValueClass.newInstance());

        if (!testValueClass.getName().equals(result))
            throw new Exception("Server didn't receive the right value class.  Got: "
                                + result);

        System.out.println("PASSED");
    }

    public static void testClientValueDownloading(Tester tester)
        throws Exception
    {
        System.out.println("Testing client value downloading");

        Object res = tester.requestValue();

        if (!res.getClass().getName().equals("TestValue"))
            throw new Exception("Client didn't receive a TestValue, got: "
                                + res.getClass().getName());

        System.out.println("PASSED");
    }

    // This is just helpful for debugging to see whether or not the
    // client has access to these files.
    public static void tryLoadingClasses()
    {
        System.out.println("java.rmi.server.codebase = "
                           + System.getProperty("java.rmi.server.codebase"));

        try {
            System.out.println("Trying to load the stub class");
            Class stub = Class.forName("corba.codebase._Tester_Stub");
            System.out.println("Client has access to the stub");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Client doesn't have access to the stub");
        }

        try {
            System.out.println("Trying to load the tie class");
            Class tie = Class.forName("corba.codebase._Server_Tie");
            System.out.println("Client has access to the tie");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Client doesn't have access to the tie");
        }

        try {
            System.out.println("Trying to load the TestValue class");
            Class testValue = Class.forName("TestValue");
            System.out.println("Client has access to the TestValue class");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Client doesn't have access to TestValue");
        }
    }

    private static InitialContext rootContext ;

    public static void main(String args[])
    {
        try {
            System.setSecurityManager(new NoSecurityManager());

            Client.tryLoadingClasses();

            rootContext = new InitialContext();
            Tester tester 
                = (Tester)PortableRemoteObject.narrow(rootContext.lookup("Tester"), Tester.class);
            
            System.out.println("Testing downloading.  Server downloading? "
                               + System.getProperty(Tester.SERVER_DOWNLOADING_FLAG));

            Client.testDownloading(tester);

            if (System.getProperty(Tester.SERVER_DOWNLOADING_FLAG) != null) {
                // The server is downloading code.  Try to send a TestValue
                // instance.
                Client.testServerValueDownloading(tester);
            } else {
                // The client is downloading code.  Try to receive a TestValue
                // instance.
                Client.testClientValueDownloading(tester);
            }

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
