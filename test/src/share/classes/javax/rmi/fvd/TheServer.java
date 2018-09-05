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

package javax.rmi.fvd;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import java.util.Properties;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

public class TheServer {

    // This test runs the NameServer on port 1050.

    private static String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    public static void main(String[] args) {
        try {

            // The RMIClassLoader requires a security manager to be set
            System.setSecurityManager(new javax.rmi.download.SecurityManager());
            //System.setSecurityManager(new java.rmi.RMISecurityManager());

            // Lets setup some properties that we are using
            // for this test and then create the ORB Object...
            
            Properties props = System.getProperties();
        
            props.put(  "java.naming.factory.initial",
                        JndiConstants.COSNAMING_CONTEXT_FACTORY);
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");

            props.put("org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
            
            // create an RMI Servant.  The Servant will actually
            // handle the users request.
            
            ServantImpl servant = new ServantImpl();
            
            // Let use PortableRemoteObject to export our servant.
            // This same method works for JRMP and IIOP.
            
            PortableRemoteObject.exportObject(servant);
            
            // Once the Object is exported we are going to link it to
            // our ORB.  To do this we need to get the Tie associated
            // with our Servant.  PortableRemoteObject.export(...) 
            // create a Tie for us.  All we have to do is to retrieve the
            // Tie from javax.rmi.CORBA.Util.getTie(...);
            
            Tie servantsTie = javax.rmi.CORBA.Util.getTie(servant);
            
            // Now lets set the orb in the Tie object.  The Sun/IBM
            // ORB will perform a orb.connect.  So at this point the
            // Tie is connected to the ORB and ready for work.
            servantsTie.orb(orb);

        
            // We are using JNDI/CosNaming to export our object so we
            // need to get the root naming context.  We use the properties
            // set above to initialize JNDI.
            
            Hashtable env = new Hashtable();
            env.put(  "java.naming.corba.orb", orb);
            
            Context ic = new InitialContext(env);

            // Now lets Export our object by publishing the object
            // with JNDI
            ic.rebind("TheFVDTestServer", servant);

            // Self-Test
            // resolve the Object Reference using JNDI
            Servant iServant = (Servant)
                PortableRemoteObject.narrow(ic.lookup("TheFVDTestServer"),
                                            Servant.class);

            System.out.println(test.Util.HANDSHAKE);
            System.out.flush();
        
            // wait for object invocation
            Object sync = new Object();
            synchronized (sync) { sync.wait(); }

        } catch (Exception ex) {

            ex.printStackTrace(System.out);
            System.out.println();
            System.out.flush();
        }
    }
}
