/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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
