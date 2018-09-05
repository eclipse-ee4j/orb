/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poamanager;

import Util.GenericFactoryHelper;
import org.omg.PortableServer.POA;

public class HelloServer {
    public static void main(String[] args) {
        try {
            Utility u = new Utility(args);
            POA rootPoa = (POA) u.getORB().resolve_initial_references("RootPOA");
            POA poa = rootPoa.create_POA("AnotherPOA", null, null);
            rootPoa.the_POAManager().activate();

            FactoryImpl theFactory = new FactoryImpl(poa);

            byte[] id = rootPoa.activate_object(theFactory);
            
            u.writeFactory(GenericFactoryHelper.narrow(
                rootPoa.servant_to_reference(theFactory)));
            
            poa.the_POAManager().activate();

            System.out.println("Server is ready.");
            u.getORB().run();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
