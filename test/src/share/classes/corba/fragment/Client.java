/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.fragment;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

public class Client
{
    // size must be divisible by four
    public static void testByteArray(FragmentTester tester, int size)
        throws RemoteException, BadArrayException
    {
        System.out.println("Sending array of length " + size);

        byte array[] = new byte[size];

        int i = 0;

        do {

            for (byte x = 0; x < 4; x++) {
                System.out.print("" + x + " ");
                array[i++] = x;
            }
            // System.out.println();

        } while (i < size);

        byte result[] = tester.verifyTransmission(array);

        if (result == null)
            throw new BadArrayException("result was null!");

        if (array.length != result.length)
            throw new BadArrayException("result length incorrect: " + result.length);

        for (i = 0; i < array.length; i++)
            if (array[i] != result[i])
                throw new BadArrayException("result mismatch at index: " + i);

        System.out.println("testByteArray completed normally");
    }

    public static org.omg.CORBA.Object readObjref(String file, org.omg.CORBA.ORB orb) {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            java.io.DataInputStream in = 
                new java.io.DataInputStream(new FileInputStream(fil));
            String ior = in.readLine();
            System.out.println("IOR: "+ior);
            return orb.string_to_object(ior);
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
        return null;
    }

    public static void main(String args[])
    {
        try{

            ORB orb = ORB.init(args, System.getProperties());

            com.sun.corba.ee.spi.orb.ORB ourORB
                = (com.sun.corba.ee.spi.orb.ORB)orb;

            System.out.println("==== Client GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
                                    ourORB.getORBData().getGIOPVersion())
                               + "====");

            /*
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("FragmentTester", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);
            */

            org.omg.CORBA.Object obj = readObjref("IOR", orb);

            FragmentTester tester = 
                (FragmentTester) PortableRemoteObject.narrow(obj, 
                                                            FragmentTester.class);

            // Do the crazy work here

            int arrayLen = Integer.parseInt(System.getProperty("array.length"));

            testByteArray(tester, arrayLen);

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
