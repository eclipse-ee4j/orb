/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.rmipoacounter;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import javax.rmi.PortableRemoteObject;
import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;

public class CounterImpl extends PortableRemoteObject implements CounterIF
{
    // Temporary hack to get this test to work and keep the output
    // directory clean
    private static final String outputDirOffset
        = "/org/glassfish/rmic/classes/rmipoacounter/".replace('/', File.separatorChar);

    private String name;
    private int value;
    private ORB orb;
    private int myid;
    private static int SERVANT_ID=1;
    private boolean debug ;

    public CounterImpl(ORB orb, boolean debug) throws RemoteException
    {
        this.myid = SERVANT_ID++;
        this.orb = orb;
        this.debug = debug ;

        name = System.getProperty("output.dir")
            + outputDirOffset
            + "counterValue";

        try {
            File f = new File(name);
            if ( !f.exists() ) {
                RandomAccessFile file = new RandomAccessFile(f, "rw");
                value = 0;
                file.writeBytes(String.valueOf(value));
                file.close();
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public synchronized long increment(long invalue) throws RemoteException
    {
        if ( debug )
            System.out.println( "\nIn counterServant " + myid +
                                " increment(), invalue = " + invalue + " Server thread is " +
                                Thread.currentThread());

        try {
            // Test Current operations
            org.omg.PortableServer.Current current =
                (org.omg.PortableServer.Current)orb.resolve_initial_references(
                                                                               "POACurrent");
            POA poa = current.get_POA();
            byte[] oid = current.get_object_id();

            if ( debug )
                System.out.println( "POA = " + poa.the_name() + " objectid = " + oid);

            // Increment counter and save state
            RandomAccessFile file = new RandomAccessFile(new File(name), "rw");
            String svalue = file.readLine();
            value = Integer.parseInt(svalue);
            file.seek(0);
            value += (int)invalue;
            file.writeBytes(String.valueOf(value));
            file.close();

            System.out.println("\nIn counterServant read "+svalue+" wrote "+value);
        } catch ( Exception ex ) {
            System.err.println("ERROR in counterServant !");
            ex.printStackTrace();
            System.exit(1);
        }

        return value;
    }
}

