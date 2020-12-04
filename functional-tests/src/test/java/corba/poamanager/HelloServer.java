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
