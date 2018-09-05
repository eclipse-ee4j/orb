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

package javax.rmi.download;

import com.sun.corba.ee.spi.JndiConstants;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import test.Util;
import test.WebServer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.File;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/*
 * @test
 */
public class TheTest extends test.Test {
    // This test runs the NameServer on port 1050.
    private static  String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    public  void run() {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() ) ;

        String testName     = new TheTest().getClass().getName();
        Process nameServer  = null;
        Process server      = null;
        Process client      = null;
        boolean testPassed  = true;
        WebServer webServer = null;

        try {
            // The RMIClassLoader requires a security manager to be set
            //  System.setSecurityManager(new javax.rmi.download.SecurityManager());
            //System.setSecurityManager(new java.rmi.RMISecurityManager());

            // First Compile the classes to generate the Stub and Tie 
            // files that are needed.  NOTE: This requires the latest
            // RMIC compiler that supports IIOP.

            if (!getArgs().containsKey("-normic")) {
                compileClasses();
            }
            
            // Now we need to start the NameServer and
            // our test server. The test server will register
            // with the NameServer.
            
            nameServer  = Util.startNameServer("1050",true);

            // Start up the HTTP server
            int port = Util.getHttpServerPort();
            File rootDir = new File(System.getProperty("http.server.root.directory"));
            webServer = new WebServer(port,rootDir,1);
            webServer.start();

            // Create user.dir property (this is how the server knows
            // where the test value is but we (this client) does not).
            Vector properties = new Vector();
            properties.addElement("-Djava.rmi.server.codebase=" + (String)System.getProperty("java.rmi.server.codebase"));
            String testPolicy = (String)System.getProperty("java.security.policy");
            if (testPolicy!=null)
                properties.addElement("-Djava.security.policy="+testPolicy);
                
                        

            //Class c = java.rmi.server.RMIClassLoader.loadClass(new java.net.URL(System.getProperty("java.rmi.server.codebase")), "javax.rmi.download.values.TheValueImpl");
            //System.out.println("Found : " + c.toString());

            // Start it
            server      = Util.startServer("javax.rmi.download.TheServer", 
                                           properties);
            
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
            java.lang.Object objref  = ic.lookup("TheDownloadTestServer");

            // This test is designed to verify PortableRemoteObject.narrow
            try{
                Servant narrowTo = null;
                if ( (narrowTo = (Servant)
                      PortableRemoteObject.narrow(objref,Servant.class)) != null ) {
                    Servant serv1 = narrowTo;
                    String mssg = narrowTo.getValue().sayHello();
                    if (!mssg.equals("Hello, world!")) {
                        System.err.println(mssg);
                        throw new Exception("javax.rmi.download.TheTest: SingleRemoteInterface() narrow failed");
                    }
                                        
                    IIOPTestSerializable ones = new IIOPTestSerializable();
                    ones.setRef(serv1);
                    IIOPTestSerializable twos = (IIOPTestSerializable)serv1.testWriteReadObject(ones);
                    Servant serv2 = twos.getRef();
                    String mssg2 = serv2.EchoSingleRemoteInterface();
                    if (!mssg2.equals("EchoSingleRemoteInterface")) {
                        System.err.println(mssg);
                        throw new Exception("javax.rmi.download.TheTest: Reverse pass failed");
                    }   
                }
                                        
                // Now try from separate client that has no codebase of its own
                Vector properties2 = new Vector();
                properties.addElement("-Djava.security.policy="+testPolicy);
                client = Util.startServer("javax.rmi.download.TheClient", 
                                          properties);
                helper.pass() ;
            } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
                ex.printStackTrace();
                testPassed = false;
                helper.fail( ex ) ;
            }
        } catch (Exception ex) {
            System.out.println(testName + " FAILED.");
            ex.printStackTrace();
            testPassed = false;
            helper.fail( ex ) ;
        } finally {
            helper.done() ;

            if (client != null) {
                client.destroy();
            }

            if (server != null) {
                server.destroy();
            }
  
            if (nameServer != null) {
                nameServer.destroy();
            }
                        
            if (webServer != null)
                webServer.quit();
        }

        if ( testPassed == true ) {
            status = null;
        } else {
            status = new Error("PortableRemoteObject.narrow Test Failed");
        }

    }

    // Compiling ComboInterface cause the compiler to compile
    // all the other classes that need to be compiled.
    
    private  void compileClasses () throws Exception
    {
        String arg = "-iiop";
        String[] additionalArgs = null;
        String[] classes = {"javax.rmi.download.ServantImpl"};
        
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
