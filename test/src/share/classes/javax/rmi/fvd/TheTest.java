/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package javax.rmi.fvd;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import java.io.IOException;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.Context;
import test.Util;
import java.util.Hashtable;
import java.util.Vector;
import org.glassfish.pfl.test.JUnitReportHelper;

public class TheTest extends test.Test {
    // This test runs the NameServer on port 1050.
    
    private static  String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };
    static Process nameServer  = null;
    static Process server      = null;
    static Process client      = null;

    public void setup() {
        try {
            nameServer  = Util.startNameServer("1050",true);
        } catch (IOException e) {
            System.out.println("Failed to start the name server: " + e);
        }
        try {
            // Now we need to start our test server. The test server will register with the NameServer.
            compileClasses();
        } catch(Throwable t) { 
            System.out.println("Compiling classes failed : "+t.toString());
        }
    }

    public  void run() {
        String testName     = TheTest.class.getName();
        JUnitReportHelper helper = new JUnitReportHelper( testName ) ;
        helper.start( "test1" ) ;
        boolean testPassed  = true;

        try {
            // The RMIClassLoader requires a security manager to be set
            //System.setSecurityManager(new javax.rmi.download.SecurityManager());
            //System.setSecurityManager(new java.rmi.RMISecurityManager());

            // First Compile the classes to generate the Stub and Tie 
            // files that are needed.  NOTE: This requires the latest
            // RMIC compiler that supports IIOP.

            // Create user.dir property (this is how the server knows
            // where the test value is but we (this client) does not).
            Vector properties = new Vector();
            String testPolicy = System.getProperty("java.security.policy");
            if (testPolicy!=null)
                properties.addElement("-Djava.security.policy="+testPolicy);
                        
                        
            // Start it
            String valueClasses = getClassesDirectory("values");
            server = Util.startServer("javax.rmi.fvd.TheServer",
                                      properties, valueClasses);
            
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
            // Resolve the Object Reference using JNDI/CosNaming
            java.lang.Object objref  = ic.lookup("TheFVDTestServer");

            // This test is designed to verify PortableRemoteObject.narrow

            try{
                // Now try from separate client that has no codebase of its own

                Vector properties2 = new Vector();
                properties.addElement("-Djava.security.policy="+testPolicy);

                // Start it
                client = Util.startServer("javax.rmi.fvd.TheClient", 
                    properties, getClassesDirectory("values2"));

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
            // Kill the client
            if (client != null) {
                client.destroy();
            }

            // Make sure we kill the test server...

            if (server != null) {
                server.destroy();
            }
  
            // Make sure we kill the NameServer...
            
            if (nameServer != null) {
                nameServer.destroy();
            }

        }

        if ( testPassed == true ) {
            status = null;
        } else {
            status = new Error("FullValueDescription Test Failed");
        }
    }

    public static void shutdown(){
        if (client != null) {
            client.destroy();
            client = null;
        }

        if (server != null) {
            server.destroy();
            server = null;
        }
  
        if (nameServer != null) {
            nameServer.destroy();
            nameServer = null;
        }
    }

    // Compiling ComboInterface causes the compiler to compile
    // all the other classes that need to be compiled.
    private  void compileClasses () throws Exception {
        String arg = "-iiop";
        String[] additionalArgs = null;
        String[] classes = {"javax.rmi.fvd.ServantImpl", "javax.rmi.fvd.LogImpl"};
        
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
