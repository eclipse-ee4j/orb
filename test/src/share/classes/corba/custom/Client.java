/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.custom;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public class Client
{
    // Create a new string with size number of filler chars
    private static String createLargeString(int size, char filler)
    {
        char valueBuf[] = new char[size];

        for (int i = 0; i < size; i++)
            valueBuf[i] = filler;
        
        return new String(valueBuf);
    }

    // Create a simple ArrayListNode (custom marshaled) with a large
    // data value.  Make it's next array support two links, but don't
    // give them any values.
    //
    // Fails at fragment size 160.  Seems to have something to do with
    // reading the Any (Anys are used when marshaling arrays).  TypeCodes
    // may assume something about the CDR buffer that is no longer valid
    // now that we fragment.
    public static void testArrayListNodeFailure1(Verifier verifier)
        throws RemoteException, Exception
    {
        System.out.println("---- Testing ArrayListNode Failure 1 ----");

        ArrayListNode a = new ArrayListNode();

        a.data = Client.createLargeString(1024, 'A');
        a.next = new java.lang.Object[2];

        ArrayListNode result = (ArrayListNode)verifier.verifyTransmission(a);

        if (!a.data.equals(result.data))
            throw new Exception("result.data isn't equal to a.data");

        System.out.println("---- Successful ----");
    }

    // This is just a little harder than test 1.  Create two ArrayListNodes
    // with large data values.  Give the first a link to the second.
    //
    // Fails at fragment sizes 32, 64, and 160.
    //
    // These are really scary failures because the ORB level doesn't throw
    // exceptions.  It delivers data, but it's not the same as what was
    // sent!  
    public static void testArrayListNodeFailure2(Verifier verifier)
        throws RemoteException, Exception
    {
        System.out.println("---- Testing ArrayListNode Failure 2 ----");

        ArrayListNode a = new ArrayListNode();
        ArrayListNode b = new ArrayListNode();

        a.data = Client.createLargeString(1024, 'A');
        b.data = Client.createLargeString(577, 'B');

        a.next = new java.lang.Object[1];
        b.next = null;
        a.next[0] = b;

        ArrayListNode result = (ArrayListNode)verifier.verifyTransmission(a);

        if (!a.data.equals(result.data))
            throw new Exception("result.data isn't equal to a.data");

        if (!b.data.equals(((ArrayListNode)result.next[0]).data))
            throw new Exception("result.next.data isn't equal to b.data");

        System.out.println("---- Successful ----");
    }

    public static void testComplexHashtable(Verifier verifier)
        throws RemoteException, Exception
    {
        System.out.println("---- Testing Complex Hashtable ----");

        Node a = Node.createNode(1024, 'A');
        Node b = Node.createNode(577, 'B');
        Node c = Node.createNode(222, 'C');
        Node d = Node.createNode(799, 'D');
        Node e = Node.createNode(1024, 'E');

        a.links.add(b);
        b.links.add(c);
        c.links.add(d);
        d.links.add(e);
        e.links.add(a);
        c.links.add(c);
        b.links.add(d);
        a.links.add(e);

        String aStr = "A";
        //        String bStr = "B";

        String bStr = new String(CharGenerator.getSomeUnicodeChars());

        String cStr = "C";
        String dStr = "D";
        String eStr = "E";

        Hashtable complex = new Hashtable();

        complex.put(aStr, a);
        complex.put(bStr, b);
        complex.put(cStr, c);
        complex.put(dStr, d);
        complex.put(eStr, e);

        Hashtable result = (Hashtable)verifier.verifyTransmission(complex);

        if (result.size() != complex.size())
            throw new Exception("Result has fewer items: " + result.size());

        Node resA = (Node)result.get(aStr);
        Node resB = (Node)result.get(bStr);
        Node resC = (Node)result.get(cStr);
        Node resD = (Node)result.get(dStr);
        Node resE = (Node)result.get(eStr);

        if (!a.equals(resA))
            throw new Exception("result a != a");
        if (!b.equals(resB))
            throw new Exception("result b != b");
        if (!c.equals(resC))
            throw new Exception("result c != c");
        if (!d.equals(resD))
            throw new Exception("result d != d");
        if (!e.equals(resE))
            throw new Exception("result e != e");
    }

    public static void main(String args[])
    {
        try {

            String fragmentSize = System.getProperty(com.sun.corba.ee.spi.misc.ORBConstants.GIOP_FRAGMENT_SIZE);

            if (fragmentSize != null)
                System.out.println("---- Fragment size: " + fragmentSize);

            ORB orb = ORB.init(args, System.getProperties());

            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("Verifier", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);

            Verifier verifier = 
                (Verifier) PortableRemoteObject.narrow(obj, 
                                                       Verifier.class);

            Client.testArrayListNodeFailure1(verifier);
            Client.testArrayListNodeFailure2(verifier);
            Client.testComplexHashtable(verifier);

        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
