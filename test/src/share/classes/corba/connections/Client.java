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
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 16:25:55 by Harold Carr.
//

package corba.connections;

import java.rmi.RemoteException;
import javax.naming.InitialContext;

import java.util.Properties ;

import com.sun.corba.ee.spi.orb.ORB ;

import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

public class Client 
{
    public static boolean showInbound = true;
    public static int NUM_THREADS = 100;
    public static boolean inParallel = false;

    public static ORB orb;
    public static InitialContext initialContext;
    public static RemoteInterface c1s11;
    public static RemoteInterface c1s12;
    public static RemoteInterface c1s21;
    public static RemoteInterface c1s22;

    public static Struct[] instance;
    public static Struct[] returnInstance;

    public static String name;

    public static ConnectionStatistics stats;

    public static void main(String[] av)
    {
        instance = Struct.getSampleInstance();

        try {
            name = av[0];

            U.sop(name + " ORB.init ...");

            Properties props = new Properties() ;
            props.setProperty( "com.sun.corba.ee.ORBDebug", "subcontract" ) ;
            orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(av, props);
            stats = new ConnectionStatistics( orb ) ;

            U.sop(name + " InitialContext ...");

            initialContext = C.createInitialContext(orb);

            showInbound = true;

            pstats(" after InitialContext");

            c1s11 = lookup(-1, Server.service11, initialContext);
            c1s12 = lookup(-1, Server.service12, initialContext);

            pstats(" after lookup s1*");

            c1s21 = lookup(-1, Server.service21, initialContext);
            c1s22 = lookup(-1, Server.service22, initialContext);

            pstats(" after lookup s2*");

            showInbound = false;

            U.sop(name + " making call...");

            call(c1s11, "c1s11");
            callBlock(c1s11, "c1s11 BLOCK");
            call(c1s12, "c1s12");
            callResume(c1s11, "c1s11 RESUME");
            call(c1s21, "c1s21");
            callBlock(c1s21, "c1s21 BLOCK");
            call(c1s22, "c1s22");
            callResume(c1s21, "c1s21 RESUME");

            for (int i = 0; i < NUM_THREADS; i++) {
                //boolean exitAndPrintResult = (i % 10 == 0) ? true : false;
                boolean exitAndPrintResult = true;
                CallThread callThread =
                    new CallThread(i, exitAndPrintResult, exitAndPrintResult);
                if (inParallel) {
                    callThread.start();
                } else {
                    callThread.doWork();
                }
            }

            U.sop(name + " PASSED");

        } catch (Exception e) {
            e.printStackTrace(System.out);
            U.sop(name + " FAILED");
            System.exit(1);
        }
    }

    public static RemoteInterface lookup(int i, String rn,
                                         InitialContext initialContext)
        throws
            Exception
    {
        RemoteInterface result = (RemoteInterface)
            U.lookupAndNarrow(rn, RemoteInterface.class, initialContext);

        if (false) {
            com.sun.corba.ee.spi.ior.IOR ior =
                ((com.sun.corba.ee.spi.transport.ContactInfoList)
                 ((com.sun.corba.ee.spi.protocol.ClientDelegate)
                  StubAdapter.getDelegate( result )).
                  getContactInfoList()).getTargetIOR();

            ORB thisOrb = (ORB)StubAdapter.getORB( result ) ;

            U.sop(i + ": lookup: " + rn 
                  + " orbIdentity: " + System.identityHashCode(thisOrb)
                  + " stubIdentity: " + System.identityHashCode(result)
                  + " iorIdentity: " + System.identityHashCode(ior)
                  + " iorHash: " + ior.hashCode());
        }
        return result;
    }

    public static void call(RemoteInterface r, String msg)
        throws
            Exception
    {
        returnInstance = r.method(instance);
        pstats(msg);
        U.sop(r.testMonitoring());
    }

    public static void callBlock(RemoteInterface r, String msg)
        throws
            Exception
    {
        BlockThread blockThread = new BlockThread(r);
        blockThread.start();
        Thread.sleep(2000);
        pstats(msg);
        U.sop(r.testMonitoring());
    }

    public static void callResume(RemoteInterface r, String msg)
        throws
            Exception
    {
        r.resume();
        Thread.sleep(2000);
        pstats(msg);
        U.sop(r.testMonitoring());
    }

    public static void pstats(String msg)
    {
        outbound(msg);
        inbound(msg);
    }

    public static void outbound(String msg)
    {
        stats.outbound(name + " " + msg, (com.sun.corba.ee.spi.orb.ORB)orb);
    }

    public static void inbound(String msg)
    {
        if (showInbound) {
            stats.inbound(name + " " + msg, (com.sun.corba.ee.spi.orb.ORB)orb);
        }
    }
}

class BlockThread
    extends
        Thread
{
    RemoteInterface r;

    BlockThread(RemoteInterface r)
    {
        this.r = r;
    }

    public void run()
    {
        try {
            r.block();
        } catch (RemoteException e) {
            e.printStackTrace(System.out);
            U.sop("BlockThread FAILED");
            System.exit(1);
        }
    }
}

class CallThread
    extends
        Thread
{
    int i;
    boolean exit;
    boolean printResult;

    CallThread(int i, boolean exit, boolean printResult)
    {
        this.i = i;
        this.exit = exit;
        this.printResult = printResult;
    }

    public void run()
    {
        doWork();
    }

    public void doWork()
    {
        try {
            U.sop(i + ": CallThread ORB.init:");
            ORB orb = (ORB)ORB.init((String[])null, null);
            U.sop(i + ": CallThread InitialContext:");
            InitialContext initialContext = C.createInitialContext(orb);
            U.sop(i + ": CallThread lookup:");
            RemoteInterface s11 =
                Client.lookup(i, Server.service11, initialContext);
            RemoteInterface s21 =
                Client.lookup(i, Server.service21, initialContext);
            U.sop(i + ": CallThread call:");
            String s11Result = s11.testMonitoring();
            String s21Result = s21.testMonitoring();
            U.sop(i + ": CallThread call complete:");
            if (printResult) {
                U.sop(i + ": CallThread result: ");
                U.sop(s11Result);
                U.sop(s21Result);
            }
            if (! exit) {
                orb.run();
            }
            U.sop(i + ": exiting");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            U.sop("CallThread " + i + " FAILED");
            System.exit(1);
        }
    }
}

// End of file.
