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

package corba.poamanager;

import HelloStuff.Hello;
import HelloStuff.HelloHelper;
import Util.CreationMethods;
import Util.GenericFactory;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.logging.POASystemException ;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.SystemException;

// Will this test exit with value 1 when errors in WorkerThreads?  REVISIT

public class HelloClient {
    private static final int N_LOOPS = 10;
    private static final ORBUtilSystemException orbutilWrapper =
        ORBUtilSystemException.self ;
    private static final POASystemException poaWrapper =
        POASystemException.self ;
    
    public static void main(String[] args) 
    {
        try {
            Utility u = new Utility(args);

            GenericFactory f = u.readFactory();

            System.out.println("----------------------------------------");
            System.out.println("Creating objects");
            System.out.println("----------------------------------------");
            
            Hello h1 = createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_POA_ASSIGNED_OIDS, f);

            Hello h2 = createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_USER_ASSIGNED_OIDS, f);

            Hello h3 = createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_POA_ASSIGNED_OIDS, f);

            Hello h4 = createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_USER_ASSIGNED_OIDS, f);

            System.out.println("----------------------------------------");
            System.out.println("Invoking");
            System.out.println("----------------------------------------");

            invoke(h1);
            invoke(h2);
            invoke(h3);
            invoke(h4);

            System.out.println("----------------------------------------");
            System.out.println("Creating threads");
            System.out.println("----------------------------------------");

            // Create lots of threads, then call holdRequests. This
            // tests the wait_for_completion code on the server.
            WorkerThread[] threads = new WorkerThread[N_LOOPS*4];
            for ( int i=0; i<N_LOOPS; i++ ) {
                threads[i*4] = invokeOnThread(h1);
                threads[i*4+1] = invokeOnThread(h2);
                threads[i*4+2] = invokeOnThread(h3);
                threads[i*4+3] = invokeOnThread(h4);
            }

            Thread.sleep(500); // sleep to allow some invocations to happen

            System.out.println("----------------------------------------");
            System.out.println("holding requests");
            System.out.println("----------------------------------------");

            f.holdRequests();

            System.out.println("----------------------------------------");
            System.out.println("finished holding");
            System.out.println("----------------------------------------");

            Thread.sleep(1000); // sleep for 1 seconds to quiesce invocations

            System.out.println("----------------------------------------");
            System.out.println("re-activating");
            System.out.println("----------------------------------------");

            f.activate();

            System.out.println("----------------------------------------");
            System.out.println("reactivated, waiting for threads to join");
            System.out.println("----------------------------------------");

            // wait for all the threads to finish
            int errors = 0;
            for ( int i=0; i<N_LOOPS*4; i++ ) {
                threads[i].join();
                if (threads[i].errorOccured())
                    errors++;
            }

            if (errors > 0) {
                String msg = "WorkerThread(s) had " + errors + " error(s)";
                System.out.println("----------------------------------------");
                System.out.println(msg);
                System.out.println("----------------------------------------");
                throw new Exception(msg);
            }

            System.out.println("----------------------------------------");
            System.out.println("discarding requests");
            System.out.println("----------------------------------------");

            f.discardRequests();

            // Each of these should throw an exception, but that's what they're
            // supposed to do

            try {
                invoke(h1);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h1)");
            } catch ( COMM_FAILURE ex ) {
                checkTransient("h1", ex);
            }
            try {
                invoke(h2);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h2)");
            } catch ( COMM_FAILURE ex ) {
                checkTransient("h2", ex);
            }
            try {
                invoke(h3);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h3)");
            } catch ( COMM_FAILURE ex ) {
                checkTransient("h3", ex);
            }
            try {
                invoke(h4);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h4)");
            } catch ( COMM_FAILURE ex ) {
                checkTransient("h4", ex);
            }

            System.out.println("----------------------------------------");
            System.out.println("deactivating");
            System.out.println("----------------------------------------");

            f.deactivate();

            try {
                invoke(h1);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h1)");
            } catch ( OBJ_ADAPTER ex ) {
                System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h1");
                System.out.println("----------------------------------------");
            }
            try {
                invoke(h2);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h2)");
            } catch ( OBJ_ADAPTER ex ) {
                System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h2");
                System.out.println("----------------------------------------");
            }
            try {
                invoke(h3);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h3)");
            } catch ( OBJ_ADAPTER ex ) {
                System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h3");
                System.out.println("----------------------------------------");
            }
            try {
                invoke(h4);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h4)");
            } catch ( OBJ_ADAPTER ex ) {
                System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h4");
                System.out.println("----------------------------------------");
            }

            try {
                f.activate();
                throw new Exception("Didn't throw AdapterInactive");
            } catch (AdapterInactive ex) {
                System.out.println("----------------------------------------");
                System.out.println("Correct behavior - AdapterInactive");
                System.out.println("----------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("----------------------------------------");
            System.out.println("Client FAILED");
            System.out.println("----------------------------------------");
            e.printStackTrace(System.out);
            System.exit(1);
        }

        System.out.println("----------------------------------------");
        System.out.println("Client SUCCEEDED");
        System.out.println("----------------------------------------");
    }

    public static Hello createHello(CreationMethods c, GenericFactory f) 
    {
        return HelloHelper.narrow(f.create(HelloHelper.id(),
                                           "corba.poamanager.HelloImpl",
                                           c));
    }

    static final void invoke(Hello h) 
    {
        System.out.println(h.hi());
    }

    static final WorkerThread invokeOnThread(Hello h) 
    {
        WorkerThread th = new WorkerThread(h);
        th.start();
        return th;
    }

    public static void checkTransient(String msg, COMM_FAILURE e)
    {
        SystemException expected = 
            orbutilWrapper.communicationsRetryTimeout( new RuntimeException(),
                -1);
        SystemException expectedCause = poaWrapper.poaDiscarding();
        if (e.getClass().isInstance(expected)
            && ((SystemException)e).minor == expected.minor
            && ((SystemException)e).completed == expected.completed
            && e.getCause() != null
            && e.getCause().getClass().isInstance(expectedCause)
            && ((SystemException)e.getCause()).minor == expectedCause.minor
            && ((SystemException)e.getCause()).completed == expectedCause.completed)
        {
            System.out.println("----------------------------------------");
            System.out.println(msg + " TRANSIENT timeout SUCCESS");
            System.out.println("----------------------------------------");
        } else {
            String message = msg + " TRANSIENT timeout FAILED";
            System.out.println("----------------------------------------");
            System.out.println(message);
            System.out.println("----------------------------------------");
            throw new RuntimeException(message);
        }
    }
}


class WorkerThread extends Thread 
{
    Hello h;
    private boolean errorOccured;

    WorkerThread(Hello h)
    {
        this.h = h;
        errorOccured = false;
    }

    public void run()
    {
        try {
            System.out.println(h.hi());
        } catch (Exception e) {
            errorOccured = true;
            e.printStackTrace();
        }
    }

    public boolean errorOccured()
    {
        return errorOccured;
    }
}


