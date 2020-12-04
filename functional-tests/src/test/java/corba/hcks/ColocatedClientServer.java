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

//
// Created       : 2000 Jun 29 (Thu) 14:16:17 by Harold Carr.
// Last Modified : 2002 May 03 (Fri) 13:33:50 by Harold Carr.
//

package corba.hcks;

import javax.naming.InitialContext;
import org.omg.CORBA.ORB;

public class ColocatedClientServer 
{
    public static final String baseMsg = ColocatedClientServer.class.getName();
    public static final String main = baseMsg + ".main";

    // REVISIT: FRAMEWORK DEVELOPMENT
    // REMOVE THIS LATER.
    // Necessary so calls not going through locals do not hang
    // until I implement the reader thread/work split.
    public static int fragmentSize = -1;
    //public static int fragmentSize = C.DEFAULT_FRAGMENT_SIZE;

    public static ORB orb;
    public static InitialContext initialContext;
    public static boolean isColocated = false;
    public static java.lang.Object signal = new java.lang.Object();

    public static void main (String args[])
    {

        Client.testName = ColocatedClientServer.class.getName() ;
        isColocated = true; // Used by Client and Server.

        try {
            // Share an ORB between a client and server.
            // So ClientDelegate.isLocal currently succeeds.

            orb = C.createORB(args, fragmentSize);
            Server.orb = orb;
            Client.orb = orb;
            
            // Share a naming context between client and server
            // so StubAdapter.isLocal is true.

            // Use the same ORB which has interceptor properties set.
            initialContext = C.createInitialContext(orb);
            Server.initialContext = initialContext;
            Client.initialContext = initialContext;
            
            ServerThread ServerThread = new ServerThread(args);
            ServerThread.start();
            synchronized (signal) {
                try {
                    signal.wait();
                } catch (InterruptedException e) {
                    ;
                }
            }
            Client.main(args);
        } catch (Exception e) {
            U.sopUnexpectedException(main, e);
        }
    }
}

class ServerThread extends Thread
{
    String[] args;
    ServerThread (String[] args)
    {
        this.args = args;
    }
    public void run ()
    {
        Server.main(args);
    }
}

// End of file.
