/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

package corba.requestpartitioning;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;

import corba.hcks.U;
import java.rmi.RemoteException;
import java.util.Properties;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class Client
{
    protected final static int stringSize = 10000;
    protected final static String stringOf36 =
           "abcdefghijklmnopqrstuvwxyz0123456789";
    protected String reallyReallyBigString = null;
    protected Tester itsTester = null;
    protected ORB itsOrb = null;

    public Client(String[] args) throws Exception {

        Properties props = System.getProperties();

        itsOrb = (ORB)org.omg.CORBA.ORB.init(args, props);

        initializeReallyBigString();
    }

    protected void initializeReallyBigString() {
        StringBuilder sb = new StringBuilder(stringSize);
        int index = 0;
        final int lengthOfStr = stringOf36.length();
        for (int i = 0; i < stringSize; i++) {
            index = i % lengthOfStr;
            sb.append(stringOf36.charAt(index));
        }
        reallyReallyBigString = sb.toString();
    }

    protected void printError(int myPoolId, int remotePoolId)
            throws Exception {
        StringBuilder error =  new StringBuilder(80);
        error.append("FAILED: client requested thread pool id (");
        error.append(myPoolId);
        error.append(") not executed on expected server thread pool id (");
        error.append(remotePoolId).append(")");
        U.sop(error.toString());
        throw new Exception(error.toString());
    }

    protected void runTest() throws RemoteException, Exception {

        U.sop("Getting name service...");
        org.omg.CORBA.Object objRef =
            itsOrb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        U.sop("Got name service.");

        int expectedPoolId;
        int returnedPoolId;
        for (int i = 0; i < TestThreadPoolManager.NUMBER_OF_THREAD_POOLS_TO_CREATE; i++)
        {
            String name = "Tester" + i;
            U.sop("Finding, looking up & narrowing " + name + " ...");
            itsTester = TesterHelper.narrow(ncRef.resolve_str(name));
            U.sop("Got " + name + " ...");

            U.sop("Testing thread pool id (" + i + ") usage...");
            expectedPoolId = i;
            returnedPoolId =
                itsTester.getThreadPoolIdForThisRequest(reallyReallyBigString);
            if (expectedPoolId != returnedPoolId) {
                printError(expectedPoolId, returnedPoolId);
            }
            U.sop("Thead pool (" + i + ") test PASSED.");
        }

        String defaultname = "DefaultTester";
        U.sop("Finding, looking up & narrowing " + defaultname + " ...");
        itsTester = TesterHelper.narrow(ncRef.resolve_str(defaultname));
        U.sop("Got " + defaultname + " ...");

        U.sop("Testing DEFAULT thread pool usage...");
        expectedPoolId = 0;
        returnedPoolId =
            itsTester.getThreadPoolIdForThisRequest(reallyReallyBigString);
        if (expectedPoolId != returnedPoolId) {
            printError(expectedPoolId, returnedPoolId);
        }
        U.sop("Default thead pool test PASSED.");

        U.sop("All thread pool tests PASSED.");
    }

    public static void main(String args[]) {
        try {

            U.sop("Beginning test...");

            Client client = new Client(args);
            client.runTest();

            U.sop("Test finished successfully...");

        } catch (Throwable t) {
            U.sop("Unexpected throwable...");
            t.printStackTrace();
            System.exit(1);
        }
    }
}

