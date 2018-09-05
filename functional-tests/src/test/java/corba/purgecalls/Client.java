/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2002 Jan 17 (Thu) 14:19:20 by Harold Carr.
// Last Modified : 2003 Mar 12 (Wed) 09:55:39 by Harold Carr.
//

package corba.purgecalls;

import com.sun.corba.ee.spi.legacy.connection.Connection;
import corba.framework.Controller;
import corba.hcks.U;
import java.net.Socket;
import java.util.Properties;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static ServerSide rServerSide;

    // The client interceptor sets this.
    public static Connection requestConnection;

    public static Throwable noExceptionExpected;

    public static void main(String av[])
    {
        try {
            Properties props = new Properties();
            props.put(U.ORBInitializerClass + "." + "corba.purgecalls.ClientORBInitializer", "ignored");
            orb = ORB.init(av, props);

            
            rServerSide =  
                ServerSideHelper.narrow(U.resolve(Server.ServerSide, orb));

            runTests();

            // Wait for other thread to do its thing.
            Thread.sleep(2000);

            U.sop("Test complete.");

        } catch (java.io.IOException e) {

            U.sop(main + " Expected: " + e);

        } catch (Throwable t) {
            U.sopUnexpectedException(main + " : ", t);
            System.exit(1);
        }

        if (noExceptionExpected == null) {
            U.normalExit(main);
            // Do not explicitly exit to test that no non-daemon threads
            // are hanging.
            //System.exit(Controller.SUCCESS);
        } else {
            U.sopUnexpectedException(main + " : ", noExceptionExpected);
            System.exit(1);
        }
    }

    public static void runTests()
        throws
            Exception
    {
        CallThread CallThread = new CallThread();
        CallThread.start();
    
        Thread.sleep(5000);

        Socket socket = requestConnection.getSocket();
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.getInputStream().close();
        socket.getOutputStream().close();
        socket.close();
    }
}

class CallThread extends Thread
{
    CallThread ()
    {
    }
    public void run ()
    {
        try {
            Client.rServerSide.neverReturns();
        } catch (COMM_FAILURE e) {
            U.sop("Expected: " + e);
        } catch (Throwable t) {
            Client.noExceptionExpected = t;
            t.printStackTrace();
        }
    }
}

// End of file.
