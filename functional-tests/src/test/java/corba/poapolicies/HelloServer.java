/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poapolicies;

import Util.FactoryHelper;
import org.omg.PortableServer.POA;

class Waiter extends Thread {
    BasicObjectFactoryImpl f;
    POA p;
    public Waiter(POA p, BasicObjectFactoryImpl f) {
        this.f = f;
        this.p = p;
    }
    public void run() {
        try {
            synchronized (f.doneCV) {
                f.doneCV.wait();
                p.destroy(true, true);
            }
        } catch (InterruptedException ex) { }
    }
}

public class HelloServer 
{
    public static boolean debug = false;

    public static void main(String[] args) {
        try {
            String debugp = System.getProperty("DebugPOA");
            if (debugp != null)
                debug = true;

            POAFactory f = null;
            String factory = System.getProperty("POAFactory");
            System.out.println("Server will use factory:" + factory);

            System.out.println("Class path: " + System.getProperty("java.class.path"));


            if (factory != null && !factory.equals(""))
                f = (POAFactory) Class.forName(factory).newInstance();

            Utility u = new Utility(args);

            POA poa = (POA) u.getORB().resolve_initial_references("RootPOA");
            
            if (poa == null)
                System.out.println("POA is null :(");

            POA thePOA = f == null ? poa : f.createPOA(poa);

            BasicObjectFactoryImpl theFactory;
            if (f == null)
                theFactory = new BasicObjectFactoryImpl();
            else
                theFactory = (BasicObjectFactoryImpl)
                    Class.forName(f.getObjectFactoryName()).newInstance();

            System.out.println("Got the basic object factory");


            theFactory.setPOA(thePOA);
            
            byte[] id = thePOA.activate_object(theFactory);
            
            u.writeFactory(FactoryHelper.narrow(thePOA.servant_to_reference(theFactory)));          
            
            thePOA.the_POAManager().activate();

            // What does this thing do???????
            new Waiter(poa, theFactory).start();

            System.out.println("Server is ready.");
            u.getORB().run();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
