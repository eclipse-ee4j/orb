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

//
// Created       : 2001 May 23 (Wed) 15:24:44 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 19:50:01 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.util.Properties;

public class Client 
{
    public static void main(String av[])
    {
        try {
            Properties props = new Properties();
            props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                      + "pi.serviceexample.AServiceORBInitializer",
                      "");
            props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                      + "pi.serviceexample.LoggingServiceClientORBInitializer",
                      "");
            ORB orb = ORB.init(av, props);

            //
            // The client obtains a reference to a service.
            // The client does not know the service is implemented
            // using interceptors.
            //

            AService aService = 
                AServiceHelper.narrow(
                    orb.resolve_initial_references("AService"));

            //
            // The client obtains a reference to some object that
            // it will invoke.
            //

            NamingContext nameService = 
                NamingContextHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            NameComponent arbitraryObjectPath[] =
                { new NameComponent("ArbitraryObject", "") };
            ArbitraryObject arbitraryObject =
                ArbitraryObjectHelper.narrow(nameService.resolve(arbitraryObjectPath));

            //
            // The client begins the service so that invocations of
            // any object will be done with that service in effect.
            //

            aService.begin();
            
            arbitraryObject.arbitraryOperation1("one");
            arbitraryObject.arbitraryOperation2(2);

            //
            // The client ends the service so that further invocations
            // of any object will not be done with that service in effect.
            //

            aService.end();

            // This invocation is not serviced by aService since
            // it is outside the begin/end.
            arbitraryObject.arbitraryOperation3("just return");


            aService.begin();
            try {
                arbitraryObject.arbitraryOperation3("throw exception");
                throw new RuntimeException("should not see this");
            } catch (ArbitraryObjectException e) {
                // Expected in this example, so do nothing.
            }
            aService.end();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Client done.");
        System.exit(0);
    }
}

// End of file.

