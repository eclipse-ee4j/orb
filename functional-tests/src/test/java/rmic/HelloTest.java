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

import test.ServantContext;
import test.RemoteTest;
import java.rmi.AccessException;
import java.util.Properties;
import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.io.File;
import test.WebServer;
import test.Util;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA.portable.Delegate;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBData ;
import org.glassfish.pfl.test.JUnitReportHelper;

/*
 * @test
 */
public class HelloTest extends RemoteTest {

    private static final String servantName     = "HelloServer";
    private static final String servantClass    = "rmic.HelloImpl";
    private static final String[] compileEm     =   {
        "rmic.HelloImpl",
        "rmic.RemoteObjectServer",
    };

    private JUnitReportHelper helper = null ;
    private boolean firstTest = true ;

    private void nextTest( String name ) {
        if (firstTest)
            firstTest = false ;
        else
            helper.pass() ;

        helper.start( name ) ;
    }

    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    @Override
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-alwaysGenerate", "-keepgenerated"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'.
     */
    @Override
    protected boolean generateStubsOnlyOnce () {
        return false;
    }

    /**
     * Return true if stubs should be generated in an external process.
     */
    @Override
    protected boolean generateStubsExternally () {
        return false;
    }
    
    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    public void doTest (ServantContext context) throws Throwable {
        WebServer webServer = null;
        
        final boolean useLocalServants ;
        useLocalServants = getArgs().get(LOCAL_SERVANTS_FLAG) != null ;
        helper = new JUnitReportHelper( this.getClass().getName() 
            + ( useLocalServants ? "_local" : "" ) ) ;

        try {
            ORB orb = (ORB)(context.getORB()) ;
            ORBData odata = orb.getORBData() ;
            boolean usesDynamicStubs = ((ORB)context.getORB()).getPresentationManager().useDynamicStubs() ;

            String outputDirPath = (String) getArgs().get(test.Test.OUTPUT_DIRECTORY);
            if (outputDirPath == null) {
                throw new Exception ("Must have valid output directory set with -output option.");
            }

            File rootDir = new File(outputDirPath);
            File rmicDir = new File(rootDir,"rmic");
            
            if (!usesDynamicStubs) {
                nextTest( "setup" ) ;
                // Rename _RemoteObject_Stub.class so can only be loaded by
                // WebServer...
      
                String origName = "_RemoteObject_Stub.class";
                String newName = "_RemoteObject_Stub.clz";
                
                File origFile = new File (rmicDir,origName);
                File newFile = new File (rmicDir,newName);
                if (origFile.exists() && newFile.exists()) {
                    newFile.delete();
                }
                
                origFile.renameTo(newFile);
                if (newFile.exists() && !origFile.exists()) {
                    //                System.out.println("Found newFile: " + newFile.getPath());
                } else {
                    throw new Exception ("_RemoteObject_Stub rename failed.");
                }

                // Create our aliases and start up the WebServer...
                Properties aliases = new Properties();
                aliases.put("/rmic/" + origName,"/rmic/" + newName);
                int port = Util.getHttpServerPort();
                webServer = new WebServer(port,rootDir,1,aliases,null);
                webServer.start();
            }

            nextTest( "getStubs" ) ;

            int stubKind; // 0 = remote, 1 = localremote (i.e. non-optimized), 2 = local

            boolean mustBeRemote;

            if (useLocalServants) {
                if (getArgs().get(NO_LOCAL_STUBS_FLAG) != null) {
                    stubKind = 1; // localremote (i.e. non-optimized)
                    mustBeRemote = true;
                    Util.setDefaultCodeBase(true);
                    if (verbose) System.out.println("(No local stubs) ");
                } else {
                    stubKind = 2; // local
                    mustBeRemote = false;
                    Util.setDefaultCodeBase(true);
                }
            } else {
                stubKind = 0; // remote
                mustBeRemote = true;
            }
            
            // Start up our servant...

            Object initialRef = context.startServant(servantClass,servantName,true,iiop);
            Hello objref = (Hello) PortableRemoteObject.narrow(initialRef,Hello.class);

            // Make sure we have the correct stub kind...

            Delegate del = StubAdapter.getDelegate( objref ) ;
            ServantObject so = del.servant_preinvoke(
                (org.omg.CORBA.Object)objref,"method",
                Hello.class);
            boolean isLocal = (so != null);
            
            if (mustBeRemote && isLocal) {
                throw new Exception("Supposed to be remote stub, but is local: " + 
                    objref.getClass().getName());
            }
            if (!mustBeRemote && !isLocal) {
                throw new Exception("Supposed to be local stub, but is remote:" + 
                    objref.getClass().getName());
            }
            
            // Make sure stub downloading works (if not local)...
            
            if (stubKind != 2) {
                nextTest( "testStubDownloading" ) ; 
                // Ask objref to publish an instance of RemoteObject...
                
                objref.publishRemoteObject("stubDownloadTest");
                
                // Look it up in the name server...
                
                Object lookupResult = context.getNameContext().lookup("stubDownloadTest");
                
                // Narrow to expected type...
                
                RemoteObject ro = (RemoteObject) PortableRemoteObject.narrow(lookupResult,
                    RemoteObject.class);
                
                // Check the codebase...
                    
                IOR ior = orb.getIOR( (org.omg.CORBA.Object)ro, false ) ;
                String localCodeBase = ior.getProfile().getCodebase();
                        
                if (localCodeBase == null) {
                    throw new Exception("localCodeBase == null");
                }
                        
                String remoteCodeBase = ro.getCodeBase();
                        
                if (remoteCodeBase == null) {
                    throw new Exception("localCodeBase == null");
                }

                if (!remoteCodeBase.equals(localCodeBase)) {
                    throw new Exception("localCodeBase (" + localCodeBase +
                        ") != remoteCodeBase (" + remoteCodeBase +")");
                }
                        
                // Check the class loader, unless we are using dynamic stubs: dynamic stus
                // are never downloaded.
                if (!usesDynamicStubs) {
                    ClassLoader loader = ro.getClass().getClassLoader();
                            
                    if (loader != null) {       
                        String loaderName = loader.getClass().getName();

                        if (!loaderName.startsWith("sun.rmi.server.LoaderHandler")) {
                            throw new Exception (
                                "Stub downloaded with wrong loader on 1.2: " + 
                                loaderName);   
                        }
                    } else {
                        throw new Exception("Got null loader for "+ro.getClass().getName());   
                    }
                }
            }

            nextTest( "testHelloString" ) ;
            if (!objref.sayHello().equals("hello")) {
                throw new Exception("HelloTest: sayHello() failed");
            }

            nextTest( "testIntSum" ) ;
            if (objref.sum(10,10) != 20) {
                throw new Exception("HelloTest: sum(10,10) failed");
            }

            nextTest( "testStringConcat" ) ;
            if (!objref.concatenate("Here"," This").equals("Here This")) {
                throw new Exception("HelloTest: concatenate(\"Here\",\" This\") failed");
            }

            nextTest( "testObjectByValue" ) ;
            ObjectByValue value = new ObjectByValue(5,7," It's ","Me!");

            if (!objref.checkOBV(value).equals("The Results are: 12 It's Me!")) {
                throw new Exception("HelloTest: checkOBV(value) failed");
            }

            nextTest( "testObjectByValue2" ) ;
            ObjectByValue result = objref.getOBV();

            if (!result.equals(value)) {
                throw new Exception("HelloTest: getOBV() failed");
            }

            nextTest( "testRemoteObject" ) ;
            Hello remoteObject = objref.getHello ();

            if (!remoteObject.sayHello().equals("hello")) {
                throw new Exception("HelloTest: remoteObject.sayHello() failed");
            }

            nextTest( "testRemoteClassName" ) ;
            String remoteClassName = remoteObject.getClass().getName();
            String matchName = "rmic._Hello_Stub";
            if (stubKind == 2) matchName = "rmic._Hello_Stub";

            // We cannot assume that we know the stub class name if we are using dynamic stubs.
            if (!usesDynamicStubs && !remoteClassName.equals(matchName)) {
                throw new Exception("HelloTest: remoteObject.getClass() failed: " + remoteClassName);
            }

            nextTest( "testNullObjref" ) ; 
            if (objref.echoRemote(null) != null) {
                throw new Exception("HelloTest: null object reference failed.");
            }
            
            nextTest( "testNullAbstract" ) ;
            if (objref.echoAbstract(null) != null) {
                throw new Exception("HelloTest: null abstract object failed.");
            }

            nextTest( "testSingleArray" ) ;
            int[] array1 = {0,5,7,9,11,13};
            int[] array1Echo = remoteObject.echoArray(array1);

            for (int i = 0; i < array1.length; i++) {
                if (array1[i] != array1Echo[i]) {
                    throw new Exception("HelloTest: echoArray (int[]) failed");
                }
            }

            nextTest( "test2DArray" ) ;
            long[][] array2 =   {
                {9,8,7,6,1},
                {18,4,6},
                {0,5,7,9,11,13}
            };

            long[][] array2Echo = remoteObject.echoArray(array2);

            for (int i = 0; i < array2.length; i++) {
                for (int j = 0; j < array2[i].length; j++) {
                    if (array2[i][j] != array2Echo[i][j]) {
                        throw new Exception("HelloTest: echoArray (int[][]) failed");
                    }
                }
            }

            nextTest( "test3DArray" ) ;
            short[][][] dim3 = {
                {
                    {0,8,7,6,1},
                    {1,5,7,13},
                    {2,4,6},
                },
                {
                    {3,4,10},
                    {4,5,7,9,13}
                },
                {
                    {5,4,6},
                    {6,5,8,9,11,13},
                    {7,8,7,6,1},
                    {8,8,7,6,9},
                },
            };

            short[][][] dim3Echo = remoteObject.echoArray(dim3);

            for (int i = 0; i < dim3.length; i++) {
                for (int j = 0; j < dim3[i].length; j++) {
                    for (int k = 0; k < dim3[i][j].length; k++) {
                        if (dim3[i][j][k] != dim3[i][j][k]) {
                            throw new Exception("HelloTest: echoArray (short[][][]) failed");
                        }
                    }
                }
            }

            nextTest( "testSingleObjectArray" ) ;
            ObjectByValue[] array3 =    {
                new ObjectByValue(5,10,"a","f"),
                new ObjectByValue(6,11,"b","g"),
                new ObjectByValue(7,12,"c","h"),
                new ObjectByValue(8,13,"d","i"),
                new ObjectByValue(9,14,"e","j"),
            };

            ObjectByValue[] array3Echo = remoteObject.echoArray(array3);
            for (int i = 0; i < array3.length; i++) {
                if (!array3[i].equals(array3Echo[i])) {
                    throw new Exception("HelloTest: echoArray (ObjectByValue[]) failed");
                }
            }

            nextTest( "test2DObjectArray" ) ;
            ObjectByValue[][] array4 =   {   {
                new ObjectByValue(0,10,"a","g"),
                new ObjectByValue(0,11,"b","h"),
                new ObjectByValue(0,12,"c","i"),
            },
                                             {
                                                 new ObjectByValue(1,13,"d","j"),
                                                 new ObjectByValue(1,14,"e","k"),
                                             },
                                             {
                                                 new ObjectByValue(2,15,"f","l"),
                                             }
            };

            ObjectByValue[][] array4Echo = remoteObject.echoArray(array4);

            for (int i = 0; i < array4.length; i++) {
                for (int j = 0; j < array4[i].length; j++) {
                    if (!array4[i][j].equals(array4Echo[i][j])) {
                        throw new Exception("HelloTest: echoArray (ObjectByValue[][]) failed");
                    }
                }
            }

            nextTest( "testReadWriteAbstract" ) ;
            AbstractObject[] remotes = remoteObject.getRemoteAbstract();
            AbstractObject[] array5 =    {
                new ValueObject(111),
                remotes[0],
                new ValueObject(222),
                remotes[1],
                new ValueObject(333),
                new ValueObject(444),
                remotes[2],
            };

            for (int i = 0; i < array5.length; i++) {
                AbstractObject abs = remoteObject.echoAbstract(array5[i]);

                if (array5[i].getValue() != abs.getValue()) {
                    throw new Exception("HelloTest: echoAbstract (AbstractObject) failed (getValue)");
                }

                if (array5[i].isValue() != abs.isValue()) {
                    throw new Exception("HelloTest: echoAbstract (AbstractObject) failed (isValue)");
                }
            }

            nextTest( "testApplicationException" ) ;
            boolean caughtExpected = false;

            try {
                remoteObject.throwHello(17,"Seventeen");
            } catch (HelloException e) {
                if (e.getCount() == 17 && e.getMessage().startsWith("Seventeen")) {
                    caughtExpected = true;
                } else {
                    throw new Exception("HelloTest: throwHello got count = " + 
                        e.getCount() + ", message = " + e.getMessage());
                }
            }
            if (!caughtExpected) {
                throw new Exception("HelloTest: throwHello failed");
            }

            nextTest( "testSystemException" ) ;
            caughtExpected = false;

            try {
                remoteObject.throw_NO_PERMISSION("ACK!",99);
            } catch (AccessException e) {
                if (e.getMessage().startsWith("CORBA NO_PERMISSION 99")) {
                    caughtExpected = true;
                } else {
                    throw new Exception("HelloTest: throwSystem got " + e.toString());
                }
            }
            if (!caughtExpected) {
                throw new Exception("HelloTest: throwSystem failed");
            }

            nextTest( "testError" ) ;
            caughtExpected = false;

            try {
                remoteObject.throwError(new Error("Error test"));
            } catch (java.rmi.ServerError e) {
                if (e.detail.getMessage().startsWith("Error test")) {
                    caughtExpected = true;
                } else {
                    throw new Exception("HelloTest: throwError got " + e.detail.toString());
                }
            }
            if (!caughtExpected) {
                throw new Exception("HelloTest: throwError failed");
            }

            nextTest( "testRemoteException" ) ;
            caughtExpected = false;

            try {
                remoteObject.throwRemoteException(new RemoteException("RemoteException test"));
            } catch (java.rmi.ServerException e) {
                if (e.detail.getMessage().startsWith("RemoteException test")) {
                    caughtExpected = true;
                } else {
                    throw new Exception("HelloTest: throwRemoteException got " + e.detail.toString());
                }
            }
            if (!caughtExpected) {
                throw new Exception("HelloTest: throwRemoteException failed");
            }

            nextTest( "testRuntimeException" ) ;
            caughtExpected = false;
            
            try {
                remoteObject.throwRuntimeException(new RuntimeException("RuntimeException test"));
            } catch (java.rmi.ServerRuntimeException e) {
                if (e.detail.getMessage().startsWith("RuntimeException test")) {
                    caughtExpected = true;
                    throw new Exception(
                        "HelloTest: throwRuntimeException got ServerRuntimeException on 1.2 vm");
                } else {
                    throw new Exception("HelloTest: throwRuntimeException got " + e.detail.toString());
                }
            } catch (RuntimeException e) {
                if (e.getMessage().startsWith("RuntimeException test")) {
                    caughtExpected = true;
                } else {
                    throw new Exception("HelloTest: throwRuntimeException got " + e.toString());
                }
            }
            if (!caughtExpected) {
                throw new Exception("HelloTest: throwRuntimeException failed");
            }

            nextTest( "testCharValue" ) ;
            CharValue v1 = remoteObject.echoCharValue(new CharValue('%'));
            if (v1.getValue() != '%') {
                throw new Exception("HelloTest: echoCharValue failed.");
            }

            CharValue v2 = remoteObject.echoCharValue(new CharValue('\u05d0'));
            if (v2.getValue() != '\u05d0') {
                throw new Exception("HelloTest: echoCharValue failed.");
            }

            nextTest( "testObject" ) ;
            String o1 = new String("An Object");
            Object o2 = remoteObject.echoObject(o1);
            if (!o1.equals(o2)) {
                throw new Exception("HelloTest: echoObject failed.");
            }

            nextTest( "testProperties" ) ;
            Properties props1 = new Properties();
            props1.put("one", v1);
            props1.put("two", v2);

            Properties props2 = (Properties) remoteObject.echoObject(props1);
            if (!equals(props1,props2)) {
                throw new Exception("HelloTest: echoObject failed.");
            }

            nextTest( "testSerializable" ) ;
            Serializable s1 = remoteObject.echoSerializable(o1);
            if (!s1.equals(o1)) {
                throw new Exception("HelloTest: echoSerializable failed.");
            }

            nextTest( "testPropsSerializable" ) ;
            Properties props3 = (Properties) remoteObject.echoSerializable(props1);
            if (!equals(props1,props3)) {
                throw new Exception("HelloTest: echoSerializable failed.");
            }

            if (!firstTest)
                helper.pass() ;
        } catch (Exception exc) {
            System.out.println( "Test failed with exception " + exc ) ;
            exc.printStackTrace() ;
            helper.fail( exc ) ;
            throw exc ;
        } finally {
            helper.done() ;
            if (context != null) {
                context.destroy();
            }
            java.util.Hashtable myArgs = getArgs();
            myArgs.remove(LOCAL_SERVANTS_FLAG);
            myArgs.remove(NO_LOCAL_STUBS_FLAG);
            myArgs.remove(SKIP_RMIC_FLAG);
            if (webServer != null) {
                webServer.quit();
            }
            Thread.sleep(500);  // Wait for name server to go down.
        }
    }

    boolean equals (Properties one, Properties two) {
        boolean result = false;
        if (one.size() == two.size()) {
            Enumeration e = one.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                Object vOne = one.get(key);
                Object vTwo = two.get(key);

                if (vOne != null && vTwo != null && !vOne.equals(vTwo)) {
                    break;
                }
            }
            result = true;
        }
        return result;
    }
}
