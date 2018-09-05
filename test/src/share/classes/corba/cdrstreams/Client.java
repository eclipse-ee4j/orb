/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrstreams;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

import org.testng.annotations.Test ;
import org.testng.annotations.BeforeSuite ;

import corba.framework.TestngRunner ;

public class Client
{
    GraphProcessor processor ;

    @Test
    public void testIndirection1() throws RemoteException, InvalidGraphException
    {
        System.out.println("---- testIndirection1 ----");

        Node start = new Node("start", new Vector());

        // Add a bunch of self links
        start.links.add(start);
        start.links.add(start);
        start.links.add(start);
        start.links.add(start);
        start.links.add(start);

        // If indirection isn't used, this will result in an infinite loop
        // and a stack overflow error
        processor.process(start);
    }

    private void testObjectArray( java.lang.Object array[]) throws RemoteException, Exception
    {
        for (int i = 0; i < array.length; i++) {
            if (!array[i].equals(processor.verifyTransmission(array[i])))
                throw new Exception("Object of type " + array[i].getClass().getName()
                                    + "at index " + i + " failed verifyTransmission");
        }
    }

    @Test
    public void testComplexHashtableTest() throws RemoteException, Exception
    {
        System.out.println("---- complex Hashtable Test ----");

        Node a = createNode(1024, 'A');
        Node b = createNode(577, 'B');
        Node c = createNode(222, 'C');
        Node d = createNode(799, 'D');
        Node e = createNode(1024, 'E');

        a.links.add(b);
        b.links.add(c);
        c.links.add(d);
        d.links.add(e);
        e.links.add(a);
        c.links.add(c);
        b.links.add(d);
        a.links.add(e);

        String aStr = "A";
        String bStr = "B";
        String cStr = "C";
        String dStr = "D";
        String eStr = "E";

        Hashtable complex = new Hashtable();

        complex.put(aStr, a);
        complex.put(bStr, b);
        complex.put(cStr, c);
        complex.put(dStr, d);
        complex.put(eStr, e);

        Hashtable result = (Hashtable)processor.verifyTransmission(complex);

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

        System.out.println("PASSED");
    }

    public void simpleHashtableTest()
        throws RemoteException, Exception
    {
        System.out.println("---- simple Hashtable Test ----");

        Hashtable simple = new Hashtable();

        byte strBytes1[] = new byte[1024];
        byte strBytes2[] = new byte[1024];

        for (int i = 0; i < 1024; i++) {
            strBytes1[i] = (byte)'A';
            strBytes2[i] = (byte)'B';
        }

        String one = new String(strBytes1);
        String two = new String(strBytes2);
        java.lang.Object oneKey = new Integer(1);
        java.lang.Object twoKey = new Integer(2);

        simple.put(oneKey, one);
        simple.put(twoKey, two);

        Hashtable result = (Hashtable)processor.verifyTransmission(simple);

        String oneTest = (String)result.get(oneKey);
        if (oneTest == null)
            throw new Exception("String one not in result Hashtable");
        if (!one.equals(oneTest))
            throw new Exception("String one doesn't equal result string one");

        String twoTest = (String)result.get(twoKey);
        if (twoTest == null)
            throw new Exception("String two not in result Hashtable");
        if (!two.equals(twoTest))
            throw new Exception("String two doesn't equal result string two");

        System.out.println("PASSED");
    }

    @Test
    public void testCustomMarshalers() throws RemoteException, Exception
    {
        System.out.println("---- testCustomMarshalers ----");

        // First test a value with a good custom marshaler (one that
        // correctly reads everything off the stream it should)

        System.out.println("Testing good custom marshalers...");

        java.lang.Object good[] = new java.lang.Object[100];
        for (int i = 0; i < good.length; i++)
            good[i] = new CustomMarshaled(i, i + 100, true);
        testObjectArray(good);

        System.out.println("Testing buggy ones that leave bytes when reading...");

        // Our code should skip over anything incorrectly left on the wire by
        // the custom marshaler
        java.lang.Object buggy[] = new java.lang.Object[100];
        for (int i = 0; i < buggy.length; i++) {
            if (i % 7 == 0)
                buggy[i] = new CustomMarshaled(i, i + 100, false);
            else
                buggy[i] = new CustomMarshaled(i, i + 100, true);
        }

        testObjectArray(buggy);

        System.out.println("Testing a buggy one that tries to read too much...");

        try {
            // This should throw a MARSHAL exception (changed to IOException)
            processor.verifyTransmission(new OverReader());

            throw new Exception("Didn't throw an error for an overread");

        } catch (java.io.IOException e) {
            System.out.println("Successful");
        }
    }

    private static class OverReader implements java.io.Serializable
    {
        private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException
        {
            out.defaultWriteObject();
        }

        private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException
        {
            in.defaultReadObject();

            // Try to read too much
            in.readDouble();
        }
    }

    @Test
    public void testLargeArray()
        throws RemoteException, Exception
    {
        System.out.println("---- testLargeArray ----");

        long bigArray[] = new long[32000];

        for (int i = 0; i < bigArray.length; i++)
            bigArray[i] = i;

        java.lang.Object resultObj = processor.verifyTransmission(bigArray);

        long testArray[] = (long[])resultObj;

        for (int i = 0; i < bigArray.length; i++)
            if (bigArray[i] != testArray[i])
                throw new Exception("Array differed at index " + i
                                    + " with values " + bigArray[i]
                                    + " != " + testArray[i]);

        System.out.println("PASSED");
    }

    public Node createNode(int valueSize, char filler)
    {
        char valueBuf[] = new char[valueSize];

        for (int i = 0; i < valueSize; i++)
            valueBuf[i] = filler;

        return new Node(new String(valueBuf), new Vector());
    }

    @Test
    public void testIndirectionAndOffset()
        throws RemoteException, InvalidGraphException, Exception
    {
        System.out.println("---- testIndirectionAndOffset ----");

        Node start = createNode(500, 'A');

        // Add some self links
        start.links.add(start);
        start.links.add(start);

        Node second = createNode(500, 'B');

        // Add a two way link to the second node
        second.links.add(start);
        start.links.add(second);

        processor.process(start);

        // Now try something much harder
        
        Node a1 = createNode(500, '1');
        Node a2 = createNode(500, '2');
        Node a3 = createNode(256, '3');
        Node a4 = createNode(1024, '4');
        Node a5 = createNode(10000, '5');

        a1.links.add(a2);
        a2.links.add(a3);
        a3.links.add(a4);
        a4.links.add(a5);
        a5.links.add(a1);
        a3.links.add(a1);
        a4.links.add(a2);
        a1.links.add(a4);
        
        Node start2 = a1;
        Node result2 = (Node)processor.verifyTransmission(start2);

        if (!start2.equals(result2))
            throw new Exception("start2 did not equal result2");

        Node xa2 = (Node)result2.links.get(0);
        Node xa3 = (Node)xa2.links.get(0);
        Node xa4 = (Node)xa3.links.get(0);
        Node xa5 = (Node)xa4.links.get(0);

        if (!a2.equals(xa2))
            throw new Exception("a2 did not equal xa2");
        if (!a3.equals(xa3))
            throw new Exception("a3 did not equal xa3");
        if (!a4.equals(xa4))
            throw new Exception("a4 did not equal xa4");
        if (!a5.equals(xa5))
            throw new Exception("a5 did not equal xa5");

        System.out.println("Success!");
    } 

    @Test
    public void testUserException() throws RemoteException, Exception
    {
        System.out.println("---- testExceptionsAndReset ----");

        // User exceptions use mark/reset to peek the exception ID

        Node start = new Node(new String("Invalid Node"), null);

        try {
            processor.process(start);

            throw new Exception("Didn't get InvalidGraphException!");

        } catch (InvalidGraphException ex) {
            System.out.println("Successfully caught exception: "
                               + ex);
        }

        System.out.println("PASSED");
    }


    @Test
    public void testMarkReset() throws RemoteException, Exception
    {
        System.out.println("---- Testing mark and reset ----");

        Properties props = System.getProperties();

        // Anything much larger makes this test prohibitively slow
        props.put(ORBConstants.GIOP_FRAGMENT_SIZE, "64");

        ORB orb = ORB.init(args, System.getProperties());
        
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        
        NameComponent nc = new NameComponent("GraphProcessor", "");
        NameComponent path[] = {nc};
        
        org.omg.CORBA.Object obj = ncRef.resolve(path);
        
        processor = 
            (GraphProcessor) PortableRemoteObject.narrow(obj, 
                                                         GraphProcessor.class);
        
        MarkResetTester tester = new MarkResetTester(64);

        if (!processor.receiveObject(tester))
            throw new Exception("Server received a null object!");

        System.out.println("PASSED");
    }
    
    private static String[] args ;

    @BeforeSuite
    public void setup() throws Exception {
        ORB orb = ORB.init(args, System.getProperties());

        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);

        NameComponent nc = new NameComponent("GraphProcessor", "");
        NameComponent path[] = {nc};

        org.omg.CORBA.Object obj = ncRef.resolve(path);

        processor = (GraphProcessor) PortableRemoteObject.narrow(obj, 
            GraphProcessor.class);
    }

    public static void main(String args[])
    {
        Client.args = args ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}
