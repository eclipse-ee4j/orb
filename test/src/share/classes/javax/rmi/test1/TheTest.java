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

package javax.rmi.test1;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import java.util.Properties;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;
import test.Util;
import java.util.Hashtable;
import org.glassfish.pfl.test.JUnitReportHelper;

public class TheTest extends test.Test {
    // This test runs the NameServer on port 1050.
    private static  String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    public  void run() {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() ) ;

        String testName     = new TheTest().getClass().getName();
        Process nameServer  = null;
        Process server      = null;
        boolean testPassed  = true;

        try {
            // First Compile the classes to generate the Stub and Tie 
            // files that are needed.  
            if (!getArgs().containsKey("-normic")) {
                compileClasses();
            }
            
            // Now we need to start the NameServer and
            // our test server. The test server will register
            // with the NameServer.
            
            nameServer  = Util.startNameServer("1050",true);
            server      = Util.startServer("javax.rmi.test1.TheServer");
            
            // Lets setup some properties that we are using
            // for this test and then create the ORB Object...

            Properties props = System.getProperties();
            
            props.put(  "java.naming.factory.initial",
                        JndiConstants.COSNAMING_CONTEXT_FACTORY);
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");
            
            props.put(  "org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
                
            // We are going to use JNDI/CosNaming so lets go ahead and
            // create our root naming context.  NOTE:  We setup CosNaming
            // as our naming plug-in for JNDI by setting properties above.
            Hashtable env = new Hashtable();
            env.put(  "java.naming.corba.orb", orb);
            
            Context ic = new InitialContext(env);
            
            // Let the test begin...
            helper.start( "test1" ) ;
            // Resolve the Object Reference using JNDI/CosNaming
            java.lang.Object objref  = ic.lookup("TheTestServer");

            // This test is designed to verify PortableRemoteObject.narrow
          
            try {
                RemoteInterface1 narrowTo = null;
                if ( (narrowTo = (RemoteInterface1)
                      PortableRemoteObject.narrow(objref,RemoteInterface1.class)) != null ) {
                    if (!narrowTo.EchoRemoteInterface1().equals("EchoRemoteInterface1")) {
                        throw new Exception("javax.rmi.test1.TheTest: EchoRemoteInterface1() narrow failed");
                    }
                }
            } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
                ex.printStackTrace();
                testPassed = false;
            }


            try {
                RemoteInterface2 narrowTo = null;
                if ( (narrowTo = (RemoteInterface2)
                      PortableRemoteObject.narrow(objref,RemoteInterface2.class)) != null ) {
                    if (!narrowTo.EchoRemoteInterface2().equals("EchoRemoteInterface2")) {
                        throw new Exception("javax.rmi.test1.TheTest: EchoRemoteInterface2() narrow failed");
                    }
                }
            } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
                ex.printStackTrace();
                testPassed = false;
            }

            try {
                RemoteInterface3 narrowTo = null;
                if ( (narrowTo = (RemoteInterface3)
                      PortableRemoteObject.narrow(objref,RemoteInterface3.class)) != null ) {
                    if (!narrowTo.EchoRemoteInterface3().equals("EchoRemoteInterface3")) {
                        throw new Exception("javax.rmi.test1.TheTest: EchoRemoteInterface3() narrow failed");
                    }
                }
            } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
                ex.printStackTrace();
                testPassed = false;
            }

            try {
                SingleRemoteInterface narrowTo = null;
                if ( (narrowTo = (SingleRemoteInterface)
                      PortableRemoteObject.narrow(objref,SingleRemoteInterface.class)) != null ) {
                    if (!narrowTo.EchoSingleRemoteInterface().equals("EchoSingleRemoteInterface")) {
                        throw new Exception("javax.rmi.test1.TheTest: SingleRemoteInterface() narrow failed");
                    }
                }
            } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
                ex.printStackTrace();
                testPassed = false;
            }

        } catch (Exception ex) {
            System.out.println(testName + " FAILED.");
            ex.printStackTrace();
            testPassed = false;
        } finally {
            if (server != null) {
                server.destroy();
            }
  
            if (nameServer != null) {
                nameServer.destroy();
            }
        }

        if ( testPassed == true ) {
            helper.pass() ;
            status = null;
        } else {
            helper.fail( "test failed" ) ;
            status = new Error("PortableRemoteObject.narrow Test Failed");
        }

        helper.done() ;
    }

    // Compiling ComboInterface cause the compiler to compile
    // all the other classes that need to be compiled.
    
    private  void compileClasses () throws Exception
    {
        String arg = "-iiop";
        String[] additionalArgs = null;
        String[] classes = {"javax.rmi.test1.ComboInterfaceImpl"};
        
        // Create the additional args array...
               
        String outputDirectory = null;
        int length = 3;
        Hashtable flags = getArgs();
        if (flags.containsKey(test.Test.OUTPUT_DIRECTORY)) {
            outputDirectory = (String)flags.get(test.Test.OUTPUT_DIRECTORY);
            length += 2;
        }
        additionalArgs = new String[length];
        int offset = 0;
        
        if (outputDirectory != null) {
            additionalArgs[offset++] = "-d";
            additionalArgs[offset++] = outputDirectory;
        }
        additionalArgs[offset++] = "-Xreverseids";
        additionalArgs[offset++] = "-alwaysgenerate";
        additionalArgs[offset++] = "-keepgenerated";
        
        // Run rmic...
        
        Util.rmic(arg,additionalArgs,classes,false);
    }
}
