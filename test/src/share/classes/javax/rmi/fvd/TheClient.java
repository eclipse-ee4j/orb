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

import java.util.Properties;
import java.io.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

public class TheClient {

    private static String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    private static void passed(){
        System.out.println(test.Util.HANDSHAKE);
        System.out.flush();
    }

    private static void failed(Throwable t){
        StringWriter strWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(strWriter);
        t.printStackTrace(printWriter);
        System.out.println("FVD test FAILED:\n"+strWriter.toString()+"\n <<< END STACK TRACE >>>");
        System.out.flush();
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
                        
            System.setSecurityManager(new javax.rmi.download.SecurityManager());
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
                Servant narrowTo = null;
                if ( (narrowTo = (Servant)
                      PortableRemoteObject.narrow(objref,Servant.class)) != null ) {

                    // Verify connection
                    String str = "hello";
                    String res = narrowTo.ping(str);
                    if (!res.equals(new String("ServantImpl:"+str)))
                        throw new Error("Connection bad!");

                    // Send a mismatched class
                    // i.e. a matching class hierarchy with differing fields
                    ParentClass mismatch = 
                        (ParentClass)Class.forName("javax.rmi.download.values.ClientA").newInstance();
                    if (mismatch == null)
                        throw new Error("Could not create javax.rmi.download.values.ClientA");

                    if (narrowTo.send(mismatch) != mismatch.getOriginalTotal())
                        throw new Error("Mismatched class not sent correctly!");

                    // Send a differing hierarchy
                    // - Sender (TheClient) has shallow hierarchy C->A whereas
                    //   receiver (TheServer) has deeper hierarchy C->B->A.
                    ParentClass shallowHierarchy = 
                        (ParentClass)Class.forName("javax.rmi.download.values.ClassC").newInstance();
                                        
                    if (shallowHierarchy == null)
                        throw new Error("Could not create javax.rmi.download.values.ClassA");

                    if (narrowTo.send(shallowHierarchy) != shallowHierarchy.getOriginalTotal())
                        throw new Error("shallowHierarchy class not sent correctly!");
                                        
                    // Send a differing hierarchy
                    // - Sender (TheClient) has deeper hierarchy E->D->A whereas
                    //   receiver (TheServer) has shallow hierarchy E->A.
                    ParentClass deeperHierarchy = 
                        (ParentClass)Class.forName("javax.rmi.download.values.ClassE").newInstance();
                                        
                    if (deeperHierarchy == null)
                        throw new Error("Could not create javax.rmi.download.values.ClassE");

                    if (narrowTo.send(deeperHierarchy) != 19) // ! 26
                        throw new Error("deeperHierarchy class not sent correctly!");

                    // Send a value with a member who's type (class) does
                    // not exist on the receiver's side (i.e. not codebase
                    // to download it from either).
                    ParentClass missingClassContainer = 
                        (ParentClass)Class.forName("javax.rmi.download.values.MissingContainer").newInstance();

                    if (missingClassContainer == null)
                        throw new Error("Could not create javax.rmi.download.values.MissingContainer");

                    if (narrowTo.send(missingClassContainer) != 5)
                        throw new Error("missingClassContainer class not sent correctly");

                    passed();
                                        
                                        
                }
                else throw new Error("Failed to find narrowTo");


            } catch (Throwable ex) {
                failed(ex);

            }        
        } catch (Exception ex) {
            failed(ex);

        }
    }
}

