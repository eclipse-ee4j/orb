/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package rmic;

import java.rmi.Remote;
import java.rmi.RemoteException;
import test.ServantContext;
import test.RemoteTest;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;
import javax.naming.Context;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.BAD_OPERATION;
import java.io.File;
import com.sun.corba.ee.impl.util.JDKBridge;
import java.rmi.MarshalException;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import org.glassfish.pfl.test.JUnitReportHelper;

/*
 * @test
 */
public class LocalStubTest extends RemoteTest {

    private static final String publishName         = "HelloServer";
    private static final String servantInterface    = "rmic.LocalHello";
    private static final String servantClass        = "rmic.LocalHelloServant";
    private static final String[] compileEm         = { servantClass,
                                                        servantInterface,
                                                        "rmic.BaseImpl"};
    private File[] classFiles                       = {null,null};
    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected String[] getRemoteServantClasses () {
        /*
        
        // If we can, delete the rmic.LocalHello and
        // rmic.LocalHelloServant class files so that
        // rmic will compile them. This will ensure that
        // real argument names will be used and tests
        // that the generated variable names will not
        // clash...
        
        ClassPath path = null;
        try {
            path = ParseTest.createClassPath();
            for (int i = 0; i < 2; i++) {
                ClassFile cls = path.getFile(compileEm[i].replace('.',File.separatorChar) + ".class");
                if (cls != null && !cls.isZipped()) {
                    File file = new File(cls.getPath());
                    File newName = new File(cls.getPath()+"X");
                    if (file.renameTo(newName)) {
                        classFiles[i] = newName;
                    }
                }
            }
        
            path.close();
        } catch (Exception e) {}
        
        */

        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-always", "-keep"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    private final JUnitReportHelper helper ;
    private boolean first = true ;

    public LocalStubTest() {
        helper = new JUnitReportHelper( this.getClass().getName() ) ;
    }

    private void newTest( String name ) {
        if (first)
            first = false ;
        else 
            helper.pass() ;

        helper.start( name ) ;
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    public void doTest (ServantContext context) throws Throwable {
        String currentCodebase = JDKBridge.getLocalCodebase();
        
        try {
            newTest( "startServant" ) ;
            // Start up our servant. (The 'iiop' flag is set to true by RemoteTest
            // unless the -jrmp flag was used).
            Remote remote = context.startServant(servantClass,publishName,true,iiop);

            if (remote == null) {
                throw new Exception ("Could not start servant: " + servantClass);
            }

            newTest( "remoteRefCorrectResponse" ) ;
            LocalHello remoteRef = (LocalHello) PortableRemoteObject.narrow(remote,LocalHello.class);

            if (!remoteRef.sayHello("LocalStubTest").equals("Hello LocalStubTest")) {
                throw new Exception("Could not communicate with servant.");   
            }
            
            newTest( "remoteRefIsRemoteStub" ) ;
            // Make sure that remoteRef is really a remote stub...
            Delegate del = StubAdapter.getDelegate( remoteRef ) ;
            ORB orb = del.orb((org.omg.CORBA.Object)remoteRef);
            ServantObject so = del.servant_preinvoke(
                (org.omg.CORBA.Object)remoteRef,"method",LocalHello.class);
            if (so != null) {
                throw new Exception("Got local stub for remoteRef.");   
            }
            
            newTest( "makeLocalServant" ) ;
            // Make a local servant, connect it and convert it to a stub...
            LocalHelloServant localImpl = new LocalHelloServant();
            PortableRemoteObject.connect(localImpl,remoteRef);
            Remote localRef1 = PortableRemoteObject.toStub(localImpl);
            if (localRef1 == null) {
                throw new Exception ("toStub() failed");
            }
            
            // Now make sure it is really a local stub by calling
            // servant_preinvoke and comparing the result to our
            // local servant...
            
            newTest( "localRefIsLocal" ) ;
            del = StubAdapter.getDelegate( localRef1 ) ;
            so = del.servant_preinvoke((org.omg.CORBA.Object)localRef1,
                "method",LocalHello.class);
            if (so == null) {
                throw new Exception("servant_preinvoke() returned null");
            }
            if (so.servant != localImpl) {
                throw new Exception("servant_preinvoke() returned wrong servant");
            }
            
            // Publish and retrieve localImpl to/from name service...
            newTest( "unmarshaledLocalRefStillLocal" ) ; 
            Context nameContext = context.getNameContext();
            nameContext.rebind("localRef2",localImpl);
            Object temp = nameContext.lookup("localRef2");

            LocalHello localRef2 = (LocalHello) PortableRemoteObject.narrow(temp,LocalHello.class);
      
            // Now make sure it is local...
            
            del = StubAdapter.getDelegate( localRef2 ) ;
            so = del.servant_preinvoke( (org.omg.CORBA.Object)localRef2,
                "method",LocalHello.class);
            if (so == null) {
                throw new Exception("servant_preinvoke() returned null");
            }
            if (so.servant != localImpl) {
                throw new Exception("servant_preinvoke() returned wrong servant");
            }

            // Now make sure local copying gets done correctly...
            newTest( "localCopyobjectOK" ) ; 
            String localString1 = "one";
            RemoteException localValue1 = new RemoteException();
            BAD_OPERATION localValue2 = new BAD_OPERATION();
            MarshalException localValue3 = new MarshalException("test");
            int localString1Hash = System.identityHashCode(localString1);
            int localValue1Hash = System.identityHashCode(localValue1);
            int localValue2Hash = System.identityHashCode(localValue2);
            int[] primitiveArray1 = {3,7,9};
            char[][] primitiveArray2 = {{'a'},{'b'},{'c','d'}};
                
            if (localRef2.echoArg1(3,localValue3) == localValue3) {
                throw new Exception("localValue3 not copied!");
            }
 
            if (localRef2.echoString(localValue1,localString1,localValue3) == localString1) {
                throw new Exception("localString1 not copied!");
            }

            if (localRef2.echoObject(localValue1) == localValue1) {
                throw new Exception("localValue1 not copied!");
            }
            
            int[] pArray1Copy = (int[])localRef2.echoObject(primitiveArray1);
            if (pArray1Copy == primitiveArray1) {
                throw new Exception("primitiveArray1 not copied!");
            }
            if (primitiveArray1[0] != pArray1Copy[0] ||
                primitiveArray1[1] != pArray1Copy[1] ||
                primitiveArray1[2] != pArray1Copy[2]) {
                throw new Exception("primitiveArray1 not copied *correctly*!");
            }
            
            char[][] pArray2Copy = (char[][])localRef2.echoObject(primitiveArray2);
            if (pArray2Copy == primitiveArray2) {
                throw new Exception("primitiveArray2 not copied!");
            }
            if (primitiveArray2[0][0] != pArray2Copy[0][0] ||
                primitiveArray2[1][0] != pArray2Copy[1][0] ||
                primitiveArray2[2][0] != pArray2Copy[2][0] ||
                primitiveArray2[2][1] != pArray2Copy[2][1]) {
                throw new Exception("primitiveArray2 not copied *correctly*!");
            }

            if (localRef2.identityHash(localValue1) == localValue1Hash) {
                throw new Exception("localValue1Hash hash == localValue1Hash!");
            }

            int[] hash = localRef2.identityHash(localString1,localString1,localString1);

            hash = localRef2.identityHash(localString1,localString1,localValue1);
            if (hash[0] == localString1Hash ||
                hash[1] == localString1Hash ||
                hash[2] == localValue1Hash) {
                throw new Exception("string,string,value did not get copies!");                
            }
            
            hash = localRef2.identityHash(localValue1,localValue1,localValue2);
            if (hash[0] == localValue1Hash ||
                hash[1] == localValue1Hash ||
                hash[2] == localValue2Hash) {
                throw new Exception("string,string,value did not get copies!");                
            }
                       
            if (!StubAdapter.isStub(localRef2.echoObject(new LocalHelloServant()))) {
                throw new Exception("localRef2.echoObject(impl) did not return a stub!");                
            }

            if (!StubAdapter.isStub(remoteRef.echoObject(new LocalHelloServant()))) {
                throw new Exception("remoteRef.echoObject(impl) did not return a stub!");                
            }
                      
            RemoteException[] array2 = {localValue1,localValue1};
            if (Util.copyObjects(array2,orb) == array2) {
                throw new Exception ("Util.copyObjects(array2) == array2");   
            }
            
            // Unexport our localImpl and make sure that stub is invalid...
            newTest( "unexportLocalObject" ) ; 
            PortableRemoteObject.unexportObject(localImpl);
            boolean failed = false;
            try {
                localRef2.echoString("hi");
            } catch (Exception e) {
                failed = true;
            }
            if (!failed) {
                throw new Exception("localRef2.echoString() succeeded on unexported impl.");    
            }
            
            del = StubAdapter.getDelegate( localRef2 ) ;
            so = del.servant_preinvoke( (org.omg.CORBA.Object)localRef2,
                "method",LocalHello.class);
            if (so != null) {
                throw new Exception("servant_preinvoke() did not return null");
            }

            if (!first) 
                helper.pass() ;
        } catch (Throwable thr) {
            helper.fail( thr ) ;
        } finally {
            helper.done() ;
            // Put the codebase back...
            JDKBridge.setLocalCodebase(currentCodebase);
            
            // Rename our class files back to the correct names...
            for (int i = 0; i < 2; i++) {
                File current = classFiles[i];
                boolean error = false;
                if (current != null) {
                    String path = current.getPath();
                    String origPath = path.substring(0,path.length()-1);
                    File orig = new File(origPath);
                    if (!current.renameTo(orig)) {
                        System.out.println("Failed to rename "+path+" to "+origPath);
                        error = true;
                    }
                }
                if (error) {
                    throw new Error("Rename failed.");
                }
            }
        }
    }
}
