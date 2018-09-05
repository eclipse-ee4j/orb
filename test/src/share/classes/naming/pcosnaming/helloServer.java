/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.pcosnaming;

import HelloApp._helloImplBase ;
import corba.framework.Controller;
import corba.framework.InternalProcess;
import java.io.PrintStream;

import java.util.Properties;
import java.util.Hashtable;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

class helloServant extends _helloImplBase
{
    public void sayHello()
    {
        helloServer.output.println("Servant: In helloServant.sayHello()");
    }

    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

public class helloServer implements InternalProcess 
{
    public NamingContext ncRef;
    public helloServant helloRef;
    public static PrintStream output;
    public static PrintStream errors;

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        Controller orbd = (Controller)extra.get("orbd");
        Controller client = (Controller)extra.get("client");

        helloServer.output = out;
        helloServer.errors = err;

        ORB orb = ORB.init(args, environment);
        
        // create servant and register it with the ORB
        helloRef = new helloServant();
        orb.connect(helloRef);
        
        // get the root naming context
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        ncRef = NamingContextHelper.narrow(objRef);
        
        // bind the Object Reference in Naming
        NameComponent nc1 = new NameComponent("HelloObj1", "");
        NameComponent path1[] = {nc1};
        ncRef.rebind(path1, helloRef);
        
        output.println("Killing and restarting ORBD...");

        orbd.stop();
        orbd.start();
        output.println("ORBD restarted");

        // Give a little more time
        Thread.sleep(1000);
        
        NamingContext ncRef1 = ncRef.new_context(); 
        output.println( "Persistent Reference was valid");
        
        NameComponent nc2 = new NameComponent("HelloContext1", "");
        NameComponent path2[] = {nc2};
        ncRef.rebind_context( path2, ncRef1 );
        
        output.println("Killing and restarting ORBD...");
        orbd.stop();
        orbd.start();
        output.println("ORBD restarted");
       
        Thread.sleep(1000);

        NamingContext ncRef2 = ncRef.new_context( ); 
        NameComponent nc3 = new NameComponent("HelloContext2", "");
        NameComponent path3[] = {nc3};
        ncRef1.rebind_context( path3, ncRef2 );
        output.println(" Persistent Reference of NCREF1 was valid....... " );
        
        NameComponent nc4 = new NameComponent( "HelloObj2", "");
        NameComponent path4[] = {nc4};
        ncRef1.rebind( path4, helloRef );
        
        output.println("Killing and restarting ORBD...");
        orbd.stop();
        orbd.start();
        output.println("ORBD restarted");

        Thread.sleep(1000);

        NameComponent nc5 = new NameComponent( "HelloObj3","");
        NameComponent path5[] = {nc5};
        ncRef2.rebind( path5, helloRef ); 
        
        output.println( " Persistent Reference of NCREF2 was valid....... " );
        
        output.println("Starting client...");
        
        // Not very intuitive, but start the client in a separate process.
        client.start();
        client.waitFor();
        
        output.println("Client finished, exiting...");

        output.flush();

        // orb.shutdown(true);
    }
}
