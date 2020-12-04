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

package pi.serviceexample;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.POA;

import java.util.Properties;

class ArbitraryObjectImpl
    extends ArbitraryObjectPOA
{
    public static ORB orb;

    private AService aService;

    //
    // The IDL operations.
    //

    public String arbitraryOperation1(String a1)
    {
        verifyService();
        return "I got this from the client: " + a1;
    }

    public void arbitraryOperation2 (int a1)
    {
        verifyService();
    }

    public void arbitraryOperation3(String a1)
        throws ArbitraryObjectException
    {
        verifyService();
        if (a1.equals("throw exception")) {
            throw new ArbitraryObjectException("because you told me to");
        }
    }

    private void verifyService()
    {
        getAService().verify();
    }

    private AService getAService()
    {
        // Only look up the service once, then cache it.

        if (aService == null) {
            try {
                aService =      
                    AServiceHelper.narrow(
                        orb.resolve_initial_references("AService"));
            } catch (InvalidName e) {
                System.out.println("Exception handling not shown.");
            }
        }
        return aService;
    }

    //
    // The server.
    //

    public static void main(String[] av)
    {
        try {
            if (orb == null) {
                Properties props = new Properties();
                props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "pi.serviceexample.AServiceORBInitializer",
                          "");
                props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "pi.serviceexample.LoggingServiceServerORBInitializer",
                          "");
                orb = ORB.init(av, props);
            }
            
            POA rootPOA =  (POA) orb.resolve_initial_references("RootPOA");
            // Create a POA so the IOR interceptor executes.
            POA childPOA = rootPOA.create_POA("childPOA", null, null);
            childPOA.the_POAManager().activate();
            
            byte[] objectId =
                childPOA.activate_object(new ArbitraryObjectImpl());
            org.omg.CORBA.Object ref = childPOA.id_to_reference(objectId);

            NamingContext nameService = 
                NamingContextHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            NameComponent path[] =
                { new NameComponent("ArbitraryObject", "") };
            nameService.rebind(path, ref);

            System.out.println("ArbitaryObject ready.");

            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}

// End of file.

