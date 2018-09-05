/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;
import java.io.*;
import java.util.*;
import javax.rmi.CORBA.*;

import org.testng.annotations.Test ;
import org.testng.annotations.BeforeSuite ;
import org.testng.Assert ;

import corba.framework.TestngRunner ;

public class Client { 
    // Important: put the initialContext here so that is does NOT get GCed and finalized
    // while the test is running!
    private InitialContext rootContext ;
    private Tester tester ;

    /* Using a byte array in front and behind the main
     * payload parameter, this should allow us to break
     * the MarshalTester's data across just about all
     * possible fragment points.  This exercises code for
     * handling primitives, etc, across fragment 
     * boundaries.  A CustomMarshalTester can be used to
     * add the further complicating factor of chunking.
     *
     * Note that this probably doesn't do much with indirections
     * across boundaries, or indirections interacting with
     * chunks.
     */
    private void testFragmentation(Tester tester, 
        MarshalTester payload) throws Exception {

        System.out.println("Testing fragmentation with a " + payload.getClass().getName() 
            + "...");

        for (int i = 0; i < 2048; i++) {
            byte[] predata = new byte[i];
            byte[] postdata = new byte[i];

            for (int y = 0; y < i; y++) {
                predata[y] = postdata[y] = (byte)(y % 128);
            }

            MarshalTester result = tester.verify(predata, payload, postdata);

            if (!payload.equals(result))
                throw new Exception("Payloads not equal at predata size "
                                    + predata.length);
        }

        System.out.println("PASSED");
    }

    @Test
    public void testFragmentationMarshalTester() throws Exception {
        MarshalTester mt = new MarshalTester();
        mt.init(tester);
        testFragmentation(tester, mt);
    }

    @Test
    public void testFragmentationCustomMarshalTester() throws Exception {
        CustomMarshalTester cmt = new CustomMarshalTester();
        cmt.init(tester);
        Hashtable extra = new Hashtable();
        extra.put("pkg1", tester);
        extra.put("pkg2", tester);
        cmt.add(extra);
        cmt.add(tester);
        testFragmentation(tester, cmt);
    }

    /**
     * Verify that checked exceptions work.
     */
    @Test
    public void testCheckedException() throws Exception {
        try {
            System.out.println("Testing CheckedException...");

            tester.throwCheckedException();

            throw new Exception("Failed - No exception");

        } catch (CheckedException re) {
            System.out.println("PASSED");
        } catch (Throwable t) {
            throw new Exception("FAILED -- received: " + t.getClass().getName());
        }
    }

    /** 
     * Very that Remote exceptions work.  We had a compatibility
     * problem between 1.3.x and 1.4.0 with Remote and unchecked
     * exceptions.  Unfortunately, unless used in multi-JVM
     * scenarios, this test still can't catch it.
     */
    @Test
    public void testRemoteException() throws Exception {
        try {
            System.out.println("Testing RemoteException...");

            tester.throwRemoteException();

            throw new Exception("Failed - No exception");

        } catch (RemoteException re) {
            System.out.println("PASSED");
        } catch (Throwable t) {
            throw new Exception("FAILED -- received: " + t.getClass().getName());
        }
    }

    @Test
    public void testRuntimeException() throws Exception {
        try {
            System.out.println("Testing RuntimeException...");

            tester.throwRuntimeException();

            throw new Exception("Failed - No exception");

        } catch (UncheckedException ex) {
            System.out.println("PASSED");
        } catch (Throwable t) {
            throw new Exception("FAILED -- received: " + t.getClass().getName());
        }
    }

    /**
     * Even though AbsTester isn't Remote or Serializable, 
     * this should work since the Server's getAbsTester
     * method returns a Tester (subinterface of AbsTester).
     */
    @Test
    public void testAbstractInterface() 
        throws RemoteException, DataCorruptedException
    {
        System.out.println("Testing abstract interface...");

        AbsTester absTester = tester.getAbsTester();

        if (!(absTester instanceof Tester))
            throw new DataCorruptedException("Not a Tester");

        // Methods in the abstract interface which
        // are declared to throw RemoteException
        // should also work.
        absTester.ping();

        System.out.println("PASSED");
    }

    /**
     * This will result in an Any on the wire and
     * a wchar type code at some point.  We don't actually
     * use our type codes to unmarshal, so this won't
     * be a real test unless in an interop scenario.
     */
    @Test
    public void testIncorrectCharTC()
        throws DataCorruptedException, RemoteException {
        
        System.out.println("Testing for incorrect char TC...");

        Character ch1 = new Character('\u6D77');
        if (!ch1.equals(tester.verify(ch1)))
            throw new DataCorruptedException("Error on character 1");

        System.out.println("PASSED");
    }

    /**
     * Simply passes an object which uses PutField/GetField.
     */    
    @Test
    public void testPutFieldsGetFields()
        throws DataCorruptedException, RemoteException {

        System.out.println("Testing PutFields/GetFields...");
        
        TestObject to = new TestObject();

        if (!to.equals(tester.verify(to)))
            throw new DataCorruptedException("TestObjects not equal");

        System.out.println("PASSED");
    }

    /**
     * Makes sure that superclass data is unmarshaled properly.
     */
    @Test
    public void testSuperClasses()
        throws DataCorruptedException, RemoteException {

        System.out.println("Testing superclass constructor call with RMI-IIOP...");

        SubClass sc = new SubClass();

        System.out.println("Sending: " + sc);

        SubClass result = (SubClass)tester.verify(sc);

        System.out.println("Received: " + result);

        if (!sc.equals(result))
            throw new DataCorruptedException("Bad result!");

        System.out.println("PASSED");
    }

    /**
     * Makes sure that a class containing a static nested
     * inner class is marshaled properly.
     *
     * NOTE: this class CANNOT be marshalled properly,
     * because the nest inner class is not in a static context,
     * which means that it does not have a no-args constructor,
     * even if no constructor is declared.  This means that 
     * the nested inner class is not properly externalizable,
     * and this test is invalid.  Note that it used to work
     * with the previous native implementation.
     */
    public void testStaticNestedInner()
        throws DataCorruptedException, RemoteException {

        TestClass data = new TestClass();
        
        System.out.println("Testing static nested inner class...");

        java.lang.Object result = tester.verify(data);

        if (result == null)
            throw new DataCorruptedException("Failed -- result was null");

        if (!data.equals(result))
            throw new DataCorruptedException("Failed: " + result);

        System.out.println("PASSED");
    }

    /**
     * Makes sure that recursive references in custom marshaled
     * values works.  This was more important as an interop
     * test in the Connectathon.
     */
    @Test
    public void testRecursiveReferences()
        throws DataCorruptedException, RemoteException {

        System.out.println("Testing recursive references...");

        // Vector is not custom marshaled before JDK 1.4
        Vector nonCustom = new Vector();
        nonCustom.add(nonCustom);
        Vector vectorResult = (Vector)tester.verify(nonCustom);
        if (vectorResult == null)
            throw new DataCorruptedException("Result Vector was null");
        if (vectorResult.size() != nonCustom.size())
            throw new DataCorruptedException("Result Vector's size is "
                                             + vectorResult.size());
        if (vectorResult.elementAt(0) != vectorResult)
            throw new DataCorruptedException("Vector graph not preserved");

        Hashtable custom = new Hashtable();
        String customKey = "Test";
        custom.put(customKey, custom);
        Hashtable hashResult = (Hashtable)tester.verify((Map)custom);
        if (hashResult == null)
            throw new DataCorruptedException("Result Hashtable was null");
        if (hashResult.size() != custom.size())
            throw new DataCorruptedException("Result Hashtable size is "
                                             + hashResult.size());
        Object hashObj = hashResult.get(customKey);

        // Should preserve self reference
        if (hashObj != hashResult)
            throw new DataCorruptedException("Hashtable graph not preserved");

        Hashtable table2 = new Hashtable();
        table2.put("three", table2);

        Hashtable table2Result = (Hashtable)tester.verify(table2);
        if (table2Result == null ||
            table2Result.size() != table2.size() ||
            table2Result.get("three") != table2Result) {
            throw new DataCorruptedException("Bad resulting Hashtable");
        }

        System.out.println("PASSED");
    }

    /**
     * If we start to actually use our TypeCodes to marshal/unmarshal
     * the Anys (produced when RMI-IIOP sends something as a
     * java.lang.Object, Serializable, or Externalizable), this
     * will test all the basic type codes.
     */
    @Test
    public void testTypeCodeCompatibility()
        throws DataCorruptedException, RemoteException
    {
        System.out.println("Testing TypeCode compatibility...");

        MarshalTester mt = new MarshalTester();
        mt.init(tester);

        MarshalTester result = (MarshalTester)tester.verify((java.lang.Object)mt);

        if (!mt.equals(result))
            throw new DataCorruptedException("MarshalTesters not equal!");

        System.out.println("PASSED");
    }

    /**
     * Occassionally people have filed bugs saying that SQL Date
     * is broken in RMI-IIOP, but the problem usually clears up.
     */
    @Test
    public void testSQLDate()
        throws DataCorruptedException, RemoteException
    {
        System.out.println("Testing SQL Date...");

        for (int i = 0; i < 100; i++) {
            java.sql.Date d = new java.sql.Date((new java.util.Date()).getTime());
            java.sql.Date res = tester.verify(d);
            if (!d.equals(res))
                throw new DataCorruptedException("Test 1 failed");
        }

        System.out.println("PASSED");
    }

    /**
     * Occassionally people have filed bugs saying that Properties
     * is broken in RMI-IIOP, but the problem usually clears up.
     */
    @Test
    public void testProperties() 
        throws RemoteException,
               DataCorruptedException 
    {
        System.out.println("Testing Properties objects...");

        Properties props = System.getProperties();

        if (!props.equals(tester.verify(props)))
            throw new DataCorruptedException("Test 1 failed");
        if (!props.equals(tester.verify((Object)props)))
            throw new DataCorruptedException("Test 2 failed");

        Properties defaults = new Properties();
        defaults.setProperty("Test1", "Test2");
        defaults.setProperty("Test3", "Test4");
        Properties props2 = new Properties(defaults);
        props2.setProperty("Test5", "Test6");
        
        if (!props2.equals(tester.verify(props2)))
            throw new DataCorruptedException("Test 3 failed");
        if (!props2.equals(tester.verify((Object)props2)))
            throw new DataCorruptedException("Test 4 failed");       

        System.out.println("PASSED");
    }

    /**
     * Indirections to Remotes in custom marshaled valuetypes
     * (like ArrayList) used to break often, but we seem to
     * have it fixed, now.
     */
    @Test
    public void testArrayList()
        throws RemoteException,
               DataCorruptedException
    {
        System.out.println("Testing ArrayList of Remotes...");

        ArrayList list = new ArrayList(255);

        for (int i = 0; i < 255; i++)
            list.add(tester);

        List result = tester.verify(list);

        if (result == null)
            throw new DataCorruptedException("Result is null!");

        if (result.size() != list.size())
            throw new DataCorruptedException("Sizes not equal!");

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(result.get(i)))
                throw new DataCorruptedException("Item not equal: " + i);
        }

        System.out.println("PASSED");
    }

    /**
     * Calendar does some interesting RMI-IIOP tricks in
     * its readObject/writeObject.  When talking to an
     * older 1.3.x Calendar, it will read more data than
     * was sent.  *** This is broken in RMI-IIOP.  However,
     * that will only occur when 1.4.x is talking to 1.3.x,
     * which we currently can't test within the framework.
     */
    @Test
    public void testCalendar()
        throws RemoteException, DataCorruptedException
    {
        System.out.println("Testing Calendar...");

        Calendar c1 = Calendar.getInstance();

        Calendar c2 = (Calendar)tester.verify(c1);

        if (!c1.equals(c2))
            throw new DataCorruptedException("Calendars not equal");

        System.out.println("PASSED");
    }

    /**
     * Makes sure that the protected superclass writeReplace/
     * readResolve methods are called.
     */
    @Test
    public void testWriteReplaceReadResolve()
        throws DataCorruptedException, RemoteException {

        System.out.println("Testing writeReplace/readResolve...");

        Status.reset();

        ReplaceSubClass rsc = new ReplaceSubClass();

        System.out.println("Sending: " + rsc);

        ReplaceSubClass result = (ReplaceSubClass)tester.verify(rsc);

        System.out.println("Received: " + result);

        if (!rsc.equals(result))
            throw new DataCorruptedException("Bad result!");

        if (!Status.writeReplaceCalled() ||
            !Status.readResolveCalled())
            throw new DataCorruptedException("Didn't call writeReplace and readResolve");

        System.out.println("PASSED");
    }

    @BeforeSuite
    public void setup() throws Exception {
        System.out.println("Finding tester...");
        rootContext = new InitialContext();

        System.out.println("Looking up tester...");
        java.lang.Object tst = rootContext.lookup("Tester");

        System.out.println("Narrowing...");
        tester = (Tester)PortableRemoteObject.narrow(tst, 
            Tester.class);
    }

    public static void main(String args[]) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}

