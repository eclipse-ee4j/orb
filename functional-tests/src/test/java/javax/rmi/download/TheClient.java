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

package javax.rmi.download;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

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
        System.out.println("Download test FAILED:\n"+strWriter.toString()+"\n <<< END STACK TRACE >>>");
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
                                        
                    passed();
                                        
                                        
                }
            } catch (Throwable ex) {
                failed(ex);
                ex.printStackTrace();
            }        
        } catch (Exception ex) {
            failed(ex);
            ex.printStackTrace(System.out);
            System.out.println();
            System.out.flush();
        }
    }
}

