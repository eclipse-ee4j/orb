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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import java.util.Hashtable;
import java.util.Properties;

public class LogImpl implements Log/*, java.awt.event.ActionListener*/ {
    private static String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };
    private static Log logServer = null;
    /*
      Frame f = null;
      TextArea ta = null;
        
      public void actionPerformed(ActionEvent e){
      try{
      logMssg("LogImpl","Shutting down");
      TheTest.shutdown();
      logMssg("LogImpl","Shutdown completed");
      System.exit(1);
      }
      catch(Throwable t){
      logMssg("LogImpl",t.getMessage());

      }

      }
      private void setup(){

      f = new Frame("Log Window");
      Button b = new Button("Shutdown test");
      f.setLayout(new BorderLayout());
      f.add("South", b);
      b.addActionListener(this);
      ta = new TextArea();
      f.add("Center", ta);
      f.show();
      f.reshape(100,20,350,350);
                
      }
    */
    public void log(String who, String what) throws java.rmi.RemoteException {
        /*
          if (f == null)
          setup();
          String text = ta.getText();
          text = text + "\n" + who + ":"+what;
          ta.setText(text);
        */
    }   

    public static void logMssg(String who, String what){
        try{
            if (logServer == null)
                connect();
            logServer.log(who,what);
        }
        catch(Throwable t){}
    }

    public static void open(){
        try{
            Properties props = System.getProperties();
        
            props.put(  "java.naming.factory.initial",
                        JndiConstants.COSNAMING_CONTEXT_FACTORY);
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");
            
            props.put(  "org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
            
            // create an RMI Servant.  The Servant will actually
            // handle the users request.
            
            LogImpl servant = new LogImpl();
            
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
            ic.rebind("LogServer", servantsTie);
            java.lang.Object objref  = ic.lookup("LogServer");
                        
            // This test is designed to verify PortableRemoteObject.narrow
                        
            logServer= (Log)
                PortableRemoteObject.narrow(objref,Log.class);
        }
        catch(Throwable t){}
    }

    private static void connect(){
        try{
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
            java.lang.Object objref  = ic.lookup("LogServer");
                        
            // This test is designed to verify PortableRemoteObject.narrow
                        
            logServer= (Log)
                PortableRemoteObject.narrow(objref,Log.class);

            // com.sun.corba.ee.impl.io.ValueUtility.logEnabled = true;
        }
        catch(Throwable t){}
    }
}
